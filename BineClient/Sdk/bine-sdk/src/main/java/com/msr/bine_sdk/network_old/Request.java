package com.msr.bine_sdk.network_old;

import com.msr.bine_sdk.cloud.models.Token;

import java.util.HashMap;

public class Request {
    String baseURl;
    String endpoint;
    HashMap<String, String> headers;
    HashMap<String, String> queryParams;
    HashMap<String, String> bodyParams;
    Token token;

    public Request(String baseURl, String endpoint, HashMap<String, String> headers, HashMap<String, String> queryParams, HashMap<String, String> bodyParams, Token token) {
        this.baseURl = baseURl;
        this.endpoint = endpoint;
        this.headers = headers;
        this.queryParams = queryParams;
        this.bodyParams = bodyParams;
        this.token = token;
    }
}
