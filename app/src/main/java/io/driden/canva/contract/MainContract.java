package io.driden.canva.contract;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;

public interface MainContract {

    interface Presenter {

        void drawMosaicImage(@NonNull String filePath, int tileWidth, int tileHeight);

        void setView(MainContract.View view);

        void shutDownThreadExecutor();

        void checkBitmapSize(String filePath);

        boolean getIsFinished();

//        boolean isThreadAlive();
    }

    interface View {
        void updateImageView(Bitmap fullImage);

        void updateTextInfo(String text);
    }
}
