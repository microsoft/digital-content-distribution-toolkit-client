package com.msr.bine_sdk.hub;

import com.msr.bine_sdk.cloud.models.Hub;

public interface HubConnectionCallback {
    void onConnectFailure(Hub response, Exception exception);
    void onConnect(Hub hub);
}
