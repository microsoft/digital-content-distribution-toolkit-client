// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.msr.bine_sdk.download.custom

import android.content.Context
import android.content.Intent
import android.os.ResultReceiver
import android.util.Log
import androidx.core.app.JobIntentService
import com.google.gson.Gson
import com.msr.bine_sdk.BineConnect
import com.msr.bine_sdk.Constants
import com.msr.bine_sdk.R
import com.msr.bine_sdk.cloud.models.Content
import com.msr.bine_sdk.download.DownloadRequest
import com.msr.bine_sdk.hub.model.FolderDownloadRequest
import com.msr.bine_sdk.notifications.NotificationHelper
import com.msr.bine_sdk.saveFile

class CustomDownloadService : JobIntentService() {
    private var resultReceiver: ResultReceiver? = null
    private var downloadProgressNotifier: DownloadProgressNotifier? = null

    companion object {
        private const val JOB_ID = 1000
        const val RequestKey = "RequestKey"
        const val ReceiverKey = "ReceiverKey"
        private var downloadQueue = arrayListOf<Intent>()

        private var inProgress = false

        fun enqueueWork(context: Context,
                        work: Intent) {
            downloadQueue.add(work)
            enqueueWork(context, CustomDownloadService::class.java, JOB_ID, work)
        }

        fun cancelWork(context: Context,
                       work: Intent) {

        }
    }

    override fun onHandleWork(intent: Intent) {

        if (inProgress) {
            return
        }

        inProgress = true
        resultReceiver = intent.getParcelableExtra(ReceiverKey)
        val downloadRequest = intent.getParcelableExtra<DownloadRequest>(RequestKey)

        val folder = Gson().fromJson(downloadRequest?.extras, Content::class.java)
        /*if (downloadRequest != null) {
            folder.type=downloadRequest.type.ordinal
        };*/
        if (downloadRequest == null) {
            //return error
            downloadProgressNotifier?.sendMessage(DownloadResultReceiver.DOWNLOAD_FAILURE, null, "Invalid Request", 0)
        }

        if (folder.size().equals(1L)) {
            // if total size is not specified, it will break notification progress
            downloadProgressNotifier?.sendMessage(DownloadResultReceiver.DOWNLOAD_FAILURE, folder, "Total size was not passed", 0)
            return
        }

        if (downloadRequest?.downloadUri == null) {
            downloadProgressNotifier?.sendMessage(DownloadResultReceiver.DOWNLOAD_FAILURE, folder, "Hub url not passed", 0)
            return
        }

        downloadProgressNotifier = DownloadProgressNotifier(folder,
                this,
                downloadRequest, resultReceiver, object : NotificationHelper(this@CustomDownloadService) {
                override fun getMetaResourceBaseUrl(): String {
                   return ""
                }

                override fun getAppIcon(): Int {
                    return R.drawable.ic_play
                }

                override fun getHomeLaunchIntent(): Intent {
                    return Intent()
                }

            })

        val mpdFileName = "${downloadRequest.downloadUri}${folder.dashUrl}"
        downloadBulkFile(mpdFileName, folder.size().toString(), Constants.MPD_FILENAME, completion = {

            Log.e(CustomDownloadService::class.simpleName, "MPD Downloaded")

            val mpdFilePath = filesDir.absolutePath + Constants.MEDIA_OUTPUT_DIR + folder.contentId+"/"+Constants.MPD_FILENAME;
            Log.e("here MPD path",mpdFilePath);

            inProgress = false
            downloadProgressNotifier?.onComplete(folder,mpdFilePath)

            /*val audioFileName = "${downloadRequest.downloadUri}${folder.bineMetaData.audioFiles[0]}"
            downloadBulkFile(audioFileName, folder.ID, folder.bineMetaData.audioFiles[0], completion = {
                Log.e(CustomDownloadService::class.simpleName, "Audio Downloaded")
                val videoFileName = "${downloadRequest.downloadUri}${folder.bineMetaData.videoFiles[0]}"
                downloadBulkFile(videoFileName, folder.ID, folder.bineMetaData.videoFiles[0], completion = {
                    Log.e(CustomDownloadService::class.simpleName, "Video Downloaded")
                    var drmSessionManager = DRMManager.getInstance(this).buildOfflineDrmSessionManager(Uri.parse(mpdFilePath),
                            folder.ID, false, true)
                    inProgress = false
                    downloadProgressNotifier?.onComplete(folder,mpdFilePath)
                })
            })*/
        })

    }


    private fun downloadBulkFile(fileUrl: String,
                                 id: String,
                                 fileName: String,
                                 completion: () -> Unit) {
        val response = BineConnect.getInstance(this).downloadBulkfile(
                this,
            FolderDownloadRequest(
                null,
                "",
                "MSR",
                "",
                fileUrl
            )
        );

        val filePath = filesDir.absolutePath + Constants.MEDIA_OUTPUT_DIR + id;

        response.stream.saveFile(
                filePath,
                fileName,
                response.fileSize,
                downloadProgressNotifier,
                completion);
    }
}