// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.microsoft.mobile.polymer.mishtu.ui.viewmodels

import android.util.Log
import androidx.lifecycle.*
import com.microsoft.mobile.polymer.mishtu.offline.DownloadManager

import com.microsoft.mobile.polymer.mishtu.storage.SharedPreferenceStore
import com.microsoft.mobile.polymer.mishtu.storage.entities.ContentDownload
import com.microsoft.mobile.polymer.mishtu.storage.entities.Content
import com.microsoft.mobile.polymer.mishtu.storage.repositories.ContentRepository
import com.microsoft.mobile.polymer.mishtu.storage.repositories.IncentivesRepository
import com.microsoft.mobile.polymer.mishtu.telemetry.AnalyticsLogger
import com.microsoft.mobile.polymer.mishtu.utils.BNConstants
import com.microsoft.mobile.polymer.mishtu.utils.DeviceConnect
import com.microsoft.mobile.polymer.mishtu.utils.Event

import com.msr.bine_sdk.BineAPI
import com.msr.bine_sdk.cloud.models.Hub
import com.msr.bine_sdk.hub.model.ConnectedHub
import com.msr.bine_sdk.models.Error

import javax.inject.Inject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*

@HiltViewModel
class DeviceViewModel @Inject constructor(private val bineConnect: DeviceConnect,
                                          private val contentRepository: ContentRepository,
                                          private val downloadManager: DownloadManager,
                                          private val incentivesRepository: IncentivesRepository): ViewModel(),
    DownloadManager.DeviceViewModelListener{

    //Device is found in detected WiFi connections, but not connected
    var deviceDetected = MutableLiveData<Boolean>()
    //Device is connected
    var deviceConnected = MutableLiveData<ConnectedHub?>()
    var deviceConnectionActive = MutableLiveData<Event<Boolean?>>()
    var error = MutableLiveData<Error>()
    val deviceContent = MutableLiveData<List<Content>>()
    val loading = MutableLiveData<Boolean>()
    val retailerName = MutableLiveData<String>()
    val wifiFrequencyBand = MutableLiveData<String>()
    var selectedMovieContetnProvider = MutableLiveData<String>()
    var selectedSeriesContetnProvider = MutableLiveData<String>()

    var deviceConnectError = MutableLiveData<String>()

    private var hubDataFetchRunning = false
    private var deviceIdFetchRunning = false

    private fun allContentLiveData(limit: Int, isMovie: Int, contentProviderId: String): LiveData<List<ContentDownload>> =
        Transformations.distinctUntilChanged(contentRepository.getAllMovies(limit,isMovie, contentProviderId))
    fun freeContentLiveData(limit: Int, isMovie: Int, contentProviderId: String): LiveData<List<ContentDownload>> =
        Transformations.distinctUntilChanged(contentRepository.getFreeContentDownloads(limit, isMovie, contentProviderId))
    fun paidContentLiveData(limit: Int, isMovie: Int, contentProviderId:String): LiveData<List<ContentDownload>> =
        Transformations.distinctUntilChanged(contentRepository.getPaidContentDownloads(limit, isMovie, contentProviderId))

    fun getAllEpisodesOfSeason(seriesName: String, seasonName: String, contentProviderId: String): LiveData<List<ContentDownload>> =
        Transformations.distinctUntilChanged(contentRepository.getAllEpisodesOfSeason(seriesName, seasonName, contentProviderId))

    fun getAllOfSeasonOfSeries(seriesName: String, contentProviderId: String): LiveData<List<String>> =
        Transformations.distinctUntilChanged(contentRepository.getAllfSeasonOfSeries(seriesName, contentProviderId))

    private fun getHubContentLiveData(hubId: String, limit: Int,isMovie: Int, contentProviderId: String): LiveData<List<ContentDownload>> {
        return Transformations.distinctUntilChanged(contentRepository.getAllMoviesForHubId(hubId, limit, isMovie, contentProviderId))
    }

    fun getHubFreeContentLiveData(hubId:String, limit: Int, isMovie: Int, contentProviderId: String): LiveData<List<ContentDownload>> {
        return Transformations.distinctUntilChanged(contentRepository.getFreeMoviesForHubId(hubId, limit, isMovie, contentProviderId))
    }

    fun getHubPaidContentLiveData(hubId:String, limit: Int, isMovie: Int, contentProviderId: String): LiveData<List<ContentDownload>> {
        return Transformations.distinctUntilChanged(contentRepository.getPaidMoviesForHubId(hubId, limit, isMovie, contentProviderId))
    }

    fun contentMovieLiveData(limit: Int): LiveData<List<ContentDownload>> =
        Transformations.switchMap(selectedMovieContetnProvider) { cp ->
            Transformations.switchMap(deviceConnected) {
                if (it == null || it.id.isEmpty()) {
                    allContentLiveData(limit, 1, cp)
                } else {
                    getHubContentLiveData(it.id, limit, 1, cp)
                }
            }
        }

    fun contentSeriesLiveData(limit: Int): LiveData<List<ContentDownload>> =
        Transformations.switchMap(selectedSeriesContetnProvider) { cp ->
            Transformations.switchMap(deviceConnected) {
                if (it == null || it.id.isEmpty()) {
                    allContentLiveData(limit, 0, cp)
                } else {
                    getHubContentLiveData(it.id, limit, 0, cp)
                }
            }
        }

    fun freeContentMovieLiveDataDeviceAware(limit: Int): LiveData<List<ContentDownload>> =
        Transformations.switchMap(selectedMovieContetnProvider) { cp ->
            Transformations.switchMap(deviceConnected) {
                if (it == null || it.id.isEmpty()) {
                    freeContentLiveData(limit, 1, cp)
                } else {
                    getHubFreeContentLiveData(it.id, limit, 1, cp)
                }
            }
        }
    
    fun freeContentSeriesLiveDataDeviceAware(limit: Int): LiveData<List<ContentDownload>> =
        Transformations.switchMap(selectedSeriesContetnProvider) { cp ->
            Transformations.switchMap(deviceConnected) {
                if (it == null || it.id.isEmpty()) {
                    freeContentLiveData(limit, 0, cp)
                } else {
                    getHubFreeContentLiveData(it.id, limit, 0, cp)
                }
            }
        }

    fun paidContentMovieLiveDataDeviceAware(limit: Int): LiveData<List<ContentDownload>> =
        Transformations.switchMap(selectedMovieContetnProvider) { cp ->
            Transformations.switchMap(deviceConnected) {
                if (it == null || it.id.isEmpty()) {
                    paidContentLiveData(limit, 1, cp)
                } else {
                    getHubPaidContentLiveData(it.id, limit, 1, cp)
                }
            }
        }
    
    fun paidContentSeriesLiveDataDeviceAware(limit: Int): LiveData<List<ContentDownload>> =
        Transformations.switchMap(selectedSeriesContetnProvider) { cp ->
            Transformations.switchMap(deviceConnected) {
                if (it == null || it.id.isEmpty()) {
                    paidContentLiveData(limit, 0, cp)
                } else {
                    getHubPaidContentLiveData(it.id, limit, 0, cp)
                }
            }
        }

    fun loadGlobalData() {
        deviceConnected.postValue(null)
    }

    fun isHubDeviceConnected(): Boolean{
        return isHubDeviceConnected(null,null)
    }

    private fun isHubDeviceConnected(videoUrl: String?, content: Content?): Boolean {
        bineConnect.isConnected(getDefaultDevice())?.let { connectedDevice ->
            if (deviceIdFetchRunning) return true
            deviceIdFetchRunning = true
            CoroutineScope(Dispatchers.Default).launch {
                val deviceIdResponse = BineAPI.Devices().getDeviceId(BNConstants.getDeviceIP())
                deviceIdResponse.result?.let {
                    deviceIdFetchRunning = false
                    retryDownloadIfconnected(videoUrl, content)
                    val oldConnectedDevice = contentRepository.connectedHubDevice()
                    if (oldConnectedDevice != null && !oldConnectedDevice.id.isNullOrEmpty()) {
                        if (oldConnectedDevice.id == it) {
                            deviceConnected.postValue(oldConnectedDevice)
                            return@launch
                        }
                    }
                    retryDownloadIfconnected(videoUrl, content)
                    connectedDevice.id = it
                    contentRepository.saveConnectedHubDevice(connectedDevice)
                    deviceConnectionActive.postValue(Event(true))
                    deviceConnected.postValue(connectedDevice)
                    wifiFrequencyBand.postValue(bineConnect.getWifiInfoFrequency() ?: "")
                } ?: deviceIdResponse.error?.let {
                    AnalyticsLogger.getInstance().logGenericLogs(
                        com.microsoft.mobile.polymer.mishtu.telemetry.Event.API_LOG,
                        "DeviceId",
                        it.name,
                        deviceIdResponse.details)
                    if(!videoUrl.isNullOrEmpty() && content != null){
                        AnalyticsLogger.getInstance().logGenericLogs(
                                com.microsoft.mobile.polymer.mishtu.telemetry.Event.DOWNLOAD_LOG,
                                "DeviceId",
                                "Retry failed, ${it.name}",
                                content.contentId)
                        Log.d(com.microsoft.mobile.polymer.mishtu.telemetry.Event.DOWNLOAD_LOG.value,"Not able to connect after retry calling getDeviceId ${content.contentId}" )
                        deviceConnectError.postValue("Unable to connect, Please check your network.")
                    }
                    else{
                        deviceConnectError.postValue(it.name)
                    }
                    contentRepository.saveConnectedHubDevice(null)
                    deviceConnected.postValue(null)
                    deviceIdFetchRunning = false
                }
            }
            return true
        } ?: if (bineConnect.isDeviceDetected(getDefaultDevice())) {
            deviceDetected.postValue(true)
            contentRepository.saveConnectedHubDevice(null)
            deviceConnected.postValue(null)
        } else {
            deviceDetected.postValue(false)
            contentRepository.saveConnectedHubDevice(null)
            deviceConnected.postValue(null)
        }
        return false
    }

    private fun retryDownloadIfconnected(videoUrl: String?, content: Content?) {
        if (!videoUrl.isNullOrEmpty() && content != null) {
            AnalyticsLogger.getInstance().logGenericLogs(
                    com.microsoft.mobile.polymer.mishtu.telemetry.Event.DOWNLOAD_LOG,
                    "DeviceId",
                    "Success, retrying download",
                    content.contentId)
            downloadManager.beginDownload(videoUrl, content)
        }
    }

    private fun getDefaultDevice(): Hub {
        val hub = Hub()
        var ssid = BNConstants.DEVICE_SSID
        val settingsSSID = SharedPreferenceStore.getInstance().get(BNConstants.KEY_EXTRA_DEVICE_SSID)
        if (!settingsSSID.isNullOrEmpty()){ ssid = settingsSSID }
        hub.wifi2GSSID = ssid
        hub.wifi5GSSID = ssid//"$ssid-5G"
        return hub
    }

    fun browseDeviceContent(deviceId: String) {
        if (hubDataFetchRunning) return

        hubDataFetchRunning = true
        CoroutineScope(Dispatchers.Default).launch {
            val contentProviderList = contentRepository.getContentProviders()
            for (cp in contentProviderList) {
                fetchDeviceContentAsync(deviceId, "", cp.id)
            }
        }
    }

    private suspend fun fetchDeviceContentAsync(
        deviceId: String,
        continuationToken: String,
        contentProviderId: String
    ) {
        //In case user had no network while launching the app, this will sync the catalog first before
        //getting the device content. Avoid condition where catalog is outdated hence the join would fail.
        if (continuationToken.isEmpty()) contentRepository.fetchContentIfTime()

        val browseContentResponse = BineAPI.Devices().browseContent(deviceId,
            contentProviderId,
            continuationToken,
            BNConstants.PAGE_SIZE)
        browseContentResponse.result?.let { ids ->
            contentRepository.setHubIdForContents(deviceId, ids)
            browseContentResponse.continuationToken?.let { continuation ->
                fetchDeviceContentAsync(deviceId, continuation, contentProviderId)
            } ?: {
                hubDataFetchRunning = false
            }
        } ?: run {
            hubDataFetchRunning = false
        }
    }

    fun getContentForDevice(deviceid: String, contentProviderIdList: List<String>) {
        loading.postValue(true)
        val contentIdList = ArrayList<String>()
        val combineErrorList = ArrayList<Error>()
        CoroutineScope(Dispatchers.Default).launch {
            for (contentProviderId in contentProviderIdList) {
                getContentForDeviceAsync(deviceid, "", contentIdList,combineErrorList, contentProviderId)
            }
            //Log.d("BottomSheetCon 3",contentIdList.toString())

            if (!contentIdList.isNullOrEmpty()) {
                val result = contentRepository.getContentForContentIds(contentIdList)
                loading.postValue(false)
                deviceContent.postValue(result)
            }
            else{
                loading.postValue(false)
                if (!combineErrorList.isEmpty())
                    error.postValue(combineErrorList[0])
            }
        }
    }

    private suspend fun getContentForDeviceAsync(
        deviceId: String,
        continuationToken: String,
        contentIdList: ArrayList<String>,
        combineResponseError: ArrayList<Error>,
        contentProviderId: String
    ) {
        val browseContentResponse = BineAPI.Devices().browseContent(deviceId,
            contentProviderId,
            continuationToken,
            BNConstants.PAGE_SIZE)
        browseContentResponse.result?.let { ids ->
            contentIdList.addAll(ids.toList())
            browseContentResponse.continuationToken?.let { continuation ->
                getContentForDeviceAsync(deviceId, continuation, contentIdList,combineResponseError, contentProviderId)
            }
        }
        browseContentResponse.error?.let {
            combineResponseError.add(it)
        }
    }

    fun getConnectedDevice(): ConnectedHub? {
        return contentRepository.connectedHubDevice()
    }
    internal fun postConnectionActivePreLoad(): Boolean {
        contentRepository.connectedHubDevice()?.let {
            deviceConnected.postValue(it)
            return true
        }
        deviceConnected.postValue(null)
        return false
    }

    internal fun isConnectionActive(): Boolean {
        contentRepository.connectedHubDevice()?.let {
            return true
        }
        return false
    }

    fun getRetailerName(
        latitude: Double,
        longitude: Double,
        deviceId: String) {
        CoroutineScope(Dispatchers.Default).launch {
            val responseData = BineAPI.RMS().getNearbyRetailer(100.0, latitude, longitude)
            responseData.result?.let {
                for (retailer in it) {
                    for (device in retailer.retailer.deviceAssignments) {
                        if (device.deviceId == deviceId) {
                            retailerName.postValue(retailer.retailer.name)
                            break
                        }
                    }
                }
            }
        }
    }

    override fun onExoDownloadFailureOccurred(videoUrl: String, content: Content) {
        //In case of exoDownloadFailure, retry calling getDeviceId api
        isHubDeviceConnected(videoUrl, content)
    }

    override fun recordDownloadCompleteEvent(contentId: String) {
            CoroutineScope(Dispatchers.Default).launch {
                incentivesRepository.triggerDownloadCompleteEvent(contentId)
            }
    }

}