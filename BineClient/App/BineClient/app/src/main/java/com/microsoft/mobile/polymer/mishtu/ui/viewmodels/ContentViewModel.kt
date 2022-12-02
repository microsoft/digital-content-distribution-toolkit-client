// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.microsoft.mobile.polymer.mishtu.ui.viewmodels

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.os.StatFs
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.*
import com.microsoft.mobile.polymer.mishtu.offline.DownloadManager
import com.microsoft.mobile.polymer.mishtu.storage.SharedPreferenceStore
import com.microsoft.mobile.polymer.mishtu.storage.entities.*
import com.microsoft.mobile.polymer.mishtu.storage.repositories.ContentRepository
import com.microsoft.mobile.polymer.mishtu.storage.repositories.OrderRepository
import com.microsoft.mobile.polymer.mishtu.telemetry.AnalyticsLogger
import com.microsoft.mobile.polymer.mishtu.utils.BNConstants
import com.microsoft.mobile.polymer.mishtu.utils.BOConverter
import com.microsoft.mobile.polymer.mishtu.utils.Event
import com.microsoft.mobile.polymer.mishtu.utils.SubscriptionManager
import com.msr.bine_sdk.BineAPI
import com.msr.bine_sdk.download.exo.DRMManager
import com.msr.bine_sdk.hub.model.DownloadStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ContentViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val contentRepository: ContentRepository,
    private val orderRepository: OrderRepository,
    private val subscriptionManager: SubscriptionManager,
    val downloadManager: DownloadManager,

) : ViewModel() {

    var goToDownloads = MutableLiveData<Boolean>()
    var downloadProgress = MutableLiveData<Boolean>()
    var selectedEpisode = MutableLiveData<ContentDownload>()
    var selectedContetnProvider = MutableLiveData<String>()

    var moviesWatchListLiveData = MutableLiveData<List<ContentDownload>>()
    var allPaidContentByProviderId = MutableLiveData<List<Content>>()
    var allContentProvidersLiveData = contentRepository.allContentProvidersLiveData
    private  var listener: DownloadManagerListener? = null

    fun setBinePlayerActivityListener(listener: DownloadManagerListener){
        this.listener = listener
    }

    fun activeSubscriptionLiveData() =
        Transformations.switchMap(selectedContetnProvider){
            orderRepository.getActiveSubscriptionLiveData(it)
        }


    fun allActiveSubscriptionLiveData() = orderRepository.getAllActiveSubscriptionLiveData()
    suspend fun activeSubscription(contentProviderId: String) =
        orderRepository.getActiveSubscription(contentProviderId)

    val allPaidContentLiveData = contentRepository.getPaidContent()
    val allContentLiveData = contentRepository.getAllContent()
    var defualtContentProviderIdLiveData = MutableLiveData<String>()

    val downloadedContent: LiveData<List<ContentDownload>> =
        Transformations.switchMap(selectedEpisode) {
            if(it == null){
                Transformations.distinctUntilChanged(contentRepository.getDownloadedContent())
            }else{
                Transformations.distinctUntilChanged(contentRepository.getDownloadedEpisodes(it.name!!,
                    it.contentProviderId))
            }
        }

    fun downloadedEpisodes(
        seriesName: String,
        contentProviderId: String,
    ): LiveData<List<ContentDownload>> =
        Transformations.distinctUntilChanged(contentRepository.getDownloadedEpisodes(seriesName,
            contentProviderId))


    companion object {
        private const val CONNECTED_HUB_ID_SAVED_STATE_KEY = "hubId"
    }

    private val connectedHub: MutableLiveData<String> =
        savedStateHandle.getLiveData<String>(CONNECTED_HUB_ID_SAVED_STATE_KEY)


    fun setConnectedHubId(query: String) {
        savedStateHandle[CONNECTED_HUB_ID_SAVED_STATE_KEY] = query
    }

    private fun getQueuedAndOtherContent(hubId: String): LiveData<List<ContentDownload>> {
        return Transformations.distinctUntilChanged(contentRepository.getDownloadingContent(hubId))
    }

    var queuedAndOtherContent: LiveData<List<ContentDownload>> =
        Transformations.switchMap(connectedHub) {
            it?.let {
                getQueuedAndOtherContent(it)
            }
        }

    /*var inProgressContentContent: LiveData<List<ContentDownload>> = Transformations.switchMap(connectedHub) {
        it?.let {
            contentRepository.getInProgressContent(it)
        }
    }*/

    val contentLiveData = MutableLiveData<Event<Content>>()

    fun getContent(contentId: String) {
        CoroutineScope(Dispatchers.Default).launch {
            contentRepository.getContent(contentId)?.let {
                contentLiveData.postValue(Event(it))
            }
        }
    }

    fun getAllContentProviders() {
        contentRepository.getAllContentProviders()
    }

    fun getContinueMoviesWatchList(isMovie: Int) {
        val contents = contentRepository.getMovieWatchList(isMovie)
        moviesWatchListLiveData.postValue(contents)
    }

    fun getActiveOrderForContentProvider(): LiveData<List<Order>?> =
        Transformations.switchMap(selectedContetnProvider){
            orderRepository.getOrderLiveData(it)
        }

    fun getActiveOrderForContentProvider(contentProviderId: String): LiveData<List<Order>?> =
            orderRepository.getOrderLiveData(contentProviderId)

    fun getActiveSubscriptionForSubscriptionId(subscriptionId: String): LiveData<ActiveSubscription?> {
        return orderRepository.getActiveSubscriptionBySubscriptionIdLiveData(subscriptionId)
    }

    fun getContentProviderId(name: String) {
        CoroutineScope(Dispatchers.Default).launch {
            val cpId = contentRepository.getContentProviderId(name)
            cpId?.let {
                defualtContentProviderIdLiveData.postValue(it)
            }
        }
    }

    /*fun getPaidMovies(contents: List<Content>) {
        val paidMovies = arrayListOf<Content>()
        for (movie in contents) {
            if (!movie.free) paidMovies.add(movie)
        }
        paidMoviesLiveData.postValue(paidMovies)
    }

    fun getFreeMovies(contents: List<Content>) {
        val trending = arrayListOf<Content>()
        for (movie in contents) {
            if (movie.free) trending.add(movie)
        }
        freeMoviesLiveData.postValue(trending)
    }*/

    fun hasValidSubscriptionForContent(contentProviderId: String, contentId: String): Boolean {
        return subscriptionManager.hasValidSubscriptionForContent(contentProviderId,contentId)
    }

    fun hasActiveOrder(contentProviderId: String): Boolean {
        return !SharedPreferenceStore.getInstance()
            .get(SharedPreferenceStore.KEY_ORDER_ID + "_$contentProviderId").isNullOrEmpty()
    }

    fun beginDownload(contentId: String, bulkDownload: Boolean, context: Context) {
        downloadProgress.postValue(true)
        CoroutineScope(Dispatchers.Default).launch {
            contentRepository.getContent(contentId)?.let {
                if (!bulkDownload) {
                    beginDownload(context, it, true)
                } else {
                    if (it.name != null) {
                        val episodeList =
                            contentRepository.getAllEpisodeOfSeries(it.name, it.contentProviderId)

                        if (episodeList != null) {
                            for (episode in episodeList) {
                                if(subscriptionManager.hasValidSubscriptionForContent(episode.contentProviderId, episode.contentId)
                                        || episode.free) {
                                    if (episodeList.indexOf(episode) == episodeList.size - 1) {
                                        beginDownload(context,
                                            BOConverter.getContentFromContentDownload(episode),
                                            true)
                                    } else {
                                        beginDownload(context,
                                            BOConverter.getContentFromContentDownload(episode),
                                            false)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    fun hasAvailableStorage(fileSize: Long): Boolean {
        val stat = StatFs(Environment.getDataDirectory().path)
        val blockSize = stat.blockSizeLong
        val availableBlocks = stat.availableBlocksLong
        return availableBlocks * blockSize > fileSize + 50 * 1024 * 1024
    }

    fun hasAvailableStorage(content: ContentDownload, bulkDownload: Boolean): Boolean {
        if (!bulkDownload) {
            return hasAvailableStorage(content.getSize().toLong())
        }
        var seasonList =
            contentRepository.getAllfSeasonOfSeries(content.title, content.contentProviderId).value
        var totalSize: Long = 0;
        seasonList?.let {
            for (season in it) {
                val episodes = contentRepository.getAllEpisodesOfSeason(content.title,
                    season,
                    content.contentProviderId).value
                episodes?.let { it1 ->
                    for (episode in it1) {
                        totalSize += episode.getSize().toLong()
                        Log.d("totalSize", totalSize.toString())
                    }
                }
            }
        }
        return hasAvailableStorage(totalSize)
    }

    fun beginDownload(content: Content, context: Context) {
        downloadProgress.postValue(true)
        CoroutineScope(Dispatchers.Default).launch {
            beginDownload(context, content, true)
        }
    }

    private suspend fun beginDownload(context: Context, content: Content, hideProgress: Boolean) {
        SharedPreferenceStore.getInstance()
            .save(SharedPreferenceStore.KEY_DOWNLOAD_INSTRUCTION, "false")

        val response = BineAPI.Devices().getFolderPath(BNConstants.getDeviceIP(), content.contentId)
        if (hideProgress)
            downloadProgress.postValue(false)
        response.result?.let {
            AnalyticsLogger.getInstance()
                .logGenericLogs(com.microsoft.mobile.polymer.mishtu.telemetry.Event.DOWNLOAD_LOG,
                    "FolderPath",
                    "Success ${content.contentId}",
                    it.folderpath)

            val mpdFileUrl = "http://${BNConstants.getDeviceIP()}:5000${it.folderpath}"
            listener?.registerDownloadManagerListener(downloadManager)
            downloadManager.beginDownload(mpdFileUrl, content)
            //goToDownloads.postValue(true)
        } ?: response.error?.let {
            AnalyticsLogger.getInstance()
                .logGenericLogs(com.microsoft.mobile.polymer.mishtu.telemetry.Event.DOWNLOAD_LOG,
                    "FolderPath",
                    it.name,
                    response.details)

            Handler(Looper.getMainLooper()).post {
                Toast.makeText(
                    context,
                    "Unable to get details for ${it.name} - Error - ${response.code} - ${response.details}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    fun cancelDownload(context: Context, content: ContentDownload) {
        DRMManager.getInstance(context).getDownloadManager().removeDownload(content.contentId)
        contentRepository.updateDownload(content.contentId, DownloadStatus.NOT_DOWNLOADED, 0)
    }

    fun deleteDownload(context: Context, content: ContentDownload, deleteAllEpisodes: Boolean) {
        if (deleteAllEpisodes) {
            deleteAllEpisodes(context, content)
        } else {
            CoroutineScope(Dispatchers.Default).launch {
                deleteDownload(context, content)
            }
        }

    }

    private fun deleteAllEpisodes(context: Context, content: ContentDownload) {
        CoroutineScope(Dispatchers.Default).launch {
            content.name?.let {
                val list = contentRepository.getAllEpisodeOfSeries(it, content.contentProviderId)
                if (list != null) {
                    for (episode in list) {
                        if (!episode.downloadUrl.isNullOrEmpty()) {
                            deleteDownload(context, episode)
                        }
                    }
                }
            }
        }
    }

    fun deleteDownload(context: Context, content: ContentDownload) {
        contentRepository.getContent(content.contentId)?.let {
            val isFileDownloaded =
                DRMManager.getInstance(context).getDownloadTracker().isDownloaded(
                    Uri.parse(content.downloadUrl))
            if (isFileDownloaded) {
                DRMManager.getInstance(context).getDownloadManager()
                    .removeDownload(content.contentId)
                contentRepository.updateDownload(content.contentId,
                    DownloadStatus.NOT_DOWNLOADED,
                    0)
            }
        }
    }

     fun getAllPaidContentByProviderId(contentProviderId: String){
         CoroutineScope(Dispatchers.Default).launch {
             allPaidContentByProviderId.postValue( contentRepository.getPaidContentListByContentProviderId(contentProviderId))
         }
    }

}
interface DownloadManagerListener{
    fun registerDownloadManagerListener(downloadManager: DownloadManager)
}