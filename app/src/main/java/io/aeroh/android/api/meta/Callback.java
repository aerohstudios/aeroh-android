package io.aeroh.android.api.meta;

public interface Callback {
    enum failureType {
        INVALID_TOKEN,
        CANNOT_REACH_SERVER,
        SERVER_ERROR
    }

    void onSuccess();
    void onFailure(failureType type, String message);
}
