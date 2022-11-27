package com.msr.bine_sdk.hub.model;

import com.msr.bine_sdk.cloud.models.Hub;

public class ConnectedHub {
    public String SSID;
    public String id;
    public String ip;

    public ConnectedHub(Hub hub) {
        this.id = hub.id;
        this.ip = hub.ip;
    }
}
