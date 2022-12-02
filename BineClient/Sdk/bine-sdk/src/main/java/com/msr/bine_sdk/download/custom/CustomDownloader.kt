// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.msr.bine_sdk.download.custom

import android.content.Context
import android.content.Intent
import android.os.Handler
import com.msr.bine_sdk.download.DownloadInterface
import com.msr.bine_sdk.download.DownloadNotifier
import com.msr.bine_sdk.download.DownloadRequest


class CustomDownloader(private var context: Context): DownloadInterface {

    private var resultReceiver: DownloadResultReceiver? = null
    //private var downloadNotifier: DownloadNotifier? = null

    override fun start(request: DownloadRequest) {
        val intent = Intent(context, CustomDownloadService::class.java)
        intent.putExtra(CustomDownloadService.ReceiverKey, resultReceiver)
        intent.putExtra(CustomDownloadService.RequestKey, request)
        CustomDownloadService.enqueueWork(context, intent)
    }

    override fun setNotifier(downloadNotifier: DownloadNotifier) {
        resultReceiver = DownloadResultReceiver(Handler(), downloadNotifier)
    }
}