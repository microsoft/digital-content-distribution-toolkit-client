package com.msr.bine_sdk.download.custom

import android.content.Context
import android.os.Bundle
import android.os.ResultReceiver
import com.google.gson.Gson
import com.msr.bine_sdk.R
import com.msr.bine_sdk.cloud.models.Content
import com.msr.bine_sdk.download.DownloadRequest
import com.msr.bine_sdk.hub.UpdateDownloadProgress
import com.msr.bine_sdk.notifications.NotificationHelper

class DownloadProgressNotifier(private val folder: Content,
                               private val context: Context,
                               private val downloadRequest: DownloadRequest,
                               private val resultReceiver: ResultReceiver?,
                               private val notificationHelper: NotificationHelper) : UpdateDownloadProgress {
    private var previousUpdatedTime: Long = 0
    private var totalDownloadedBytes: Long = 0

    override fun onProgress(done: Int) {
        totalDownloadedBytes += done.toLong()

        if (System.currentTimeMillis() - previousUpdatedTime > notificationFrequencyMs) {
            previousUpdatedTime = System.currentTimeMillis()
            val downloaded = totalDownloadedBytes.toFloat() / folder.size() * 100
            sendMessage(DownloadResultReceiver.DOWNLOAD_PROGRESS, folder, "" , downloaded.toInt())

            showProgressNotification(downloaded.toInt(), folder)
        }
    }

    override fun onComplete(folder: Content, mpdFile:String) {
        sendMessage(DownloadResultReceiver.DOWNLOAD_SUCCESS, folder, "" ,100)
        showDownloadCompleteNotification(folder, mpdFile)
    }

    private fun showDownloadCompleteNotification(folder: Content, mpdFile: String) {
        val mpdFileName = "${downloadRequest.downloadUri}${folder.dashUrl}"

        notificationHelper.showDownloadComplete(
                folder.getThumbnailLandscapeImage(),
                folder.title,
                folder.contentId, mpdFileName, android.R.drawable.sym_def_app_icon,mpdFile, folder.size().toLong())
    }

    private fun showProgressNotification(current: Int, folder: Content) {
        notificationHelper.showProgress(current,
                folder.getThumbnailLandscapeImage(),
                folder.title, folder.contentId)
    }

    private fun showDownloadFailedNotification(folder: Content?) {
        notificationHelper.showDownloadFailed(
                folder?.title,
                R.drawable.ic_download)
    }

    fun sendMessage(code: Int, folder: Content?, message: String, progress: Int) {

        if(code ==  DownloadResultReceiver.DOWNLOAD_FAILURE) {
            showDownloadFailedNotification(folder)
        }
        val bundle = Bundle()
        bundle.putString(DownloadResultReceiver.BUNDLE_MESSAGE_KEY, message)
        bundle.putString(DownloadResultReceiver.FOLDER_STRING_KEY, Gson().toJson(folder))
        bundle.putInt(DownloadResultReceiver.PROGRESS_KEY, progress)
        resultReceiver!!.send(code, bundle)
    }

    companion object {
        private const val notificationFrequencyMs = 1000
    }
}