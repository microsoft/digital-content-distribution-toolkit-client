package com.msr.bine_sdk.download;

import com.msr.bine_sdk.cloud.models.Content;
import com.msr.bine_sdk.hub.model.DownloadStatus;

public interface DownloadNotifier {
    void onDownloadSuccess(Content content, long time);
    void onDownloadQueued(String folderID, Downloader.TYPE type);
    void onLicenseDownloaded(String folderID);
    void onDownloadGenericEvent(String folderID, String event, String message);
    void onDownloadProgress(String folderID, DownloadStatus status, int progress);
    void onDownloadDeleted(String folderID);
    void onDownloadFailure(String folderID, String exception);
    void onExoDownloadFailure(String videoUrl, Content content);
}
