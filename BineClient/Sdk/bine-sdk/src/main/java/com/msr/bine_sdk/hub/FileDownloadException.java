package com.msr.bine_sdk.hub;

public class FileDownloadException extends Exception {
    int code;

    public FileDownloadException(String message, int code) {
        super(message);
        this.code = code;
    }
}
