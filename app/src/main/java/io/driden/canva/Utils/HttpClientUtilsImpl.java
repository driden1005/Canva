package io.driden.canva.Utils;

import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;

public class HttpClientUtilsImpl extends OkHttpClient implements HttpClientUtils{

    OkHttpClient client;

    public HttpClientUtilsImpl(Cache cache){

        this.client = new OkHttpClient().newBuilder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .cache(cache).build();
    }

    public void addInterceptor(Interceptor interceptor){
        client.interceptors().add(interceptor);
    }

    public OkHttpClient getClient(){
        return client;
    }

}
