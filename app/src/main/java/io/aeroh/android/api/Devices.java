package io.aeroh.android.api;

import io.aeroh.android.models.Device;

import java.util.List;

import io.aeroh.android.models.Device;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface Devices {
    @GET("devices")
    Call<List<Device>> list();

    @GET("devices/{id}")
    Call<Device> get(@Path("id") String deviceId);

    @POST("devices")
    Call<Device> post(@Body Device device);

    @PUT("devices/{id}")
    Call<Device> put(@Path("id") String deviceId, @Body Device device);

    @DELETE("devices/{id}")
    Call<ResponseBody> delete(@Path("id") String deviceId);
}
