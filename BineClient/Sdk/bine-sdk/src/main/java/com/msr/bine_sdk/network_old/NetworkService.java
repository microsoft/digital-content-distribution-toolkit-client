package com.msr.bine_sdk.network_old;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class NetworkService {

    private OkHttpClient client;
    protected NetworkCallback callback;

    /**
     * Network call to GET from a url
     * @param baseURL
     * @param endpoint
     * @param cb
     */
    public void get(String baseURL, String endpoint, NetworkCallback cb) {
        callback = cb;
        call("GET", null, HttpUrl.get(baseURL  + endpoint));
    }

    /**
     * Network call to POST from a url
     * @param url - the url
     * @param cb - {@NetworkCallback}
     */
    public void post(String url, RequestBody requestBody, NetworkCallback cb) {
        callback = cb;
        call("POST", requestBody, HttpUrl.get(url));
    }

    protected void call(String method, RequestBody requestBody, HttpUrl url) {
        if(client == null) {
            client = new OkHttpClient.Builder()
                    .addInterceptor(new LoggingInterceptor()).build();
        }
        Request.Builder requestBuilder = new Request.Builder()
                .url(url);

        if(!method.equals("GET")) {
            requestBuilder.method(method, requestBody);
        }
        else {
            requestBuilder.method(method, null);
        }

        client.newCall(requestBuilder.build()).enqueue(new Callback() {
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    callback.onFailure(response, null);
                    return;
                }
                callback.onSuccess(response);
            }

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                callback.onFailure(null, e);
            }
        });
    }
}
