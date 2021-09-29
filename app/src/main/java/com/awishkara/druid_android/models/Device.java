package com.awishkara.druid_android.models;

import com.squareup.moshi.Json;

import moe.banana.jsonapi2.JsonApi;
import moe.banana.jsonapi2.Resource;

@JsonApi(type="devices")
public class Device extends Resource {
    @Json(name="name") public String name;
    @Json(name="mac-addr") public String mac_addr;
}
