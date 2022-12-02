// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.msr.bine_sdk.download.exo

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.google.android.exoplayer2.offline.DownloadService
import com.google.android.exoplayer2.upstream.HttpDataSource.InvalidResponseCodeException
import com.google.android.exoplayer2.util.Util
import com.google.gson.Gson
import com.msr.bine_sdk.BineAPI
import com.msr.bine_sdk.Constants
import com.msr.bine_sdk.auth.AuthManager
import com.msr.bine_sdk.cloud.models.APIResponse
import com.msr.bine_sdk.cloud.models.Content
import com.msr.bine_sdk.download.DownloadInterface
import com.msr.bine_sdk.download.DownloadNotifier
import com.msr.bine_sdk.download.DownloadRequest
import com.msr.bine_sdk.eventbus.events.LogEvent
import com.msr.bine_sdk.secure.BineSharedPreference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import javax.inject.Inject

class ExoDownloader @Inject constructor(private var context: Context,
                                        private val authManager: AuthManager,
                                        private val sharedPreference: BineSharedPreference,
                                        private val drmManager: DRMManager) : DownloadInterface {

    private var downloadNotifier: DownloadNotifier? = null
    private var retryCount = 0
    override fun start(request: DownloadRequest) {
        CoroutineScope(Dispatchers.Default).launch {
            var tokenResponse = callTokenApi(request)
            while (tokenResponse == null && retryCount<Constants.RETRY_COUNT){
                retryCount++
                tokenResponse = callTokenApi(request)
            }
            tokenResponse?.result?.let { it ->
                startService(it, request)

            } ?: tokenResponse?.error?.let {
                EventBus.getDefault().post(LogEvent("AccessToken", "Error", it.toString()))
                downloadNotifier?.onDownloadFailure(request.id,
                    "Token Request Failed " + tokenResponse.details)
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(context, "Error getting token - ${tokenResponse.code} - ${tokenResponse.details}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    private suspend fun callTokenApi(request: DownloadRequest): APIResponse.AssetTokenResponse? {
        return try {
            val response = BineAPI.OMS().getAssetToken(request.id)
            if(!response.result.isNullOrEmpty()){
                EventBus.getDefault().post(LogEvent("AccessToken", "Success, RetryCount = $retryCount", null))
                response
            }else{
                EventBus.getDefault().post(LogEvent("AccessToken", "Code : ${response.result}, RetryCount = $retryCount", null))
                null
            }
        }catch (e:Exception ){
            EventBus.getDefault().post(LogEvent("AccessToken", "Failed, RetryCount = $retryCount", null))
            null
        }

    }

    override fun setNotifier(downloadNotifier: DownloadNotifier) {
        this.downloadNotifier = downloadNotifier
    }
// define a new method for retry and call this method
    private fun startService(token: String, request: DownloadRequest) {

        CoroutineScope(Dispatchers.Default).launch {
            var retryCountForLicenseCall = 0
            var isSuccess = startServiceWithRetry(token, request,0)
            while(!isSuccess && retryCountForLicenseCall< Constants.RETRY_COUNT){
                retryCountForLicenseCall++
                isSuccess = startServiceWithRetry(token, request, retryCountForLicenseCall)
            }
        }
    }

    private fun startServiceWithRetry(token: String, request: DownloadRequest, retryCount: Int): Boolean {

        try {
            val content: Content = Gson().fromJson(request.extras, Content::class.java)

            //Build manager for offline - will download license and persist
            drmManager.buildOfflineDrmSessionManager(Uri.parse(content.dashUrl),
                    request.id,
                    true,
                    token)

            downloadNotifier?.onLicenseDownloaded(request.id)

            val downloadRequest = com.google.android.exoplayer2.offline.DownloadRequest.Builder(request.id,
                    request.downloadUri)
                    .setData(Util.getUtf8Bytes(request.extras))
                    .build()

            //Start service
            DownloadService.sendAddDownload(
                    context, ExoDownloadService::class.java,
                    downloadRequest,
                    false)
            EventBus.getDefault().post(LogEvent("License call", "Success, RetryCount = $retryCount", null))
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            if (e.localizedMessage != null) {
                downloadNotifier?.onDownloadFailure(request.id, e.localizedMessage)
            }
            else {
                downloadNotifier?.onDownloadFailure(
                        request.id,
                        "Download Failed" + e.message
                )
            }
            EventBus.getDefault().post(LogEvent("License call", "Failed in While, RetryCount = $retryCount", e.toString()))
            return false
        }
    }

    private fun hasHubTokenFailed(e: java.lang.Exception): Boolean {
        if (e.message != null && e.message!!.contains("401") &&
                e is InvalidResponseCodeException) {
            val dataSpec = e.dataSpec
            return dataSpec.uri.toString().contains("mpd") ||
                    dataSpec.uri.toString().contains("mp4")
        }
        return false
    }

    private fun hasAssetTokenFailed(e: java.lang.Exception): Boolean {
        return e.cause != null && e.cause!!.message != null && e.cause!!.message!!.contains("401")
    }

    private fun requestHubToken(request: DownloadRequest) {
        //Fetch Asset Token
        CoroutineScope(Dispatchers.Default).launch {
            val tokenResponse = authManager.refreshHubToken()
            tokenResponse.result?.let { it ->
                //Start download
                downloadNotifier?.onDownloadGenericEvent(request.id, "HubTokenRefreshed", "")
                drmManager.resetDownloadManager()
                startService(it, request)
            } ?: tokenResponse.error?.let {
                downloadNotifier?.onDownloadFailure(request.id, "AssetTokenRefreshFailed " + tokenResponse.details)
            }
        }
    }
}