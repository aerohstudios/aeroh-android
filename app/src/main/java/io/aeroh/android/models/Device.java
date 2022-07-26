package io.aeroh.android.models;

import com.squareup.moshi.Json;

import moe.banana.jsonapi2.JsonApi;
import moe.banana.jsonapi2.Resource;

@JsonApi(type="devices")
public class Device extends Resource {
    @Json(name="name") public String name;
    @Json(name="thing-name") public String thing_name;
    @Json(name="mac-addr") public String mac_addr;

    @Json(name="certificate-pem") public String certificate_pem;
    @Json(name="certificate-public-key") public String certificate_public_key;
    @Json(name="certificate-private-key") public String certificate_private_key;

    @Json(name="mqtt-uri") public String mqtt_uri;
    @Json(name="root-ca") public String root_ca;

}
