package com.msr.bine_android.download

data class DownloadProgress(
        var contentId: String,
        var progress:Int,
        var isQueued: Boolean
) {}