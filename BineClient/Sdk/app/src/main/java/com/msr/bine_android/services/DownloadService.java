package com.msr.bine_android.services;

import android.content.Context;
import android.content.Intent;
import android.os.ResultReceiver;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;

import com.msr.bine_android.data.DataRepository;

public class DownloadService extends JobIntentService {
    static final int JOB_ID = 1000;
    private ResultReceiver resultReceiver;
    public static final String DownloadServiceReceiverKey = "DSReceiverKey";
    public static final String BulkFileNamesListKey = "DSBulkFilesKey";
    public static final String FolderPathKey = "DSFolderPathKey";
    public static final String HubIdKey = "DSHubIdKey";
    public static final String TotalSizeKey = "DSTotalSizeKey";
    public static final String MediaHouseKey = "MediaHouseKey";
    public static final String FolderIdKey = "FolderIDKey";
    public static final String HubURL = "HubURL";
    private static final int notificationFrequencyMs = 500;

    private DataRepository dataRepository;

    public static void enqueueWork(Context context,
                                   Intent work) {
        enqueueWork(context, DownloadService.class, JOB_ID, work);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        /*dataRepository = DataRepository.Companion.getInstance(
                AppDatabase.Companion.getDatabase(getApplicationContext()).folderDao(),
                new SharedPreferenceStore(getApplication()));

        resultReceiver = intent.getParcelableExtra(DownloadServiceReceiverKey);
        final int scale = 100;

        NotificationHelper.getInstance(this).startProgress(Constants.ID_PROGRESS_CHANNEL, scale);
        final String folderId = intent.getStringExtra(FolderIdKey);
        ArrayList<String> bulkFileNames = intent.getStringArrayListExtra(BulkFileNamesListKey);
        final long totalSize = intent.getLongExtra(TotalSizeKey, -1);
        String mediaHouse = intent.getStringExtra(MediaHouseKey);
        String hubURL = intent.getStringExtra(HubURL);
        String folderPath = intent.getStringExtra(FolderPathKey);
        String hubID = intent.getStringExtra(HubIdKey);

        try {
            if(totalSize == -1) {
                // if total size is not specified, it will break notification progress
                sendMessage(DownloadResultReceiver.DOWNLOAD_FAILURE, folderId, "Total size was not passed");
                return;
            }

            if(bulkFileNames == null) {
                sendMessage(DownloadResultReceiver.DOWNLOAD_FAILURE, folderId,"Null bulk file names");
                return;
            }

            if(hubURL == null) {
                sendMessage(DownloadResultReceiver.DOWNLOAD_FAILURE, folderId,"Hub url not passed");
                return;
            }

            Date startTime = new Date();
            UpdateDownloadProgress updateProgress = new UpdateDownloadProgress() {
                long previousUpdatedTime = 0;
                long totalDownloadedBytes = 0;

                @Override
                public void onProgress(int done) {
                    totalDownloadedBytes += done;
                    // update notification every 500 ms
                    if(System.currentTimeMillis() - previousUpdatedTime > notificationFrequencyMs) {
                        previousUpdatedTime = System.currentTimeMillis();
                        int downloaded = (int)(((float)totalDownloadedBytes / (float)totalSize) * scale);
                        NotificationHelper.getInstance(DownloadService.this).showProgress(Constants.ID_PROGRESS_CHANNEL, scale, downloaded);
                        sendMessage(DownloadResultReceiver.DOWNLOAD_PROGRESS, folderId,""+downloaded);
                    }
                }
            };

            File root  = getApplication().getFilesDir();
            for(String bulkFileName: bulkFileNames) {
                Log.d("Mishtu", "Download Service Started - " + bulkFileName);
                Log.d("Mishtu", "Download Service Started - " + hubURL);

                *//*DownloadResponse response = BineConnect.getInstance(getApplicationContext()).downloadBulkfile(
                        getApplicationContext(),
                        new FolderDownloadRequest(bulkFileName,
                                com.msr.bine_sdk.Constants.HUB_DOWNLOAD_FILE,
                                mediaHouse,
                                folderPath,
                                hubURL));

                Log.d("Mishtu", "Download Response - Size " + response.getFileSize());

                String filePath = root.getAbsolutePath() + com.msr.bine_android.utils.Constants.MEDIA_OUTPUT_DIR + folderId;

                IOStreamUtilKt.saveFile(response.getStream(),
                        filePath,
                        com.msr.bine_android.utils.Constants.VIDEO_FILENAME,
                        response.getFileSize(),
                        updateProgress);
            }
            NotificationHelper.getInstance(this).stopProgress(Constants.ID_PROGRESS_CHANNEL, false);
            long timeDifference = new Date().getTime() - startTime.getTime();

           /* HashMap<String, String> params = new HashMap<>();
            params.put("Duration", String.valueOf(timeDifference));
            params.put("Asset_Id", folderId);
            params.put("Hub_Id", hubID);
            params.put("Count", "1");*//*

            }
            Telemetry.getInstance().sendContentDownload(startTime.getTime(), folderId, totalSize);
            sendMessage(DownloadResultReceiver.DOWNLOAD_SUCCESS, folderId, "");
        }
        catch (Exception e) {
            e.printStackTrace();
            NotificationHelper.getInstance(this).stopProgress(Constants.ID_PROGRESS_CHANNEL, true);
            sendMessage(DownloadResultReceiver.DOWNLOAD_FAILURE, folderId, "Download Failed");
        }*/
    }

    private void sendMessage(int code, String folderId, String message) {
       /* Log.d("Mishtu", "Download Progress - " + message);
        switch (code) {
            case DownloadResultReceiver.DOWNLOAD_SUCCESS:
                dataRepository.updateDownload(folderId, DownloadStatus.DOWNLOADED, 100);
                break;
            case DownloadResultReceiver.DOWNLOAD_FAILURE:
                dataRepository.updateDownload(folderId, DownloadStatus.FAILED, 0);
                break;
            case DownloadResultReceiver.DOWNLOAD_PROGRESS:
                dataRepository.updateDownload(folderId, DownloadStatus.IN_PROGRESS, Integer.valueOf(message));
                break;

        }
        Bundle bundle = new Bundle();
        bundle.putString(DownloadResultReceiver.BUNDLE_MESSAGE_KEY, message);
        bundle.putString(DownloadResultReceiver.FOLDERID_KEY, folderId);
        resultReceiver.send(code, bundle);*/
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
