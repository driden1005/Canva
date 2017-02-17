package io.driden.canva.module;

import android.app.Application;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class AppModule {

    public  Application application;

    public AppModule(Application application) {
        this.application = application;
    }

    @Provides
    @Singleton
    Application provideApplication() {
        return application;
    }
}
