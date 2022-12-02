// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.msr.bine_sdk.download.exo

import android.app.Notification
import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.android.exoplayer2.offline.Download
import com.google.android.exoplayer2.offline.DownloadManager
import com.google.android.exoplayer2.util.NotificationUtil
import com.google.android.exoplayer2.util.Util
import com.google.gson.Gson
import com.msr.bine_sdk.R
import com.msr.bine_sdk.cloud.models.Content
import com.msr.bine_sdk.download.DownloadNotifier
import com.msr.bine_sdk.download.Downloader
import com.msr.bine_sdk.hub.model.DownloadStatus
import com.msr.bine_sdk.notifications.NotificationHelper
import java.lang.Exception
import java.util.*

class TerminalStateNotificationHelper(var context: Context,
                                      var nextNotificationId: Int,
                                      var notificationHelper: NotificationHelper): DownloadManager.Listener {


    private var downloadNotifier: DownloadNotifier? = null
    private var downloadUri: Uri? = null

    fun setDownloadNotifier(downloadNotifier: DownloadNotifier, downloadUri: Uri) {
        this.downloadNotifier = downloadNotifier
        this.downloadUri = downloadUri
    }

    override fun onDownloadChanged(
        downloadManager: DownloadManager,
        download: Download,
        finalException: Exception?
    ) {
        var notification: Notification? = null

        Log.e("TAG", "onDownloadChanged: " + download.state)
        if (download.state == Download.STATE_COMPLETED) {

            val folderString = Util.fromUtf8Bytes(download.request.data)
            Log.d("Download Complete", Date().toString())
            Log.d("Download Complete", Date(download.startTimeMs).toString())
            val folder: Content? = Gson().fromJson(folderString, Content::class.java)
            if (folder != null) {
                notification = getDownloadCompleteNotification(folder, download.request.uri,
                    (Date().time - download.startTimeMs).toString(), download.bytesDownloaded,
                    nextNotificationId)
            }
            Log.d("ExoDownloadService", ExoDownloadService.lastFinishTime.toString())
            val startTime = if (ExoDownloadService.lastFinishTime == 0L) download.startTimeMs else ExoDownloadService.lastFinishTime
            downloadNotifier?.onDownloadSuccess(folder, Date().time - startTime)
            ExoDownloadService.lastFinishTime = Date().time
        } else if (download.state == Download.STATE_FAILED) {
            val folderString = Util.fromUtf8Bytes(download.request.data)
            val folder: Content? = Gson().fromJson(folderString, Content::class.java)
            downloadNotifier?.onExoDownloadFailure(downloadUri.toString(), folder)

            /*if (folder != null) {
                notification = notificationHelper.getContentFailedNotifications(folder.title,
                        R.drawable.ic_download)
            }*/
            /*Telemetry.getInstance().sendFailureEvent("DownloadFailed", (if (folder != null) folder.ID else "") + "|Failure Reason - " + download.failureReason)*/
        } else {
            return
        }
        val finalNotification = notification
        Handler(Looper.getMainLooper()).postDelayed({ NotificationUtil.setNotification(context, nextNotificationId, finalNotification) }, 2000)
    }

    fun onDownloadProgress(folderId: String, progress: Int){
        downloadNotifier?.onDownloadProgress(folderId, DownloadStatus.IN_PROGRESS, progress)
    }

    fun onDownloadDeleted(folderId: String){
        downloadNotifier?.onDownloadDeleted(folderId)
    }

    fun onDownloadFailed(folderId: String, reason: String){
        downloadNotifier?.onDownloadFailure(folderId, reason)
    }

    fun onDownloadQueued(folderId: String){
        downloadNotifier?.onDownloadQueued(folderId, Downloader.TYPE.EXO)
    }

    fun onDownloadGenericEvent(folderId: String, event: String){
        downloadNotifier?.onDownloadGenericEvent(folderId,event, null)
    }

    private fun getDownloadCompleteNotification(folder: Content, uri: Uri, duration: String, size: Long, notificationId: Int): Notification {
        return notificationHelper.getDownloadCompleteNotification(
                folder.getThumbnailLandscapeImage(),
                folder.title,
                folder.contentId, uri.toString(),
            "", duration, size, notificationId)
    }
}