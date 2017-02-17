package io.driden.canva.executor;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class TileThreadExecutor implements ThreadExecutor {

    private static TileThreadExecutor instance;
    static final BlockingQueue<Runnable> WORK_QUEUE = new LinkedBlockingQueue<>();
    ThreadPoolExecutor poolExecutor;

    TileThreadExecutor(int corePoolSize, int maxPoolSize, int keepAliveTime) {
        poolExecutor = new ThreadPoolExecutor(corePoolSize, maxPoolSize, keepAliveTime, TimeUnit.SECONDS, WORK_QUEUE);
    }

    public static synchronized  TileThreadExecutor getInstance(int corePoolSize, int maxPoolSize, int keepAliveTime){

        if(instance==null){
            instance = new TileThreadExecutor(corePoolSize, maxPoolSize, keepAliveTime);
        }

        return instance;
    }

    @Override
    public void execute(Runnable runnable){
        poolExecutor.execute(runnable);
    }

    @Override
    public Future<?> submit(Runnable runnable) {
        return poolExecutor.submit(runnable);
    }

    @Override
    public <T> Future<T> submit(Callable<T> callable) {

        return poolExecutor.submit(callable);
    }

    public void shutdown() {
        poolExecutor.shutdown();
    }

    public void shutdownNow() {
        poolExecutor.shutdownNow();
    }

    public boolean isShutdown(){
        return poolExecutor.isShutdown();
    }

    @Override
    public boolean isTerminated() {
        return poolExecutor.isTerminated() || poolExecutor.isTerminating();
    }

}
