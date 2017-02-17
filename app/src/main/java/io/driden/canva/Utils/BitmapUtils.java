package io.driden.canva.Utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Color;
import android.graphics.Rect;
import android.support.annotation.ColorInt;
import android.util.DisplayMetrics;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;

import io.driden.canva.api.TileAPI;
import io.driden.canva.data.ImageInfo;
import io.driden.canva.data.TileInfo;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;

public class BitmapUtils {

    static final String TAG = BitmapUtils.class.getSimpleName();

    /**
     * Instantiate BitmapRegionDecorder.
     *
     * @param filePath the image file path.
     * @return the BitmapRegionDecorder object.
     */
    public static BitmapRegionDecoder initBitmapRegionDecoder(String filePath) {
        try {
            return BitmapRegionDecoder.newInstance(filePath, false);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Filling Tiles into the original image size is not always fitted.
     * It calculates the number of required tiles,
     * the width of the height of new image size according to the number of the tile.
     *
     * @param filePath   the image file path.
     * @param tileWidth  the width of the tile.
     * @param tileHeight the height of the tile.
     * @return the information relating the new image size.
     */
    public static ImageInfo processFileInfo(String filePath, int tileWidth, int tileHeight, DisplayMetrics metrics) {

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);

        // get original sizes of the image.
        int originalW = options.outWidth;
        int originalH = options.outHeight;

        float sampleSize = getScaleSize(originalW, originalH, metrics);

        options.inSampleSize = Math.round(sampleSize);
        BitmapFactory.decodeFile(filePath, options);

        int scaledW = options.outWidth;
        int scaledH = options.outHeight;

        // get the number of rows and columns of tiles required to fill the most of the original size.
        int columnNum = scaledW / tileWidth;
        int rowNum = scaledH / tileHeight;

        // new image size
        int croppedW = columnNum * tileWidth;
        int croppedH = rowNum * tileHeight;

        // get start x, y points to crop image
        int startX = (scaledW - croppedW) / 2;
        int startY = (scaledH - croppedH) / 2;

        ImageInfo imageInfo = new ImageInfo();

        imageInfo.setFilePath(filePath);
        imageInfo.setOriginalW(originalW);
        imageInfo.setOriginalH(originalH);
        imageInfo.setCroppedW(croppedW);
        imageInfo.setCroppedH(croppedH);
        imageInfo.setStartX(startX);
        imageInfo.setStartY(startY);
        imageInfo.setRowNum(rowNum);
        imageInfo.setColNum(columnNum);
        imageInfo.setTileWidth(tileWidth);
        imageInfo.setTileHeight(tileHeight);
        imageInfo.setSampleSize(options.inSampleSize);

        Log.d(TAG, imageInfo.toString());

        return imageInfo;
    }

    /**
     * Cut the image
     *
     * @param imageInfo the informaiton of the image to be processed.
     * @return the bitmap.
     */
    public static Bitmap cropBitmap(ImageInfo imageInfo) {
        Rect rect = new Rect();
        BitmapRegionDecoder decoder = initBitmapRegionDecoder(imageInfo.getFilePath());
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = imageInfo.getSampleSize();

        rect.set(
                0,
                0,
                decoder.getWidth(),
                decoder.getHeight()
        );

        // The bitmap from image file is immutable. "copy" it to make mutable.
        final Bitmap tempBitmap = decoder.decodeRegion(rect, options).copy(Bitmap.Config.ARGB_8888, true);
        Bitmap croppedBitmap = Bitmap.createBitmap(tempBitmap, imageInfo.getStartX(),
                imageInfo.getStartY(),
                imageInfo.getCroppedW() + imageInfo.getStartX(),
                imageInfo.getCroppedH() + imageInfo.getStartY());
        decoder.recycle();

        if (tempBitmap != croppedBitmap) {
            tempBitmap.recycle();
        }

        return croppedBitmap;
    }

    public static synchronized String saveFile(Bitmap bitmap, String filePath) {

        FileOutputStream out = null;
        try {
            out = new FileOutputStream(filePath);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return filePath;
    }

    /**
     * It reads every single pixels in the bitmap to sample colors.
     *
     * @param bitmap the specific region of the target bitmap.
     * @return Integer value of the average RGB colors in the bitmap.
     */
    public static int getAvgColorInt(Bitmap bitmap) {
        @ColorInt int R = 0;
        @ColorInt int G = 0;
        @ColorInt int B = 0;

        int pixelCount = 0;

        for (int i = 0; i < bitmap.getWidth(); i++) {
            for (int j = 0; j < bitmap.getHeight(); j++) {
                int color = bitmap.getPixel(i, j);

                R += Color.red(color);
                G += Color.green(color);
                B += Color.blue(color);

                pixelCount++;
            }
        }

        bitmap.recycle();

        return Color.rgb(R / pixelCount, G / pixelCount, B / pixelCount);
    }

    /**
     * Convert the color integer to a string of the hex color.
     *
     * @param color
     * @return the string of the hex color value
     */
    public static String convertColorIntToHexString(int color) {
        return String.format("%06X", (0xFFFFFF & color));
    }


    /**
     * the 2D Rect array sequentially save the regions of each tile.
     *
     * @param imageInfo
     * @return 2D rect array.
     */
    public static Rect[][] initRectArray(ImageInfo imageInfo) {
        Rect[][] rects = new Rect[imageInfo.getRowNum()][imageInfo.getColNum()];

        for (int row = 0; row < imageInfo.getRowNum(); row++) {
            for (int col = 0; col < imageInfo.getColNum(); col++) {
                rects[row][col] = new Rect(
                        col * imageInfo.getTileWidth(),
                        row * imageInfo.getTileHeight(),
                        (col + 1) * imageInfo.getTileWidth(),
                        (row + 1) * imageInfo.getTileHeight()
                );
            }
        }

        return rects;
    }

    /**
     * Sample a specific region of the target bitmap.
     * The region size is as same as the tile size.
     * The returning bitmap is used for calculating the average color.
     *
     * @param rect
     * @param croppedImage
     * @return
     */
    public static Bitmap getTileFromImage(Rect rect, byte[] byteArray) {

//        ByteArrayOutputStream stream = new ByteArrayOutputStream();
//        croppedImage.compress(Bitmap.CompressFormat.PNG, 100, stream);
//        byte[] byteArray = stream.toByteArray();
        try {
            BitmapRegionDecoder decoder = BitmapRegionDecoder.newInstance(byteArray, 0, byteArray.length, true);
            BitmapFactory.Options options = new BitmapFactory.Options();
            Bitmap imageTile = decoder.decodeRegion(rect, options);
            return imageTile;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Make the array of Retrofit requests
     *
     * @param retrofit
     * @param imageInfo
     * @param colors
     * @return
     */
    public static TileInfo[] makeTileRequest(Retrofit retrofit, ImageInfo imageInfo, int[] colors) {

        TileInfo[] tileInfos = new TileInfo[colors.length];

        for (int col = 0; col < colors.length; col++) {

            tileInfos[col] = new TileInfo();

            String hexColor = BitmapUtils.convertColorIntToHexString(colors[col]);

            Call<ResponseBody> call = retrofit.create(TileAPI.class).getTile(imageInfo.getTileWidth(), imageInfo.getTileHeight(), hexColor);
            tileInfos[col].setCall(call);
            // keys must match regex [a-z0-9_-]{1,64}
            String tileKey = String.format("color_%d_%d_%s", imageInfo.getTileWidth(), imageInfo.getTileHeight(), hexColor.toLowerCase());
            tileInfos[col].setKey(tileKey);
        }

        return tileInfos;
    }

    public static float getScaleSize(int w, int h, DisplayMetrics metrics) {
        int bitmapWidth = w;
        int bitmapHeight = h;

        int screenWidth = metrics.widthPixels;
        int screenHeight = metrics.heightPixels;

        float sampleSize = 1;

        if (bitmapHeight > screenHeight || bitmapWidth > screenWidth) {

            float scaledWidth = bitmapWidth / sampleSize;
            float scaledHeight = bitmapHeight / sampleSize;

            while (scaledWidth > screenWidth || scaledHeight > screenHeight) {
                sampleSize += 0.1;
                scaledWidth = bitmapWidth / sampleSize;
                scaledHeight = bitmapHeight / sampleSize;
            }
        }

        return sampleSize;
    }

    /**
     * Resize if the original image is larger than the device screen size.
     *
     * @param filePath the image file path
     * @param metrics  device metrics
     * @return resized bitmap
     */
    public static Bitmap resizeBitmap(String filePath, DisplayMetrics metrics) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        // If it is true, it allows querying the bitmap without allocating the memory.
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(filePath, options);

        int bitmapWidth = options.outWidth;
        int bitmapHeight = options.outHeight;

        float sampleSize = getScaleSize(bitmapWidth, bitmapHeight, metrics);

        options.inSampleSize = Math.round(sampleSize);

        options.inJustDecodeBounds = false;

        Bitmap bitmap = BitmapFactory.decodeFile(filePath, options);

        return bitmap;
    }
}
