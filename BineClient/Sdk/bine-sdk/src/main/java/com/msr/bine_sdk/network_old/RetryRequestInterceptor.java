package com.msr.bine_sdk.network_old;
import android.util.Log;

import com.msr.bine_sdk.Constants;
import com.msr.bine_sdk.eventbus.events.LogEvent;

import org.greenrobot.eventbus.EventBus;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class RetryRequestInterceptor implements Interceptor {
    int retryCount = 0;

    public RetryRequestInterceptor() {
    }

    @NotNull
    @Override
    public Response intercept(@NotNull Chain chain) throws IOException {

        Request request = chain.request();
        /*
         * try the request
         */
        Response response = sendRequest(chain,request);
        while (response == null && retryCount < Constants.RETRY_COUNT-1) {
            Log.e("intercept", "Request is not successful - " + retryCount);
            retryCount++;
            Log.e("type of request", "type :" + request.url()+"_"+response);
            //response.close();
            response = sendRequest(chain,request);
        }
        if(response == null && retryCount == Constants.RETRY_COUNT-1){
            response = chain.proceed(request);
        }
        return response;
    }



    private Response sendRequest(Chain chain, Request request){
       Response response = null;
        try {
            response = chain.proceed(request);
            if(!response.isSuccessful()) {
                EventBus.getDefault().post(new LogEvent("Retrying api", "Retry count: "+retryCount, response.code()+""));
            }
            return response;
        } catch (IOException e) {
            EventBus.getDefault().post(new LogEvent("Retrying api", "Retry count: "+retryCount, e.toString()));
            return null;
        }
    }
}
