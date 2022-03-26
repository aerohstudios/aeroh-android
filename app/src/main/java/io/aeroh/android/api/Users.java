package io.aeroh.android.api;

import io.aeroh.android.models.User;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface Users {
    @GET("users")
    Call<List<User>> list();

    @GET("users/{id}")
    Call<User> get(@Path("id") int userId);
}
