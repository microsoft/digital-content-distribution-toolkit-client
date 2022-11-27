package com.msr.bine_sdk.download.custom;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

import com.google.gson.Gson;
import com.msr.bine_sdk.cloud.models.Content;
import com.msr.bine_sdk.download.DownloadNotifier;
import com.msr.bine_sdk.hub.model.DownloadStatus;

public class DownloadResultReceiver extends ResultReceiver {
    /**
     * Create a new ResultReceive to receive results.  Your
     * {@link #onReceiveResult} method will be called from the thread running
     * <var>handler</var> if given, or from an arbitrary thread if null.
     *
     * @param handler
     */
    private DownloadNotifier hubSocketCallback;
    static final int DOWNLOAD_SUCCESS = 1;
    static final int DOWNLOAD_FAILURE = 2;
    static final int DOWNLOAD_PROGRESS = 3;
    static final String BUNDLE_MESSAGE_KEY = "DownloadResultReceiverMessageKey";
    static final String FOLDER_ID_KEY = "FolderId";
    static final String FOLDER_STRING_KEY = "FolderJSON";
    static final String PROGRESS_KEY = "Progress";

    public DownloadResultReceiver(Handler handler, DownloadNotifier hubSocketCallback) {
        super(handler);
        this.hubSocketCallback = hubSocketCallback;
    }

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        switch (resultCode) {
            case DOWNLOAD_SUCCESS:
                Content content = new Gson().fromJson(resultData.getString(FOLDER_STRING_KEY), Content.class);
                hubSocketCallback.onDownloadSuccess(content,
                        0);
                break;
            case DOWNLOAD_FAILURE:
            default:
                hubSocketCallback.onDownloadFailure(resultData.getString(FOLDER_ID_KEY), resultData.getString(BUNDLE_MESSAGE_KEY));
                break;
            case DOWNLOAD_PROGRESS:
                hubSocketCallback.onDownloadProgress(resultData.getString(FOLDER_ID_KEY), DownloadStatus.IN_PROGRESS, resultData.getInt(PROGRESS_KEY));
                break;
        }
    }
}
