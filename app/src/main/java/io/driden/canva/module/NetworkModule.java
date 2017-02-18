package io.driden.canva.module;

import android.app.Application;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.driden.canva.Utils.BitmapUtils;
import io.driden.canva.Utils.HttpClientUtils;
import io.driden.canva.Utils.HttpClientUtilsImpl;
import okhttp3.Cache;
import retrofit2.Retrofit;

@Module
public class NetworkModule {

    private String baseUrl;

    public NetworkModule(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    @Provides
    @Singleton
    Cache provideHttpCache(Application application) {
        long cacheSize = BitmapUtils.MAX_DISC_CACHE_SIZE;
        return new Cache(application.getCacheDir(), cacheSize);
    }

    @Provides
    @Singleton
    HttpClientUtils provideOkHttpClient(Cache cache) {

        HttpClientUtils clientUtils = new HttpClientUtilsImpl(cache);
        return clientUtils;
    }

    @Provides
    @Singleton
    Retrofit.Builder provideRetrofit() {
        return new Retrofit.Builder()
                .baseUrl(baseUrl);
    }
}
