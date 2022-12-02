// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.microsoft.mobile.polymer.mishtu.storage.repositories

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.microsoft.mobile.polymer.mishtu.storage.AppDatabase
import com.microsoft.mobile.polymer.mishtu.storage.SharedPreferenceStore
import com.microsoft.mobile.polymer.mishtu.storage.entities.*
import com.msr.bine_sdk.BineAPI

import java.util.*

import com.microsoft.mobile.polymer.mishtu.storage.snappyDB.LocalStorageException
import com.microsoft.mobile.polymer.mishtu.storage.snappyDB.NoSqlDBException
import com.microsoft.mobile.polymer.mishtu.storage.snappyDB.SnappyDB
import com.microsoft.mobile.polymer.mishtu.telemetry.bootmarker.BootTelemetryLogger
import com.microsoft.mobile.polymer.mishtu.utils.BNConstants
import com.microsoft.mobile.polymer.mishtu.utils.BOConverter
import com.msr.bine_sdk.hub.model.ConnectedHub
import com.msr.bine_sdk.hub.model.DownloadStatus
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import kotlin.collections.ArrayList

class ContentRepository internal constructor(
    private val contentDao: ContentDao,
    private val contentProviderDao: ContentProviderDao,
    private val downloadsDao: DownloadsDao,
    private val contentDownloadDao: ContentDownloadDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val mDB: SnappyDB,
) {

    private val formatter = SimpleDateFormat(BNConstants.formatter, Locale.getDefault())
    var allContentProvidersLiveData = MutableLiveData<List<ContentProvider>>()

    companion object {
        const val LOG_TAG = "ContentRepository"

        const val KEY_WATCH_LIST = "watchList"
        const val KEY_CONTENT_DATE_MODEL = "ContentFetchDate"
    }

    private fun saveContentProviders(contentProviders: List<ContentProvider>) {
        contentProviderDao.deleteAll()
        for (provider in contentProviders) {
            contentProviderDao.insert(provider)
        }
        allContentProvidersLiveData.postValue(contentProviders)
    }

    suspend fun getContentProviderId(name: String): String? {
        contentProviderDao.getProviderByName(name)?.let {
            return it.id
        }
        return null
    }

    private fun saveBNContent(contents: List<com.msr.bine_sdk.cloud.models.Content>) {
        for (content in contents) {
            val contentBO = BOConverter.getContentBOFromBNContent(content)
            contentDao.insert(contentBO)
        }
    }

    //Get All Content
    fun getAllContent(): LiveData<List<Content>> {
        return contentDao.getAllContent()
    }

    fun getPaidContentDownloads(limit: Int, isMovie: Int, contentProviderId: String) =
        contentDownloadDao.getMovies(0, limit, isMovie, contentProviderId)

    fun getPaidContent() = contentDao.getPaidContent()
    fun getFreeContentDownloads(limit: Int, isMovie: Int, contentProviderId: String) =
        contentDownloadDao.getMovies(1, limit, isMovie, contentProviderId)

    fun getContent(contentId: String) = contentDao.getContent(contentId)
    fun getAllEpisodesOfSeason(seriesName: String, seasonName: String, contentProviderId: String) =
        contentDownloadDao.getEpisodesOfSeason(seriesName, seasonName, contentProviderId)

    fun getAllfSeasonOfSeries(seriesName: String, contentProviderId: String) =
        contentDownloadDao.getAllfSeasonOfSeries(seriesName, contentProviderId)

    suspend fun getAllEpisodeOfSeries(seriesName: String, contentProviderId: String) =
        contentDownloadDao.getAllEpisodesOfSeries(seriesName, contentProviderId)

    //Get All Downloads
    fun getAllDownloads() = downloadsDao.getAllDownloads()

    suspend fun getContentForContentIds(ids: List<String>): List<Content> {
        return contentDao.getContentListByIds(ids)
    }
    suspend fun getPaidContentListByContentProviderId(ids: String): List<Content>? {
        return contentDao.getPaidContentListByContentProviderId(ids)
    }


    //Get All Content
    fun getAllMovies(
        limit: Int,
        isMovie: Int,
        contentProviderId: String,
    ): LiveData<List<ContentDownload>> {
        return contentDownloadDao.getAllMovies(limit, isMovie, contentProviderId)
    }

    fun setHubIdForContents(hubId: String, ids: Array<String>) {
        return contentDao.setHubIdForContents(hubId, ids)
    }

    //Get All Content for hubId
    fun getAllMoviesForHubId(
        hubId: String,
        limit: Int,
        isMovie: Int,
        contentProviderId: String,
    ): LiveData<List<ContentDownload>> {
        return contentDownloadDao.getAllMoviesForHubId(hubId, limit, isMovie, contentProviderId)
    }

    fun getFreeMoviesForHubId(
        hubId: String,
        limit: Int,
        isMovie: Int,
        contentProviderId: String,
    ): LiveData<List<ContentDownload>> {
        return contentDownloadDao.getMoviesForHubId(hubId, 1, limit, isMovie, contentProviderId)
    }

    fun getPaidMoviesForHubId(
        hubId: String,
        limit: Int,
        isMovie: Int,
        contentProviderId: String,
    ): LiveData<List<ContentDownload>> {
        return contentDownloadDao.getMoviesForHubId(hubId, 0, limit, isMovie, contentProviderId)
    }

    fun insertDownload(contentId: String, url: String, status: DownloadStatus, progress: Int) {
        AppDatabase.databaseWriteExecutor.execute {
            downloadsDao.insert(Downloads(contentId,
                url,
                status.value,
                progress))
        }
    }

    fun updateDownload(contentId: String, status: DownloadStatus, progress: Int) {
        AppDatabase.databaseWriteExecutor.execute {
            downloadsDao.updateDownloadStatus(
                contentId,
                status.value,
                progress
            )
        }
    }

    fun delete(contentId: String) {
        AppDatabase.databaseWriteExecutor.execute {
            downloadsDao.deleteDownload(contentId)
        }
    }

    //Get All Content with prefix ContentModel/1
    suspend fun getAllSeriesContent(seriesName: String): List<Content> {
        return contentDao.getSeriesContent(seriesName)
    }


    fun addWatchList(it: ContentDownload) {
        try {
            val strObject = Gson().toJson(it)
            val isMovie = if (it.isMovie) "1" else "0"
            checkAndDeleteIfAlreadyAddedToWatchList(it)
            mDB.putString(KEY_WATCH_LIST + "/" + isMovie + "/" + Date().time +"/" +it.contentId, strObject)
        } catch (e: NoSqlDBException) {
            e.printStackTrace()
            throw LocalStorageException(e)
        }
    }

    private fun checkAndDeleteIfAlreadyAddedToWatchList(content: ContentDownload){
        val list = mDB.findKeysByPrefix("$KEY_WATCH_LIST/" + if(content.isMovie) 1 else 0)
        for(key in list.reversed()){
            val strObject = mDB.getString(key)
            val contentBO: ContentDownload? = Gson().fromJson(strObject, ContentDownload::class.java)
            if (contentBO != null && contentBO.contentId == content.contentId) {
                mDB.deleteKey(key)
                return
            }
        }

    }

    @kotlin.Throws(LocalStorageException::class)
    fun getMovieWatchList(isMovie: Int): ArrayList<ContentDownload> {
        val contentBOs = ArrayList<ContentDownload>()
        for (key in mDB.findKeysByPrefix("$KEY_WATCH_LIST/" + isMovie)) {
            val strObject = mDB.getString(key)
            val contentBO: ContentDownload? = Gson().fromJson(strObject, ContentDownload::class.java)
            contentBO?.let {
                contentBOs.add(it)
            }
        }
        return ArrayList(contentBOs.reversed())
    }

    /*@kotlin.Throws(LocalStorageException::class)
    fun getSeriesWatchList(): ArrayList<Content> {
        val contentBOs = ArrayList<Content>()
        for (key in mDB.findKeysByPrefix("$KEY_WATCH_LIST/0")) {
            Log.e("here keys", "" + key.toString())
            val contentBO = mDB.getObject(key, Content::class.java)
            contentBO?.let {
                contentBOs.add(it)
            }
        }
        return contentBOs
    }*/

    fun getContentByLanguage(language: String): List<Content> {
        return contentDao.getContentForLanguage(language)
    }

    fun getContentByGenre(genre: String): List<Content> {
        return contentDao.getContentForGenre(genre)
    }

    fun getContentByQuery(query: String): List<Content> {
        return contentDao.getContentForQuery("%${query.uppercase(Locale.getDefault())}%")
    }

    fun reset() {
        SharedPreferenceStore.getInstance().save(KEY_CONTENT_DATE_MODEL, "")
        clearContent()
        clearWatchList()
        clearDownloads()
    }

    private fun clearDownloads() {
        downloadsDao.deleteAll()
    }

    private fun clearContent() {
        //contentProviderDao.deleteAll()
        contentDao.deleteAll()
    }

    fun clearWatchList() {
        for (key in mDB.findKeysByPrefix(KEY_WATCH_LIST)) {
            mDB.deleteKey(key)
        }
    }

    //Async Fetch operation
    fun fetchContentAsync() {
        CoroutineScope(Dispatchers.Default).launch(ioDispatcher) {
            val contentProviders = contentProviderDao.getAllProviders()
            fetchContent(contentProviders)
        }
    }

    suspend fun getContentProviders(): List<ContentProvider> {
        return contentProviderDao.getAllProviders()
    }

    private fun shouldFetch(): Boolean {
        val dateString = SharedPreferenceStore.getInstance().get(KEY_CONTENT_DATE_MODEL)
        if (dateString.isNullOrEmpty()) {
            Log.d(LOG_TAG, "DB - Date empty")
            return true
        } else {
            try {
                val date = formatter.parse(dateString)
                Log.d(LOG_TAG, "DB - $dateString")
                date?.let {
                    val difference: Long = Date().time - it.time
                    val hours = difference / (60 * 60 * 1000)
                    Log.d(LOG_TAG, "DB Difference- $hours")
                    if (hours >= 24) return true
                }
            } catch (e: Exception) {
                return true
            }
        }
        return false
    }

    suspend fun fetchContentIfTime(): Boolean {
        val shouldFetch = shouldFetch()

        if (shouldFetch) {
            val cpList = fetchContentProviders()
            var success = false
            cpList?.let {
                success = fetchContent(it)
            }
            return success
        }
        return false
    }

    fun getAllContentProviders() {
        CoroutineScope(Dispatchers.Default).launch {
            val contentProviderList = getContentProviders()
            allContentProvidersLiveData.postValue(contentProviderList)
        }
    }

    private suspend fun fetchContentProviders(): List<ContentProvider>? {
        val contentProviderResponse = BineAPI.CMS().getContentProviders()
        contentProviderResponse.result?.let {
            if (it.isNotEmpty()) {
                val contentProviders = BOConverter.getContentProviderBOFromBN(it)
                saveContentProviders(contentProviders)
                return contentProviders
            } else {
                Log.d(LOG_TAG, "Content Fetched Error - Empty response")
            }
        }
        contentProviderResponse.error?.let {
            Log.d(LOG_TAG, "Content Fetched Error- ${contentProviderResponse.details}")
        }
        return null
    }

    private suspend fun fetchContent(contentProviders: List<ContentProvider>): Boolean {
        contentDao.deleteAll()

        var success = false
        for (cp in contentProviders) {
            success = fetchContent(cp.id, "")
        }
        if (success)
            SharedPreferenceStore.getInstance()
                .save(KEY_CONTENT_DATE_MODEL, formatter.format(Date()))
        return success
    }

    private suspend fun fetchContent(
        contentProviderId: String,
        continuationToken: String,
    ): Boolean {
        val contentResponse = BineAPI.CMS().getContent(contentProviderId,
            continuationToken,
            BNConstants.PAGE_SIZE)

        contentResponse.result?.let { contents ->
            Log.d(LOG_TAG, "Content Fetched - ${contents.size}")
            BootTelemetryLogger.getInstance().recordIntermediaryEvent(
                BootTelemetryLogger.BootMarker.CATALOG_GET)

            saveBNContent(contents)
            contentResponse.continuationToken?.let { continuation ->
                return fetchContent(contentProviderId, continuation)
            }
            return true
        } ?: contentResponse.error?.let {
            Log.d(LOG_TAG, "Content Fetch Error - ${contentResponse.details}")
        }
        return false
    }

    //Downloads
    fun getDownloadedContent(): LiveData<List<ContentDownload>> {
        return contentDownloadDao.getDownloadedContent()
    }

    fun getDownloadedEpisodes(
        seriesName: String,
        contentProviderId: String,
    ): LiveData<List<ContentDownload>> {
        return contentDownloadDao.getDownloadedEpisodes(seriesName, contentProviderId)
    }

    fun getDownloadingContent(hubId: String): LiveData<List<ContentDownload>> {
        return contentDownloadDao.getDownloadingContent(hubId)
    }

    fun getInProgressContent(hubId: String): LiveData<List<ContentDownload>> {
        return contentDownloadDao.getInProgressContent(hubId)
    }

    fun saveConnectedHubDevice(hub: ConnectedHub?) {
        var json = "";
        if (hub != null) {
            json = Gson().toJson(hub)
        }
        SharedPreferenceStore.getInstance().save(SharedPreferenceStore.KEY_CONNECTED_HUB, json)
    }

    fun connectedHubDevice(): ConnectedHub? {
        val hubString =
            SharedPreferenceStore.getInstance().get(SharedPreferenceStore.KEY_CONNECTED_HUB)
        if (!hubString.isNullOrEmpty()) {
            return Gson().fromJson(hubString, ConnectedHub::class.java)
        }
        return null
    }
}