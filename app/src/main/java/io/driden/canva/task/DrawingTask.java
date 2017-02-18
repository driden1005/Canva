package io.driden.canva.task;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;

import java.io.InputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.inject.Inject;

import io.driden.canva.Utils.BitmapUtils;
import io.driden.canva.Utils.DiskLruImageCache;
import io.driden.canva.app.CanvaApplication;
import io.driden.canva.data.ImageInfo;
import io.driden.canva.data.TileInfo;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class DrawingTask implements Runnable {

    String TAG = getClass().getSimpleName();

    byte[] byteArray;
    ExecutorService threadExecutor;
    Canvas mainCanvas;
    ImageInfo imageInfo;

    @Inject
    DiskLruImageCache imageCache;   // Disk Cache

    Retrofit retrofit;

    public interface DrawingRowListener {
        void onDrawingFinished(boolean isFinished);

        void onDrawingFailed(Throwable t);

    }

    DrawingRowListener listener;

    public DrawingTask(Retrofit retrofit, byte[] byteArray, Canvas mainCanvas, ImageInfo imageInfo, DrawingRowListener listener) {
        CanvaApplication.getImageComponent().inject(this);
        this.retrofit = retrofit;
        this.byteArray = byteArray;
        this.mainCanvas = mainCanvas;
        this.imageInfo = imageInfo;
        this.listener = listener;
        this.threadExecutor = Executors.newCachedThreadPool();
    }

    /**
     * It creates network connections to the rest API.
     * Save the mosaics into the cache.
     *
     * @param row         'n'th row
     * @param colorIntRow the array of color Integers in a row.
     */
    void receiveRowColors(final int row, int[] colorIntRow) {
        // Make the network requests.
        final TileInfo[] tileInfos = BitmapUtils.makeTileRequest(retrofit, imageInfo, colorIntRow);

//        int col = 0;
        // A complete row of the mosaic tiles is placed in this bitmap.
        final Bitmap rowBitmap = Bitmap.createBitmap(imageInfo.getTileWidth() * imageInfo.getColNum(), imageInfo.getTileHeight(), Bitmap.Config.ARGB_8888);

        final Canvas rowCanvas = new Canvas(rowBitmap);

        for (int col = 0; col < tileInfos.length; col++) {

            final TileInfo tileInfo = tileInfos[col];

            final int width = imageInfo.getTileWidth() * col;

            final String key = tileInfo.getKey();

            final int colNum = col;

            if (imageCache.containsKey(key)) {
                Bitmap tile = imageCache.get(key);
                drawBitmap(rowCanvas, tile, width, tileInfo, key);

                if (colNum == tileInfos.length - 1) {
                    drawRows(rowBitmap, row);
                }
            } else {
//                    Response<ResponseBody> response = tileInfo.getCall().execute();
                tileInfo.getCall().enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        InputStream in = response.body().byteStream();
                        Bitmap tile = BitmapFactory.decodeStream(in);
                        drawBitmap(rowCanvas, tile, width, tileInfo, key);

                        if (colNum == tileInfos.length - 1) {
                            drawRows(rowBitmap, row);
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        if (listener != null) {
                            listener.onDrawingFailed(t);
                        }
                    }
                });

            }
        }
    }

    void drawRows(Bitmap rowBitmap, int row) {
        // draw the row set of the tile bitmaps on the target image.
        mainCanvas.drawBitmap(rowBitmap, 0, imageInfo.getTileWidth() * row, null);
        rowBitmap.recycle();

        // callback to update the ImageView.
        if (listener != null) {
            if (row == imageInfo.getRowNum() - 1) {
                listener.onDrawingFinished(true);
            } else {
                listener.onDrawingFinished(false);
            }
        }
    }

    void drawBitmap(Canvas canvas, Bitmap tile, int width, TileInfo tileInfo, String key) {
        canvas.drawBitmap(tile, width, 0, null);

        if (!imageCache.containsKey(key)) {
            imageCache.put(tileInfo.getKey(), tile);
        }
        tile.recycle();
    }

    @Override
    public void run() {
        // Regions of the image as tiles.
        Rect[][] rects = BitmapUtils.initRectArray(imageInfo);

        for (int row = 0; row < imageInfo.getRowNum(); row++) {
            // get row sets of the average colors.
            Callable colorIntTask = new ColorIntTask(imageInfo, rects[row], row, byteArray);
            Future<int[]> future = threadExecutor.submit(colorIntTask);
            try {
                receiveRowColors(row, future.get());
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }

        threadExecutor.shutdown();
    }
}
