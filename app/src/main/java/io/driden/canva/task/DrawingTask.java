package io.driden.canva.task;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;

import java.io.IOException;
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
import retrofit2.Response;
import retrofit2.Retrofit;

public class DrawingTask implements Runnable {

    String TAG = getClass().getSimpleName();

    byte[] byteArray;
    ExecutorService threadExecutor;
    Canvas mainCanvas;
    ImageInfo imageInfo;

    @Inject
    Retrofit retrofit;

    @Inject
    DiskLruImageCache imageCache;   // Disk Cache

    public interface DrawingRowListener {
        void onDrawingFinished(boolean isFinished);
    }

    DrawingRowListener listener;

    public DrawingTask(byte[] byteArray, Canvas mainCanvas, ImageInfo imageInfo, DrawingRowListener listener) {

        CanvaApplication.getImageComponent().inject(this);

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
    void receiveRowColors(int row, int[] colorIntRow) {
        // Make the network requests.
        TileInfo[] tileInfos = BitmapUtils.makeTileRequest(retrofit, imageInfo, colorIntRow);

        int col = 0;
        // A complete row of the mosaic tiles is placed in this bitmap.
        Bitmap rowBitmap = Bitmap.createBitmap(imageInfo.getTileWidth() * imageInfo.getColNum(), imageInfo.getTileHeight(), Bitmap.Config.ARGB_8888);

        Canvas rowCanvas = new Canvas(rowBitmap);

        for (TileInfo tileInfo : tileInfos) {

            try {

                String key = tileInfo.getKey();

                Bitmap tile;

                if (imageCache.containsKey(key)) {
                    tile = imageCache.get(key);
                } else {
                    Response<ResponseBody> response = tileInfo.getCall().execute();
                    InputStream in = response.body().byteStream();
                    tile = BitmapFactory.decodeStream(in);
                }

                rowCanvas.drawBitmap(tile, imageInfo.getTileWidth() * col, 0, null);

                if (!imageCache.containsKey(key)) {
                    imageCache.put(tileInfo.getKey(), tile);
                }
                tile.recycle();
            } catch (IOException e) {
                e.printStackTrace();
            }

            col++;
        }
        // draw the row set of the tile bitmaps on the target image.
        mainCanvas.drawBitmap(rowBitmap, 0, imageInfo.getTileWidth() * row, null);
        rowBitmap.recycle();

        // callback to update the ImageView.
        if (listener != null) {
            if(row == imageInfo.getRowNum()-1){
                listener.onDrawingFinished(true);
            }else{
                listener.onDrawingFinished(false);
            }
        }
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
