package io.driden.canva.executor;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public interface ThreadExecutor {

    void execute(Runnable runnable);
    Future<?> submit(Runnable runnable);
    <T> Future<T> submit(Callable<T> callable);

    void shutdown();
    void shutdownNow();
    boolean isShutdown();
    boolean isTerminated();

}
