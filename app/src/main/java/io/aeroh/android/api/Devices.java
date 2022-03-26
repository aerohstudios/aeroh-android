package io.aeroh.android.api;

import io.aeroh.android.models.Device;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface Devices {
    @GET("devices")
    Call<List<Device>> list();

    @GET("devices/{id}")
    Call<Device> get(@Path("id") int deviceId);
}
