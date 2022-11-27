package com.msr.bine_sdk.hub.model;

public class FolderDownloadRequest {
    public String bulkFileName;
    public String mediaHouse;
    public String path;
    public String hubUrl;
    public String endpoint;

    public FolderDownloadRequest(String bulkFileName, String endpoint, String mediaHouse, String path, String hubUrl) {
        this.bulkFileName = bulkFileName;
        this.endpoint = endpoint;
        this.mediaHouse = mediaHouse;
        this.path = path;
        this.hubUrl = hubUrl;
    }
}
