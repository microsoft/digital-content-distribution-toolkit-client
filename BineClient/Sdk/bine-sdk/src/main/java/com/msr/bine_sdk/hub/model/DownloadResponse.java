package com.msr.bine_sdk.hub.model;

import java.io.InputStream;

public class DownloadResponse {
    private InputStream stream;
    private long fileSize;

    public DownloadResponse(InputStream stream, long fileSize) {
        this.stream = stream;
        this.fileSize = fileSize;
    }

    public InputStream getStream() {
        return stream;
    }

    public long getFileSize() {
        return fileSize;
    }
}
