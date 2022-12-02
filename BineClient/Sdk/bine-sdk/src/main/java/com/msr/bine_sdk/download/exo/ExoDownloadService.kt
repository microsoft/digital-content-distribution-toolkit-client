// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.msr.bine_sdk.download.exo

import android.app.Notification
import android.content.Intent
import com.google.android.exoplayer2.offline.Download
import com.google.android.exoplayer2.offline.DownloadManager
import com.google.android.exoplayer2.offline.DownloadService
import com.google.android.exoplayer2.scheduler.PlatformScheduler
import com.google.android.exoplayer2.scheduler.Scheduler
import com.google.android.exoplayer2.util.Util
import com.google.gson.Gson
import com.msr.bine_sdk.BuildConfig
import com.msr.bine_sdk.Constants
import com.msr.bine_sdk.R
import com.msr.bine_sdk.cloud.models.Content
import kotlin.math.roundToInt

class ExoDownloadService/* channelDescriptionResourceId= */ : DownloadService(
    DOWNLOAD_STATUS_NOTIFICATION_ID,
    DEFAULT_FOREGROUND_NOTIFICATION_UPDATE_INTERVAL,
    Constants.ID_PROGRESS_CHANNEL,
    R.string.exo_download_notification_channel_name,  /* channelDescriptionResourceId= */
    0) {

    companion object{
        var lastFinishTime = 0L
        const val DOWNLOAD_STATUS_NOTIFICATION_ID = 1
        private const val JOB_ID = 1
        const val CANCEL_DOWNLOAD = "CANCEL_DOWNLOAD"
    }

    init {
        lastFinishTime = 0
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null && intent.action != null) if (intent.action == CANCEL_DOWNLOAD) {
            val folerID = intent.getStringExtra(Constants.FOLDER_ID)

            if (BuildConfig.DEBUG && folerID == null) {
                error("Assertion failed")
            }
            sendRemoveDownload(
                    baseContext,
                    ExoDownloadService::class.java,
                    folerID!!,  /* foreground= */
                    false)
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun getDownloadManager(): DownloadManager {
        val downloadManager = DRMManager.getInstance(this).getDownloadManager()
        downloadManager.addListener(DRMManager.getInstance(this).getDownloadServiceListener())
        return downloadManager
    }

    override fun getForegroundNotification(downloads: MutableList<Download>): Notification {
        val downloadManager = DRMManager.getInstance(this)

        for (download in downloads) {
            val folderString = Util.fromUtf8Bytes(download.request.data)
            val folder: Content = Gson().fromJson(folderString, Content::class.java)
            when (download.state) {
                Download.STATE_DOWNLOADING -> {
                    downloadManager.getDownloadServiceListener().onDownloadProgress(download.request.id, download.percentDownloaded.roundToInt())
                    return showNotification(download.percentDownloaded.roundToInt(), folder)
                }
                Download.STATE_REMOVING -> {
                    downloadManager.getDownloadServiceListener().onDownloadDeleted(download.request.id)
                }
                Download.STATE_FAILED -> {
                    downloadManager.getDownloadServiceListener().onDownloadFailed(download.request.id, download.failureReason.toString())
                }
                Download.STATE_COMPLETED -> {
                    downloadManager.getDownloadServiceListener().onDownloadGenericEvent(download.request.id, "STATE_RESTARTING")
                }
                Download.STATE_QUEUED -> {
                    downloadManager.getDownloadServiceListener().onDownloadQueued(download.request.id)
                }
                Download.STATE_RESTARTING -> {
                    downloadManager.getDownloadServiceListener().onDownloadGenericEvent(download.request.id, "STATE_RESTARTING")
                }
                Download.STATE_STOPPED -> {
                    downloadManager.getDownloadServiceListener().onDownloadFailed(download.request.id, download.stopReason.toString())
                }
            }
        }

        return downloadManager.getDownloadNotificationHelper()
            .buildProgressNotification(
                applicationContext,
                R.drawable.ic_download,  /* contentIntent= */
                null,  /*
                         message= */
                null,
                downloads)
    }

    override fun getScheduler(): Scheduler {
        return PlatformScheduler(this, JOB_ID)
    }

    private fun showNotification(current: Int, folder: Content): Notification {
        return DRMManager.getInstance(this).getNotificationHelper()
            .getProgress(current,
                folder.getThumbnailLandscapeImage(),
                folder.title, folder.contentId)
    }
}