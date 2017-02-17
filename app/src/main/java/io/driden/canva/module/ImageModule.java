package io.driden.canva.module;

import android.app.Application;
import android.graphics.Bitmap;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.driden.canva.Utils.DiskLruImageCache;

@Module
public class ImageModule {

    String filePath;
    long cacheSize;
    Bitmap.CompressFormat compressFormat;
    int quality;

    public ImageModule(String filePath, long cacheSize, Bitmap.CompressFormat compressFormat, int quality) {
        this.filePath = filePath;
        this.cacheSize = cacheSize;
        this.compressFormat = compressFormat;
        this.quality = quality;
    }

    @Provides
    @Singleton
    public DiskLruImageCache ProvidediskLruImageCache(Application application) {
        return new DiskLruImageCache(application, filePath, cacheSize, compressFormat, quality);
    }
}
