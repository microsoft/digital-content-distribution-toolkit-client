// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.microsoft.mobile.polymer.mishtu.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.microsoft.mobile.polymer.mishtu.R
import com.microsoft.mobile.polymer.mishtu.databinding.ListItemCotentViewAllBinding
import com.microsoft.mobile.polymer.mishtu.storage.SharedPreferenceStore
import com.microsoft.mobile.polymer.mishtu.storage.entities.Content
import com.microsoft.mobile.polymer.mishtu.storage.entities.ContentDownload
import com.microsoft.mobile.polymer.mishtu.storage.entities.Downloads
import com.microsoft.mobile.polymer.mishtu.utils.*
import com.msr.bine_sdk.hub.model.DownloadStatus

class SearchListAdapter(private val context: Context,
                        private val onItemClickListener: OnItemClickListener?,
                        private val hubConnectedId: String,
                        private val subscriptionManager: SubscriptionManager
) : ListAdapter<ContentDownload, RecyclerView.ViewHolder>(ContentAdapterDiffCallback()) {

    var downloadsList = listOf<Downloads>()
        set(value) {
            field = value
            populateContentDownloadList()
        }

    var contentList = listOf<Content>()
        set(value) {
            field = value
            populateContentDownloadList()
        }

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): RecyclerView.ViewHolder {
        val binding: ListItemCotentViewAllBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context), R.layout.list_item_cotent_view_all, parent, false
        )
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val dataModel = getItem(position)
        if (holder is ItemViewHolder) {
            holder.bind(dataModel)
        }
    }

    inner class ItemViewHolder(var binding: ListItemCotentViewAllBinding) : RecyclerView.ViewHolder(
        binding.root
    ) {
        fun bind(content: ContentDownload) {
            binding.title.text = content.getContentTitle()

            binding.bannerParent.clipToOutline = true
            AppUtils.loadImageWithRoundedCorners(
                context,
                content.getThumbnailImage(),
                binding.banner
            )

            AppUtils.loadImage(context, AppUtils.getContentProviderSquareLogoURL(content.contentProviderId),binding.providerLogo)
            binding.root.setOnClickListener {
                onItemClickListener?.onItemCLicked(content)
            }

            binding.freeTag.visibility = if (content.free) View.VISIBLE else View.GONE

            if (content.free || isPackActive(content)) {
                binding.playButton.visibility = View.VISIBLE
                binding.infoButton.visibility = ViewGroup.GONE

                if (hubConnectedId.isNotEmpty())
                    binding.playButton.setImageDrawable(
                        ContextCompat.getDrawable(
                            context,
                            R.drawable.ic_btn_download
                        )
                    )
                else
                    binding.playButton.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.bn_ic_play
                    )
                )
            } else {
                showInfoIcon(content)
            }
            updateDownloadStatus(content)
        }

        private fun isPackActive(content: ContentDownload): Boolean {
            return subscriptionManager.hasValidSubscriptionForContent(content.contentProviderId, content.contentId)
        }

        private fun updateDownloadStatus(content: ContentDownload) {
            when (content.downloadStatus) {
                DownloadStatus.QUEUED.value -> {
                    binding.playButton.setImageDrawable(
                        ContextCompat.getDrawable(
                            context,
                            R.drawable.ic_download_cancel
                        )
                    )
                }
                DownloadStatus.IN_PROGRESS.value -> {
                    binding.playButton.setImageDrawable(
                        ContextCompat.getDrawable(
                            context,
                            R.drawable.bn_ic_play
                        )
                    )
                }
                DownloadStatus.DOWNLOADED.value -> {
                    binding.playButton.setImageDrawable(
                        ContextCompat.getDrawable(
                            context,
                            R.drawable.bn_ic_play
                        )
                    )
                }
            }
        }
    }

    private fun ItemViewHolder.showInfoIcon(
        content: ContentDownload,
    ) {
        binding.playButton.visibility = View.GONE
        binding.infoButton.visibility = View.VISIBLE
        binding.infoButton.setOnClickListener {
            onItemClickListener?.onItemCLicked(content)
        }
    }

    private fun populateContentDownloadList() {
        val contentDownloadList = arrayListOf<ContentDownload>()
        for (content in contentList) {
            var contentDownload: ContentDownload? = null
            for (download in downloadsList) {
                if (download.contentId == content.contentId) {
                    contentDownload = BOConverter.getContentDownloadFromContent(content, download)
                    break
                }
            }
            if (contentDownload == null) {
                contentDownload = BOConverter.getContentDownloadFromContent(content)
            }
            if (hubConnectedId.isEmpty() || content.hubId == hubConnectedId) {
                contentDownloadList.add(contentDownload)
            }
        }
        submitList(contentDownloadList)
    }
}