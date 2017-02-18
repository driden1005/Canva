package io.driden.canva.presenter;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.driden.canva.Utils.BitmapUtils;
import io.driden.canva.Utils.DiskLruImageCache;
import io.driden.canva.Utils.HttpClientUtils;
import io.driden.canva.app.CanvaApplication;
import io.driden.canva.contract.MainContract;
import io.driden.canva.data.ImageInfo;
import io.driden.canva.task.DrawingTask;
import retrofit2.Retrofit;

public class MainFragmentPresenter implements MainContract.Presenter {

    String TAG = getClass().getSimpleName();

    Activity activity;

    MainContract.View view;

    ExecutorService threadExecutor;

    boolean isThreadFinished = true;

    @Inject
    Retrofit.Builder retrofitBuilder;
    @Inject
    HttpClientUtils clientUtils;
    @Inject
    DiskLruImageCache imageCache;

    public MainFragmentPresenter(Activity activity) {
        CanvaApplication.getImageComponent().inject(this);
        this.activity = activity;
    }

    public void setView(MainContract.View view) {
        this.view = view;
    }


    /**
     * Check the original image size, and return the file path.
     *
     * @param filePath The image file path.
     */
    @Override
    public void checkBitmapSize(String filePath) {
        BitmapFactory.Options options = new BitmapFactory.Options();

        // if it is true, it allows querying the bitmap without allocating the memory.
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);

        int bitmapWidth = options.outWidth;
        int bitmapHeight = options.outHeight;

        String imageType = options.outMimeType;

        String text = String.format("W%1$d x H%2$d", bitmapWidth, bitmapHeight);

        view.updateTextInfo(text);

    }

    public boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    /**
     * The function has three stages.
     * First, process the original bitmap.
     * Second, run threads to avoid running batch processes on the UI thread.
     * Third, Update the ImageView in every row of tiles is created.
     *
     * @param filePath   the image file path
     * @param tileWidth  the width of the tile
     * @param tileHeight the height of the tile
     */
    @Override
    public void drawMosaicImage(@NonNull String filePath, int tileWidth, int tileHeight) {

        isThreadFinished = false;

        System.gc();

        if (!isNetworkConnected()) {
            Toast.makeText(activity, "The network connection is off.", Toast.LENGTH_SHORT).show();
            return;
        }

        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);

        BitmapRegionDecoder decoder = BitmapUtils.initBitmapRegionDecoder(filePath);

        if (decoder == null) {
            Log.d(TAG, "drawMosaicImage: No image file found.");
            return;
        }

        decoder.recycle();

        DisplayMetrics metrics = activity.getResources().getDisplayMetrics();

        // crop the original image in order to fit N, M multiple of the width and height of the tile.
        ImageInfo imageInfo = BitmapUtils.processFileInfo(filePath, tileWidth, tileHeight, metrics);

        String text = String.format("W%d x H%d\nResized by W%d x H%d", imageInfo.getOriginalW(), imageInfo.getOriginalH(), imageInfo.getCroppedW(), imageInfo.getCroppedH());

        view.updateTextInfo(text);

        final Bitmap croppedImage = BitmapUtils.cropBitmap(imageInfo);

        // Full Image Canvas
        Canvas mainCanvas = new Canvas(croppedImage);

        threadExecutor = Executors.newCachedThreadPool();

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        croppedImage.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();

        Retrofit retrofit = retrofitBuilder.client(clientUtils.getClient()).build();

        final DrawingTask drawingTask = new DrawingTask(retrofit, byteArray, mainCanvas, imageInfo,
                new DrawingTask.DrawingRowListener() {
                    @Override
                    public void onDrawingFinished(boolean isFinished) {
                        view.updateImageView(croppedImage, isFinished);
                        if (isFinished) {
                            setIsFinished(isFinished);
                            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                        }
                    }

                    @Override
                    public void onDrawingFailed(Throwable t) {
                        Log.d(TAG, t.getMessage());
                        setIsFinished(true);
                        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                        threadExecutor.shutdown();
                    }
                }
        );

        threadExecutor.execute(drawingTask);

    }

    public void setIsFinished(boolean isFinished) {
        this.isThreadFinished = isFinished;
    }

    @Override
    public boolean getIsFinished() {

        return isThreadFinished;
    }

    /**
     * Save the mosaic bitmap into the file
     * The file path is the same as the original file path.
     *
     * @param mImageView
     */
    @Override
    public void downloadImage(ImageView mImageView) {
        if (!getIsFinished()) {
            Toast.makeText(activity, "The job is still running", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            String filePath = (String) mImageView.getTag();

            if (!"".equals(filePath)) {
                String filename = filePath.substring(filePath.lastIndexOf("/") + 1);


                StringBuffer sb = new StringBuffer();
                sb.append("result_").append(filename.replaceFirst("(\\.[^.]*$|$)", ".png"));

                String newFileName = sb.toString();

                String savingPath = filePath.replace(filename, newFileName);

                Bitmap bitmap = ((BitmapDrawable) mImageView.getDrawable()).getBitmap();
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(savingPath);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (fos != null) {
                            fos.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                File file = new File(savingPath);

                if (file.isFile()) {
                    Toast.makeText(activity, "File has been saved", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "downloadImage: File has been saved:" + file.getAbsolutePath());
                } else {
                    Toast.makeText(activity, "No File has been saved", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "downloadImage: No File has been saved");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(activity, "No file exits", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "downloadImage: No such Image:" + e.getMessage());
        }
    }

    @Override
    public void updateTextInfo(String infoText) {
        view.updateTextInfo(infoText);
    }

    @Override
    public void setByteArray(byte[] bitmapByteArray) {
        Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapByteArray, 0, bitmapByteArray.length, new BitmapFactory.Options());
        view.updateImageView(bitmap, false);
    }

    public void shutDownThreadExecutor() {
        if (threadExecutor != null) {
            threadExecutor.shutdown();
            try {
                threadExecutor.awaitTermination(3, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
