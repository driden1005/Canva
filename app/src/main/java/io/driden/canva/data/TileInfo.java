package io.driden.canva.data;

import okhttp3.ResponseBody;
import retrofit2.Call;

public class TileInfo {

    private Call<ResponseBody> call;
    private String key;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Call<ResponseBody> getCall() {
        return call;
    }

    public void setCall(Call<ResponseBody> call) {
        this.call = call;
    }

}
