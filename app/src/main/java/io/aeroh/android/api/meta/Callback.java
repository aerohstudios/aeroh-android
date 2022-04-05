package io.aeroh.android.api.meta;

public interface Callback {
    enum failureType {
        INVALID_TOKEN,
        SERVER_ERROR
    }

    void onSuccess();
    void onFailure(failureType type, String message);
}
