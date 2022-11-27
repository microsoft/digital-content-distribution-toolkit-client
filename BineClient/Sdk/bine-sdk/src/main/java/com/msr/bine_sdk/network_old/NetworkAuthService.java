package com.msr.bine_sdk.network_old;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.util.Log;

import com.msr.bine_sdk.Constants;
import com.msr.bine_sdk.auth.AuthLogoutException;
import com.msr.bine_sdk.hub.TokenAuthenticator;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.Authenticator;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Response;

public class NetworkAuthService {
    private static final String TAG = "NetworkService";
    private OkHttpClient client;
    private Context context;

    public void get(Request request,
                    Authenticator authenticator,
                    NetworkCallback cb, Context context) {
        this.context = context;
        call(request, "GET", authenticator, cb);
    }

    public void post(Request request,
                     Authenticator authenticator,
                     NetworkCallback cb, Context context) {
        this.context = context;
        call(request, "POST", authenticator, cb);
    }

    public Response getSync(Request request, TokenAuthenticator authenticator) throws IOException {
        if (client == null) {
            client = new OkHttpClient.Builder()
                    .addInterceptor(new LoggingInterceptor())
                    .authenticator(authenticator)
                    .addInterceptor(new RetryRequestInterceptor())
                    .build();
        }
        HttpUrl.Builder httpUrl = HttpUrl.parse(request.baseURl + request.endpoint).newBuilder();
        if (request.queryParams != null) {
            for (String key : request.queryParams.keySet()) {
                httpUrl.addQueryParameter(key, request.queryParams.get(key));
            }
        }

        okhttp3.Request okhttpRequest = new okhttp3.Request.Builder().url(httpUrl.build())
                .addHeader(Constants.AUTH_HEADER, "Bearer " + request.token.getAccess())
                .build();

        return client.newCall(okhttpRequest).execute();
    }

    protected void call(Request request,
                        final String method,
                        Authenticator authenticator,
                        final NetworkCallback callback) {
        if (client == null) {
            client = new OkHttpClient.Builder()
                    .authenticator(authenticator)
                    .addInterceptor(new LoggingInterceptor())
                    .addInterceptor(new RetryRequestInterceptor())
                    .build();
        }

        HttpUrl.Builder httpUrl = HttpUrl.parse(request.baseURl + request.endpoint).newBuilder();

        if (request.queryParams != null) {
            for (String key : request.queryParams.keySet()) {
                httpUrl.addQueryParameter(key, request.queryParams.get(key));
            }
        }

        Log.d(TAG, request.token.getAccess());
        Log.d(TAG, request.token.getRefresh());
        okhttp3.Request.Builder requestBuilder = new okhttp3.Request.Builder()
                .url(httpUrl.build())
                .addHeader(Constants.AUTH_HEADER, "Bearer " + request.token.getAccess());

        if (!method.equals("GET")) {
            FormBody.Builder requestBody = new FormBody.Builder();
            if (request.bodyParams != null) {
                for (String key : request.bodyParams.keySet()) {
                    if (request.bodyParams.get(key) != null)
                        requestBody.add(key, request.bodyParams.get(key));
                }
            }
            requestBuilder.method(method, requestBody.build());
        } else {
            requestBuilder.method(method, null);
        }

        if (request.headers != null) {
            for (String key : request.headers.keySet()) {
                if (request.headers.get(key) != null)
                    requestBuilder.addHeader(key, request.headers.get(key));
            }
        }
        call(requestBuilder.build(), callback, authenticator);
    }

    private void call(final okhttp3.Request request, final NetworkCallback callback, final Authenticator authenticator) {
        client.newCall(request).enqueue(new Callback() {
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
                if (request.url().toString().contains(Constants.API_VALIDATE_COUPON) ||
                        request.url().toString().contains(Constants.ASSET_REFRESH_TOKEN) ||
                        request.url().toString().contains(Constants.GET_ASSET_TOKEN)) {
                    if (Connectivity.isConnectedMobile(context)) {
                        Log.e("mobile net", "enabled");
                        requestMobileNetApiCall(request, callback, authenticator);
                    } else {
                        callback.onFailure(null, e);
                    }
                } else if (!(e instanceof AuthLogoutException)) {
                    callback.onFailure(null, e);
                }
            }
        });
    }

    private void requestMobileNetApiCall(final okhttp3.Request request,
                                         final NetworkCallback callback,
                                         final Authenticator authenticator) {
        final ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkRequest.Builder builder = new NetworkRequest.Builder();
        builder.addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR);
        connectivityManager.registerNetworkCallback(builder.build(), new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(@NotNull Network network) {
                Log.e("Network",network.getSocketFactory().toString());
                final OkHttpClient client = new OkHttpClient.Builder()
                        .socketFactory(network.getSocketFactory())
                        .authenticator(authenticator)
                        .addInterceptor(new LoggingInterceptor())
                        .build();
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        if (!(e instanceof AuthLogoutException)) {
                            callback.onFailure(null, e);
                        }
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        if (!response.isSuccessful()) {
                            callback.onFailure(response, null);
                            return;
                        }
                        callback.onSuccess(response);
                    }
                });
                connectivityManager.unregisterNetworkCallback(this);
            }
        });
    }
}
