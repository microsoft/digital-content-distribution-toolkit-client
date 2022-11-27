package com.microsoft.mobile.polymer.mishtu.utils

import androidx.recyclerview.widget.DiffUtil
import com.microsoft.mobile.polymer.mishtu.storage.entities.ContentDownload

class ContentAdapterDiffCallback : DiffUtil.ItemCallback<ContentDownload>() {
    override fun areItemsTheSame(
        oldItem: ContentDownload,
        newItem: ContentDownload
    ): Boolean {
        return oldItem.contentId == newItem.contentId
    }

    override fun areContentsTheSame(
        oldItem: ContentDownload,
        newItem: ContentDownload
    ): Boolean {
        return oldItem.contentId == newItem.contentId &&
                oldItem.downloadStatus == newItem.downloadStatus &&
                oldItem.downloadProgress == newItem.downloadProgress
    }
}