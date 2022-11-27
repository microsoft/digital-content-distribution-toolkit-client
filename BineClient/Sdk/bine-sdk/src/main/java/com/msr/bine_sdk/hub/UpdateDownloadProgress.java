package com.msr.bine_sdk.hub;


import com.msr.bine_sdk.cloud.models.Content;

public interface UpdateDownloadProgress {
    void onProgress(int done);
    void onComplete(Content folder, String mpdPath);
}
