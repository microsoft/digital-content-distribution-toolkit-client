package com.msr.bine_sdk.network_old;

import java.io.IOException;

import okhttp3.Response;

public interface NetworkCallback {
    /**
     * called when the server response was not 2xx or when an exception was thrown in the process
     * @param response - in case of server error (4xx, 5xx) this contains the server response
     *                 in case of IO exception this is null
     * @param exception - contains the exception. in case of server error (4xx, 5xx) this is null
     */
    void onFailure(Response response, IOException exception);

    /**
     * contains the server response
     * @param response
     */
    void onSuccess(Response response);
}
