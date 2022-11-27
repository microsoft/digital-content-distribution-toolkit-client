package com.msr.bine_sdk.cloud.models;

public enum Source {
    HUB(1),
    CLOUD(2);

    private int mValue;

    Source(int value) {
        mValue = value;
    }

    public int getValue() {
        return mValue;
    }
}
