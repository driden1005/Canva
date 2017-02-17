package io.driden.canva.api;


import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface TileAPI {
    @GET("/color/{width}/{height}/{hexcolor}")
    Call<ResponseBody> getTile(@Path("width") int width, @Path("height") int height, @Path("hexcolor") String hexcolor);
}
