package com.msr.bine_sdk;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.RequestBody;

public class MockNetworkService {

    /*public void get(String baseUrl, String url, NetworkCallback cb) {
        this.callback = cb;
        try {
            HttpUrl httpUrl = new MockServer().getServer().url(baseUrl+url);
            call("GET", null, httpUrl);
        }
        catch (Exception e) {
            System.out.println("Exception in MockNetworkService: " + e.getMessage());
        }
    }

    public void post(String url, RequestBody requestBody, NetworkCallback cb) {
        callback = cb;

        try {
            HttpUrl httpUrl = new MockServer().getServer().url(url);
            call("POST", requestBody, httpUrl);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/

}
