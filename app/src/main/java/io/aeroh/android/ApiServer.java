package io.aeroh.android;

import io.aeroh.android.api.Devices;
import io.aeroh.android.api.Users;
import io.aeroh.android.api.meta.Callback;
import io.aeroh.android.models.Device;
import io.aeroh.android.models.User;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import java.io.IOException;
import java.util.List;

import moe.banana.jsonapi2.JsonApiConverterFactory;
import moe.banana.jsonapi2.ResourceAdapterFactory;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import retrofit2.Call;
import retrofit2.Retrofit;

public class ApiServer {
    private String access_token;
    private String baseUrl = BuildConfig.API_SERVER_SCHEME + "://" + BuildConfig.API_SERVER_HOST + "/api/v1/";

    public Users users;
    public Devices devices;

    ApiServer(String access_token) {
        this.access_token = access_token;

        JsonAdapter.Factory jsonApiAdapterFactory = ResourceAdapterFactory.builder()
                .add(User.class, Device.class)
                .build();

        Moshi moshi = new Moshi.Builder()
                .add(jsonApiAdapterFactory)
                .build();

        OkHttpClient httpClient = new OkHttpClient.Builder()
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        okhttp3.Request.Builder requestBuilder = chain.request().newBuilder();
                        requestBuilder.header("Authorization", "Bearer " + access_token);
                        return chain.proceed(requestBuilder.build());
                    }
                })
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(httpClient)
                .addConverterFactory(JsonApiConverterFactory.create(moshi))
                .build();
        this.users = retrofit.create(Users.class);
        this.devices = retrofit.create(Devices.class);
    }

    void isAuthenticated(Callback cb) {
        Call<List<User>> call = this.users.list();

        call.enqueue(new retrofit2.Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, retrofit2.Response<List<User>> response) {
                int statusCode = response.code();
                if (statusCode == 200) {
                    cb.onSuccess();
                } else if (statusCode == 401) {
                    cb.onFailure();
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                // log failure
            }
        });
    }
}
