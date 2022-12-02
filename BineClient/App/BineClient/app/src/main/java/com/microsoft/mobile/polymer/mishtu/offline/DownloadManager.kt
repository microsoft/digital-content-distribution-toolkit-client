// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.microsoft.mobile.polymer.mishtu.offline

import android.net.Uri
import androidx.lifecycle.Observer
import com.google.gson.Gson
import com.microsoft.mobile.polymer.mishtu.storage.SharedPreferenceStore
import com.microsoft.mobile.polymer.mishtu.storage.entities.Content
import com.microsoft.mobile.polymer.mishtu.storage.repositories.ContentRepository
import com.microsoft.mobile.polymer.mishtu.storage.repositories.NotificationBadgeHelper
import com.microsoft.mobile.polymer.mishtu.telemetry.AnalyticsLogger
import com.microsoft.mobile.polymer.mishtu.telemetry.Event
import com.microsoft.mobile.polymer.mishtu.ui.viewmodels.DeviceViewModel
import com.microsoft.mobile.polymer.mishtu.utils.BOConverter
import com.microsoft.mobile.polymer.mishtu.utils.DeviceConnect
import com.msr.bine_sdk.download.DownloadNotifier
import com.msr.bine_sdk.download.DownloadRequest
import com.msr.bine_sdk.download.Downloader
import com.msr.bine_sdk.hub.model.DownloadStatus
import java.util.*
import javax.inject.Inject

class DownloadManager @Inject constructor(private val contentRepository: ContentRepository,
                                          private val downloader: Downloader,
                                          private val deviceConnect: DeviceConnect,
                                          private val notificationBadgeHelper: NotificationBadgeHelper) {

    private val observers = arrayListOf<Observer<DownloadStatus>>()
    private var listener: DeviceViewModelListener? = null

    fun observe(observers: Observer<DownloadStatus>) {
        this.observers.add(observers)
    }

    fun unObserver(observer: Observer<DownloadStatus>) {
        this.observers.remove(observer)
    }

    fun setListener(listener: DeviceViewModelListener) {
        this.listener = listener
    }

    fun beginDownload(
        videoUrl: String,
        content: Content
    )
    {
        val uriValue = Uri.parse(videoUrl)

        contentRepository.insertDownload(content.contentId, videoUrl, DownloadStatus.QUEUED, 0)
        val request = DownloadRequest(content.contentId,
            uriValue,
            Downloader.TYPE.EXO,
            Gson().toJson(BOConverter.getBNContentFromContent(content)))
        downloader.start(request, downloadNotifier)
    }

    fun clearDownloads(contentIds: List<String>) {
        downloader.clearDownloads(contentIds)
    }

    private val downloadNotifier: DownloadNotifier = object : DownloadNotifier {
        override fun onDownloadSuccess(content: com.msr.bine_sdk.cloud.models.Content, timeTaken: Long) {
            //Notify UI
            for (observer in observers) {
                observer.onChanged(DownloadStatus.DOWNLOADED)
            }
            var downloadCount = getDownloadCount()
            contentRepository.updateDownload(content.contentId, DownloadStatus.DOWNLOADED, 100)
            val connectedHub = contentRepository.connectedHubDevice()
            AnalyticsLogger.getInstance().logContentDownload(timeTaken,
                content,
                connectedHub?.id ?: "",
                deviceConnect.getWifiInfoFrequency() ?: "",
                deviceConnect.getWifiInfoFrequencyValue(), downloadCount)
            notificationBadgeHelper.markNotification(NotificationBadgeHelper.BadgeType.NEW_DOWNLOADS)
            listener?.recordDownloadCompleteEvent(content.contentId)
        }

        override fun onDownloadQueued(folderID: String, type: Downloader.TYPE) {
            contentRepository.updateDownload(folderID, DownloadStatus.QUEUED, 0)
            AnalyticsLogger.getInstance().logGenericLogs(Event.DOWNLOAD_LOG, "DownloadQueued", "Success", folderID)
        }

        override fun onDownloadProgress(folderID: String, status: DownloadStatus, value: Int) {
            contentRepository.updateDownload(folderID, status, value)
        }

        override fun onDownloadDeleted(folderID: String) {
            contentRepository.delete(folderID)
        }

        override fun onDownloadFailure(folderID: String, exception: String) {
            //Notify
            for (observer in observers) {
                observer.onChanged(DownloadStatus.FAILED)
            }
            contentRepository.delete(folderID)
            AnalyticsLogger.getInstance().logGenericLogs(Event.DOWNLOAD_LOG, "DownloadFailure", "Failed $folderID", exception)
        }

        override fun onLicenseDownloaded(folderID: String?) {
            AnalyticsLogger.getInstance().logGenericLogs(Event.DOWNLOAD_LOG, "LicenseFetch", "Success", folderID)
        }

        override fun onDownloadGenericEvent(folderID: String?, event: String?, message: String?) {
            AnalyticsLogger.getInstance().logGenericLogs(Event.DOWNLOAD_LOG, "DownloadEvent", "$event $folderID", message)
        }

        override fun onExoDownloadFailure(videoUrl: String, content: com.msr.bine_sdk.cloud.models.Content) {
            if (listener != null) {
                AnalyticsLogger.getInstance().logGenericLogs(Event.DOWNLOAD_LOG, "ExoDownloadFailure", "Failed, retrying calling getDeviceId", content.contentId)
                listener?.onExoDownloadFailureOccurred(videoUrl, BOConverter.getContentBOFromBNContent(content))
            }else{
                //mark as failed if again get the same error after retrying (listener will be null in that case)
                for (observer in observers) {
                    observer.onChanged(DownloadStatus.FAILED)
                }
                contentRepository.delete(content.contentId)
                AnalyticsLogger.getInstance().logGenericLogs(Event.DOWNLOAD_LOG, "ExoDownloadFailure", "Failed, after retrying again", content.contentId)

            }
        }
    }

    private fun getDownloadCount(): String {
        var downloadCount: String?
                if(!shouldResetDownloadCount()) {
                    downloadCount = SharedPreferenceStore.getInstance().get(SharedPreferenceStore.DOWNLOAD_COUNT)
                    downloadCount = if (downloadCount.isNullOrEmpty() || downloadCount.toInt() == 0) {
                        "1"
                    } else {
                        (downloadCount.toInt() + 1).toString()
                    }
                }else{
                    downloadCount = "0"
                }
        SharedPreferenceStore.getInstance().save(SharedPreferenceStore.DOWNLOAD_COUNT,downloadCount)
        return downloadCount
    }

    //reset downloadCount in every 10 days or logout
    private fun shouldResetDownloadCount(): Boolean {
        var res = false
        val downloadCountStartDate = SharedPreferenceStore.getInstance().get(SharedPreferenceStore.DOWNLOAD_COUNT_START_DATE)?.toLong()
        downloadCountStartDate?.let {
            if(Date().time - it > 10*24*60*60*1000){
                res = true
            }
        }
        return res
    }

    interface DeviceViewModelListener {
        fun onExoDownloadFailureOccurred(videoUrl: String, content: Content)
        fun recordDownloadCompleteEvent(contentId: String)
    }
}