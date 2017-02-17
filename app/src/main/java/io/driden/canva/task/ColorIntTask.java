package io.driden.canva.task;

import android.graphics.Rect;
import android.support.annotation.ColorInt;

import java.util.concurrent.Callable;

import io.driden.canva.Utils.BitmapUtils;
import io.driden.canva.data.ImageInfo;

public class ColorIntTask implements Callable<int[]> {

    String TAG = getClass().getSimpleName();

    ImageInfo imageInfo;
    Rect[] rects;
    int row;
    byte[] byteArray;

    public ColorIntTask(ImageInfo imageInfo, Rect[] rects, int row, byte[] byteArray) {
        this.imageInfo = imageInfo;
        this.rects = rects;
        this.row = row;
        this.byteArray = byteArray;
    }

    @Override
    public int[] call() throws Exception {
        @ColorInt int[] colorInts = new int[rects.length];

        int col = 0;
        for (Rect rect : rects) {
            colorInts[col] = BitmapUtils.getAvgColorInt(BitmapUtils.getTileFromImage(rect, byteArray));
            col++;
        }

        return colorInts;
    }
}
