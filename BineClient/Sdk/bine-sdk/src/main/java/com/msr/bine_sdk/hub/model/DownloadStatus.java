package com.msr.bine_sdk.hub.model;

public enum DownloadStatus {
    NOT_DOWNLOADED(0),
    IN_PROGRESS(1),
    QUEUED(2),
    DOWNLOADED(3),
    FAILED(4);

    private int mValue;

    DownloadStatus(int value) {
        mValue = value;
    }

    public int getValue() {
        return mValue;
    }
}
