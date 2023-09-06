package io.aeroh.android.models;

import com.squareup.moshi.Json;

import moe.banana.jsonapi2.JsonApi;
import moe.banana.jsonapi2.Resource;

@JsonApi(type="users")
public class User extends Resource {
    @Json(name="first-name") public String firstName;
    @Json(name="email") public String email;

}
