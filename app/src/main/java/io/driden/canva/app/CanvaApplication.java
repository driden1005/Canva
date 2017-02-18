package io.driden.canva.app;

import android.app.Application;
import android.graphics.Bitmap;

import io.driden.canva.R;
import io.driden.canva.component.DaggerImageComponent;
import io.driden.canva.component.ImageComponent;
import io.driden.canva.module.AppModule;
import io.driden.canva.module.ImageModule;
import io.driden.canva.module.NetworkModule;

//import io.driden.canva.component.DaggerAppComponent;

public class CanvaApplication extends Application {

    static ImageComponent imageComponent;

    long MAX_DISC_CACHE_SIZE = 10 * 1024 * 1024;

    @Override
    public void onCreate() {
        super.onCreate();

        imageComponent = DaggerImageComponent.builder()
                .appModule(new AppModule(this))
                .networkModule(new NetworkModule(getString(R.string.base_url)))
                .imageModule(new ImageModule("/tiles", MAX_DISC_CACHE_SIZE, Bitmap.CompressFormat.PNG, 100))
                .build();
    }

    public static ImageComponent getImageComponent() {
        return imageComponent;
    }
}