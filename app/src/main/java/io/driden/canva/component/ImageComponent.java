package io.driden.canva.component;

import javax.inject.Singleton;

import dagger.Component;
import io.driden.canva.module.AppModule;
import io.driden.canva.module.ImageModule;
import io.driden.canva.module.NetworkModule;
import io.driden.canva.presenter.MainFragmentPresenter;
import io.driden.canva.task.DrawingTask;

@Singleton
@Component(modules = {AppModule.class, NetworkModule.class, ImageModule.class})
public interface ImageComponent {

    void inject(MainFragmentPresenter obj);
    void inject(DrawingTask obj);

}
