package io.driden.canva.contract;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.widget.ImageView;

public interface MainContract {

    interface Presenter {

        void drawMosaicImage(@NonNull String filePath, int tileWidth, int tileHeight);

        void setView(MainContract.View view);

        void shutDownThreadExecutor();

        void checkBitmapSize(String filePath);

        boolean getIsFinished();

        void downloadImage(ImageView mImageView);

        void updateTextInfo(String infoText);

        void setByteArray(byte[] bitmapByteArray);

//        boolean isThreadAlive();
    }

    interface View {
        void updateImageView(Bitmap fullImage, boolean isFinished);

        void updateTextInfo(String text);
    }
}
