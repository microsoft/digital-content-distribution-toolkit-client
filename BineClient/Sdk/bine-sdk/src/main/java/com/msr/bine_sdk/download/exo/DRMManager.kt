// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.msr.bine_sdk.download.exo

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Base64
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.database.DatabaseProvider
import com.google.android.exoplayer2.database.ExoDatabaseProvider
import com.google.android.exoplayer2.drm.*
import com.google.android.exoplayer2.ext.okhttp.OkHttpDataSourceFactory
import com.google.android.exoplayer2.offline.*
import com.google.android.exoplayer2.source.dash.DashUtil
import com.google.android.exoplayer2.ui.DownloadNotificationHelper
import com.google.android.exoplayer2.upstream.*
import com.google.android.exoplayer2.upstream.cache.*
import com.google.android.exoplayer2.util.Log
import com.google.android.exoplayer2.util.Util
import com.msr.bine_sdk.Constants
import com.msr.bine_sdk.R
import com.msr.bine_sdk.download.DownloadNotifier
import com.msr.bine_sdk.eventbus.events.LogEvent
import com.msr.bine_sdk.notifications.NotificationHelper
import com.msr.bine_sdk.notifications.NotificationHelper.Companion.DOWNLOAD_STATUS_NOTIFICATION_ID
import com.msr.bine_sdk.secure.BineSharedPreference
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.greenrobot.eventbus.EventBus
import java.io.File
import java.io.IOException
import java.util.*
import java.util.concurrent.Executors

class DRMManager(var context: Context) {
    private val TAG = DRMManager::class.java

    private var sharedPreferences: BineSharedPreference

    companion object {
        const val KEY_OFFLINE_OFFSET_ID = "key_offline_offset_id"
        const val EMPTY = ""
        private const val DOWNLOAD_ACTION_FILE = "actions"
        private const val DOWNLOAD_TRACKER_ACTION_FILE = "tracked_actions"
        private const val DOWNLOAD_CONTENT_DIRECTORY = "downloads"

        //const val licenseUrl = "https://binemediaservices.keydelivery.southeastasia.media.azure.net/Widevine/?kid=d06549e5-6cd2-4fe6-923b-12991854be47"

        private var userAgent: String? = null
        private var downloadDirectory: File? = null
        private var downloadManager: DownloadManager? = null
        private var downloadTracker: DownloadTracker? = null
        private var downloadNotificationHelper: DownloadNotificationHelper? = null
        private var terminalStateNotificationHelper: TerminalStateNotificationHelper? = null
        private var downloadCache: Cache? = null
        private var instance: DRMManager? = null
        private var databaseProvider: DatabaseProvider? = null

        private var notificationHelper: NotificationHelper? = null

        @Synchronized
        fun getInstance(context: Context): DRMManager {
            if (instance == null) {
                instance = DRMManager(context)
            }
            return instance!!
        }

        private fun buildReadOnlyCacheDataSource(
            upstreamFactory: DataSource.Factory?, cache: Cache?
        ): CacheDataSource.Factory {
            return CacheDataSource.Factory()
                .setCache(cache!!)
                .setUpstreamDataSourceFactory(upstreamFactory)
                //.setCacheWriteDataSinkFactory(DataSink.Factory {  })
                .setFlags(CacheDataSource.FLAG_BLOCK_ON_CACHE)
            /*cache!!,
                    upstreamFactory!!,
                    FileDataSource.Factory(),  *//* cacheWriteDataSinkFactory= *//*
                    null,
                    CacheDataSource.FLAG_BLOCK_ON_CACHE,  *//* eventListener= *//*
                    null)
        }*/
        }
    }

    private fun getDatabaseProvider(): DatabaseProvider {
        if (databaseProvider == null) {
            databaseProvider = ExoDatabaseProvider(context)
        }
        return databaseProvider!!
    }

    init {
        userAgent = Util.getUserAgent(context, "Mishtu")
        val spKey = userAgent
        val prefKey = spKey?.replace("/", "_").hashCode().toString()
        sharedPreferences = BineSharedPreference(context)
    }

    fun getDownloadTracker(): DownloadTracker {
        initDownloadManager()
        return downloadTracker!!
    }

    private fun getDownloadCache(): Cache {
        if (downloadCache == null) {
            val downloadContentDirectory = File(getDownloadDirectory(), DOWNLOAD_CONTENT_DIRECTORY)
            downloadCache = SimpleCache(downloadContentDirectory, NoOpCacheEvictor(), getDatabaseProvider())
        }
        return downloadCache!!
    }

    fun getDownloadManager(): DownloadManager {
        initDownloadManager()
        return downloadManager!!
    }

    fun setNotificationHelper(notification: NotificationHelper) {
        notificationHelper = notification
    }

    fun getNotificationHelper(): NotificationHelper {
        if (notificationHelper == null) {
            notificationHelper = object: NotificationHelper(context) {
                override fun getMetaResourceBaseUrl(): String {
                    return ""
                }

                override fun getAppIcon(): Int {
                    return R.drawable.ic_play
                }

                override fun getHomeLaunchIntent(): Intent {
                    return Intent()
                }

            }
        }
        return notificationHelper!!
    }

    @Throws(InterruptedException::class, IOException::class)
    fun buildOfflineDrmSessionManager(
        uri: Uri, requestId: String,
        forceFetchLicense: Boolean,
        token: String
    ): DrmSessionManager {
        var drmLicenseUrl = ""
        var drmSessionManager: DrmSessionManager? = null

        val spKey: String = /*userAgent + "_" +*/ requestId.replace(".", "_")
        val prefKey = spKey.replace("/", "_").hashCode().toString()
        val offlineAssetKeyIdStr: String? = sharedPreferences.getSecure(prefKey)
        var offlineAssetKeyId = Base64.decode(offlineAssetKeyIdStr, Base64.DEFAULT)

        //val token = sharedPreferences.getSecure(requestId) ?: return null
        Log.d(TAG.toString(), "$spKey \n$prefKey")
        Log.d(TAG.toString(), "$offlineAssetKeyId \n$offlineAssetKeyIdStr ContentId: $requestId")
        Log.d(TAG.toString(), "$forceFetchLicense \n ${offlineAssetKeyId.isNotEmpty()}")

        if (!forceFetchLicense && offlineAssetKeyId.isNotEmpty()) {
            /**
             * fetch offline license keys using uri with user agent
             */
            drmLicenseUrl = sharedPreferences.getSecure("$prefKey-$requestId").toString()
            Log.d(TAG.toString(), drmLicenseUrl)

            val mediaDrmCallback: MediaDrmCallback = createMediaDrmCallback(token, drmLicenseUrl, null)
            drmSessionManager = DefaultDrmSessionManager.Builder()
                    .setUuidAndExoMediaDrmProvider(C.WIDEVINE_UUID,
                            FrameworkMediaDrm.DEFAULT_PROVIDER)
                    .setMultiSession(false)
                    .build(mediaDrmCallback)
            drmSessionManager.setMode(DefaultDrmSessionManager.MODE_QUERY, offlineAssetKeyId)

            /*Handler(Looper.getMainLooper()).post {
               // Toast.makeText(context, "Offline Play using stored license key", Toast.LENGTH_SHORT).show()
            }*/
        } else {
            val dataSource: DataSource = buildHttpDataSourceFactory().createDataSource()

            val dashManifest = DashUtil.loadManifest(dataSource, uri)
            val drmInitData = DashUtil.loadFormatWithDrmInitData(dataSource,
                    dashManifest.getPeriod(0))
            drmInitData?.drmInitData?.let {
                for (i in 0 until it.schemeDataCount) {
                    it.get(i).licenseServerUrl?.let { it1 ->
                        drmLicenseUrl = it1
                    }
                }
            }
            val mediaDrmCallback: MediaDrmCallback = createMediaDrmCallback(token, drmLicenseUrl, null)

            drmSessionManager = DefaultDrmSessionManager.Builder()
                    .setUuidAndExoMediaDrmProvider(C.WIDEVINE_UUID,
                            FrameworkMediaDrm.DEFAULT_PROVIDER)
                    .setMultiSession(false)
                    .build(mediaDrmCallback)
            drmSessionManager.setMode(DefaultDrmSessionManager.MODE_QUERY, offlineAssetKeyId)

            val offlineLicenseHelper = OfflineLicenseHelper(
                    drmSessionManager,
                    DrmSessionEventListener.EventDispatcher())

            offlineAssetKeyId = offlineLicenseHelper.downloadLicense(drmInitData!!)

            val p = offlineLicenseHelper.getLicenseDurationRemainingSec(offlineAssetKeyId)
            Log.d(TAG.toString(), "Saving $spKey \n$prefKey")
            Log.d(TAG.toString(), "Saving $offlineAssetKeyId ContentId: $requestId")
            Log.d(TAG.toString(), "Saving $drmLicenseUrl")

            sharedPreferences.saveSecure(prefKey, Base64.encodeToString(offlineAssetKeyId, Base64.DEFAULT))
            sharedPreferences.saveSecure("$prefKey-$requestId", drmLicenseUrl)
            drmSessionManager = DefaultDrmSessionManager.Builder()
                    .setUuidAndExoMediaDrmProvider(C.WIDEVINE_UUID,
                            FrameworkMediaDrm.DEFAULT_PROVIDER)
                    .setMultiSession(false)
                    .build(mediaDrmCallback)
            drmSessionManager.setMode(DefaultDrmSessionManager.MODE_QUERY, offlineAssetKeyId)

            /*Handler(Looper.getMainLooper()).post {
                *//*Toast.makeText(context, "License downloaded $uri",
                        Toast.LENGTH_SHORT).show()*//*
            }*/
        }
        return drmSessionManager
    }

    fun resetDownloadManager() {
        downloadManager = null
    }

    fun setDownloadServiceListener(downloadNotifier: DownloadNotifier, downloadUri: Uri) {
        terminalStateNotificationHelper = getDownloadServiceListener()
        terminalStateNotificationHelper?.setDownloadNotifier(downloadNotifier, downloadUri)
    }

    fun getDownloadServiceListener(): TerminalStateNotificationHelper {
        if (terminalStateNotificationHelper == null) {
            terminalStateNotificationHelper = TerminalStateNotificationHelper(context,
                DOWNLOAD_STATUS_NOTIFICATION_ID,
                getNotificationHelper())
        }
        return terminalStateNotificationHelper!!
    }

    private fun initDownloadManager() {
        if (downloadManager == null) {
            val downloadIndex = DefaultDownloadIndex(getDatabaseProvider())
            upgradeActionFile(context,
                    DOWNLOAD_ACTION_FILE, downloadIndex,  /* addNewDownloadsAsCompleted= */false)
            upgradeActionFile(context,
                    DOWNLOAD_TRACKER_ACTION_FILE, downloadIndex,  /* addNewDownloadsAsCompleted= */true)
            val downloaderConstructorHelper = CacheDataSource.Factory().setCache(getDownloadCache())
                    .setUpstreamDataSourceFactory(buildHttpDataSourceFactory())
            var threadCount = 1
            sharedPreferences.get(BineSharedPreference.KEY_PARALLEL_COUNT)?.let {
                threadCount = if (it.isEmpty()) 1 else it.toInt()
            }
            downloadManager = DownloadManager(
                    context, downloadIndex, DefaultDownloaderFactory(downloaderConstructorHelper, Executors.newFixedThreadPool(threadCount)))

            downloadManager!!.maxParallelDownloads = 1
            downloadTracker = DownloadTracker( /* context= */context, buildDataSourceFactory(), downloadManager)
        }
    }

    private fun upgradeActionFile(
            context: Context, fileName: String, downloadIndex: DefaultDownloadIndex, addNewDownloadsAsCompleted: Boolean) {
        try {
            ActionFileUpgradeUtil.upgradeAndDelete(
                    File(getDownloadDirectory(), fileName),  /* downloadIdProvider= */
                    null,
                    downloadIndex,  /* deleteOnFailure= */
                    true,
                    addNewDownloadsAsCompleted)
        } catch (e: IOException) {
            Log.e("TAG", "Failed to upgrade action file: $fileName", e)
        }
    }

    private fun getDownloadDirectory(): File? {
        if (downloadDirectory == null) {
            downloadDirectory = context.filesDir
            if (downloadDirectory == null) {
                downloadDirectory = context.getExternalFilesDir(null)
            }
        }
        return downloadDirectory
    }

    fun buildDataSourceFactory(): CacheDataSource.Factory {
        val upstreamFactory = DefaultDataSourceFactory(context, buildHttpDataSourceFactory())
        return buildReadOnlyCacheDataSource(upstreamFactory, downloadCache)
    }

    fun createMediaDrmCallback(
            token: String, licenseUrl: String?, keyRequestPropertiesArray: Array<String?>?): HttpMediaDrmCallback {

        val builder = OkHttpClient.Builder()
        builder.addInterceptor { chain ->
            val newRequest = chain.request().newBuilder()
                    .addHeader("Authorization", token)
                    .build()

            val response = chain.proceed(newRequest)
            if (!response.isSuccessful) {
                EventBus.getDefault().post(LogEvent("DownloadLicenseInterceptor", "Error", "${response.code} - ${response.message} - ${newRequest.url}"))
            }

            response
        }

        val logging = HttpLoggingInterceptor()
        logging.setLevel(HttpLoggingInterceptor.Level.BODY)
        builder.addInterceptor(logging)
        val client = builder.build()
        val licenseDataSourceFactory = OkHttpDataSourceFactory(client,
                userAgent)

        val drmCallback = HttpMediaDrmCallback(licenseUrl!!, licenseDataSourceFactory)
        if (keyRequestPropertiesArray != null) {
            var i = 0
            while (i < keyRequestPropertiesArray.size - 1) {
                drmCallback.setKeyRequestProperty(keyRequestPropertiesArray[i]!!,
                        keyRequestPropertiesArray[i + 1]!!)
                i += 2
            }
        }
        return drmCallback
    }

    private fun createOptionalKeyRequestParameters(
            keyRequestPropertiesArray: Array<String>?): Map<String, String>? {
        val result: MutableMap<String, String> = HashMap()
        if (keyRequestPropertiesArray != null) {
            var i = 0
            while (i < keyRequestPropertiesArray.size - 1) {
                result[keyRequestPropertiesArray[i]] = keyRequestPropertiesArray[i + 1]
                i += 2
            }
        }
        return result
    }

    /**
     * Returns a [HttpDataSource.Factory].
     */
    private fun buildHttpDataSourceFactory(): HttpDataSource.Factory {
        val token = sharedPreferences.getSecure(BineSharedPreference.KEY_TOKEN_HUB)
        val builder = OkHttpClient.Builder()
        builder.addInterceptor { chain ->
            Log.d("ThreadInfo", Thread.currentThread().name)
            /*var url = chain.request().url.toUrl().toString()
            val index = url.indexOf("QualityLevels")
            if( index != -1) {
                val encodedPart = url.substring(index)
                url = url.replace(encodedPart, URLEncoder.encode(encodedPart, "UTF-8"))
            }

            val newRequest: Request = chain.request().newBuilder()
                    .url(url)
                    .addHeader("Authorization", "Bearer $token")
                    .build()

            Handler(Looper.getMainLooper()).post {
                Toast.makeText(context, "Requesting: $url",
                        Toast.LENGTH_SHORT).show()
            }*/
            val newRequest = chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()

            val response = chain.proceed(newRequest)
            if (!response.isSuccessful) {
                EventBus.getDefault().post(LogEvent("DownloadMediaInterceptor", "Error", "${response.code} - ${response.message} - ${newRequest.url}"))
            }

            response
        }

        val logging = HttpLoggingInterceptor()
        logging.setLevel(HttpLoggingInterceptor.Level.BASIC)
        builder.addInterceptor(logging)
        val client = builder.build()
        return OkHttpDataSourceFactory(client, userAgent)
    }

    fun getDownloadNotificationHelper(): DownloadNotificationHelper {
        if (downloadNotificationHelper == null) {
            downloadNotificationHelper = DownloadNotificationHelper(context, Constants.ID_PROGRESS_CHANNEL)
        }
        return downloadNotificationHelper!!
    }

    fun clearLicenseInfo(requestId: String) {
        val spKey: String = requestId.replace(".", "_")
        val prefKey = spKey.replace("/", "_").hashCode().toString()
        Log.d(TAG.toString(), spKey)
        Log.d(TAG.toString(), prefKey)
        sharedPreferences.saveSecure(prefKey, "")
        sharedPreferences.saveSecure("$prefKey-$requestId", "")
    }

    fun hasLicenseInfo(requestId: String): Boolean {
        val spKey: String = requestId.replace(".", "_")
        val prefKey = spKey.replace("/", "_").hashCode().toString()
        return !sharedPreferences.getSecure(prefKey).isNullOrEmpty()
    }
}