package com.msr.bine_sdk.network_old;

import android.util.Log;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Response;

class LoggingInterceptor implements Interceptor {
    private static final String TAG = "NetworkService";

    @Override public Response intercept(Interceptor.Chain chain) throws IOException {
        okhttp3.Request request = chain.request();

        long t1 = System.nanoTime();
        Log.d(TAG,String.format("Sending request %s on %s%n%s",
                request.url(), chain.connection(), request.toString()));

        Response response = chain.proceed(request);

        long t2 = System.nanoTime();

        Log.d(TAG, String.format("Received response for %s in %.1fms%n%s",
                response.request().url(), (t2 - t1) / 1e6d, response.body().toString()));
        Log.d(TAG, String.format("Received code - %d",
                response.code()));
        return response;
    }
}