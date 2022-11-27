package com.msr.bine_android.download

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.lifecycle.Observer
import com.google.gson.Gson
import com.msr.bine_android.data.DataRepository
import com.msr.bine_sdk.BineAPI
import com.msr.bine_sdk.cloud.models.Content
import com.msr.bine_sdk.download.DownloadNotifier
import com.msr.bine_sdk.download.DownloadRequest
import com.msr.bine_sdk.download.Downloader
import com.msr.bine_sdk.hub.model.DownloadStatus
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class DownloadManager @Inject constructor(
    private val context: Context, private val repository: DataRepository,
    private val downloader: Downloader,
    private val bineAPI: BineAPI) {

    private val observers = arrayListOf<Observer<DownloadStatus>>()

    fun observe(observers: Observer<DownloadStatus>) {
        this.observers.add(observers);
    }

    fun unObserver(observer: Observer<DownloadStatus>) {
        this.observers.remove(observer);
    }

    fun beginDownload(folder: Content,
                      type: Downloader.TYPE)
    {
        GlobalScope.launch {
            val response = BineAPI.Devices().getFolderPath(bineAPI.getHubIP(), folder.contentId)
            response.result?.let {
                var mpdFileUrl = "http://${bineAPI.getHubIP()}/${it.folderpath}"
                //mpdFileUrl = mpdFileUrl.replace("/mnt/hdd_1/mstore/QCAST.ipts", "/mstore")
                val uriValue = Uri.parse(mpdFileUrl)
                //repository.updateFolderUrl(folder.contentId, mpdFileUrl)
                /*Handler(Looper.getMainLooper()).post {
                    //Toast.makeText(context, "Starting - ${mpdFileUrl}", Toast.LENGTH_LONG).show()
                }*/

                val request = DownloadRequest(folder.contentId,
                        uriValue,
                        type,
                        Gson().toJson(folder))
                downloader.start(request,
                        downloadNotifier)
            } ?:
            response.error?.let {
                Handler(Looper.getMainLooper()).post {
                    //Toast.makeText(context, "Error getting folder path - ${response.code} - ${response.details}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private val downloadNotifier: DownloadNotifier = object : DownloadNotifier {

        override fun onDownloadSuccess(content: Content, time: Long) {
            //Notify UI
            for (observer in observers) {
                observer.onChanged(DownloadStatus.DOWNLOADED)
            }
            repository.updateDownload(content.contentId, DownloadStatus.DOWNLOADED, 100)
            onDownloadComplete(content.contentId)
        }

        override fun onDownloadQueued(folderID: String, type: Downloader.TYPE) {
            repository.updateDownload(folderID, DownloadStatus.QUEUED, 0, type)
        }

        override fun onLicenseDownloaded(folderID: String?) {
        }

        override fun onDownloadGenericEvent(folderID: String?, event: String?, message: String?) {
        }

        override fun onDownloadProgress(folderID: String, status: DownloadStatus, value: Int) {
            repository.updateDownload(folderID, status, value)
        }

        override fun onDownloadDeleted(folderID: String) {
            repository.updateDownload(folderID, DownloadStatus.NOT_DOWNLOADED, 0)
        }

        override fun onDownloadFailure(folderID: String, exception: String) {
            //Notify
            for (observer in observers) {
                observer.onChanged(DownloadStatus.FAILED)
            }
            repository.updateDownload(folderID, DownloadStatus.FAILED, 0)
        }

        override fun onExoDownloadFailure(folderID: String?, content: Content?) {
        }
    }

    private fun onDownloadComplete(folderId: String?) {
        repository.getUserId()?.let {
            /*GlobalScope.async {
                val response = bineAPI.sendDownloadStats(it, repository.getHubId())
                if (response.result) {
                    Telemetry.getInstance().sendUserStats(folderId);
                } else
                    Telemetry.getInstance().sendFailureEvent("UserStatsAPIFailed", response.details);
            }*/
        }
    }
}