// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.msr.bine_sdk.download

import android.content.Context
import android.text.TextUtils
import com.msr.bine_sdk.di.BineComponent
import com.msr.bine_sdk.di.BineModule
import com.msr.bine_sdk.di.DaggerBineComponent
import com.msr.bine_sdk.download.custom.CustomDownloader
import com.msr.bine_sdk.download.exo.DRMManager
import com.msr.bine_sdk.download.exo.ExoDownloader
import com.msr.bine_sdk.notifications.NotificationHelper
import com.msr.bine_sdk.secure.KeystoreManager
import javax.inject.Inject

class Downloader @Inject constructor(private val context: Context, notificationHelper: NotificationHelper) {

    enum class TYPE {
        EXO,
        CUSTOM
    }

    @Inject
    lateinit var exoDownloader: ExoDownloader

    @Inject
    lateinit var customDownloader: CustomDownloader

    val bineComponent: BineComponent by lazy {
        DaggerBineComponent.builder().bineModule(BineModule(context)).build()
    }

    init {
        bineComponent.inject(this)
        KeystoreManager.init(context)
        DRMManager.getInstance(context).setNotificationHelper(notificationHelper)
    }

    fun start(request: DownloadRequest,
                     downloadNotifier: DownloadNotifier) {
        if (TextUtils.isEmpty(request.downloadUri.toString())) {
            downloadNotifier.onDownloadFailure(request.id, "Empty Download Uri")
            return
        }

        if (request.type == TYPE.EXO) {
            DRMManager.getInstance(context).setDownloadServiceListener(downloadNotifier, request.downloadUri)
        }

        downloadNotifier.onDownloadQueued(request.id, request.type)

        val downloader = getDownloader(request)
        downloader.setNotifier(downloadNotifier)
        downloader.start(request)
    }

    fun clearDownloads(requestIds: List<String>) {
        for (reqId in requestIds) {
            DRMManager.getInstance(context).clearLicenseInfo(reqId)
        }
    }

    private fun getDownloader(request: DownloadRequest): DownloadInterface {
        return when (request.type) {
            TYPE.EXO -> exoDownloader
            TYPE.CUSTOM -> customDownloader
        }
    }
}