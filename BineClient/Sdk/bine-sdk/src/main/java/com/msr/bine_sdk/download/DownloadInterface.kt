// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.msr.bine_sdk.download


interface DownloadInterface {
    fun start(request: DownloadRequest)
    fun setNotifier(downloadNotifier: DownloadNotifier)
}