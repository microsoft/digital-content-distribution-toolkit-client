// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.microsoft.mobile.polymer.mishtu.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.View
import android.view.ViewGroup.*
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ListAdapter

import com.microsoft.mobile.polymer.mishtu.R
import com.microsoft.mobile.polymer.mishtu.databinding.ListItemFooterBinding
import com.microsoft.mobile.polymer.mishtu.databinding.ListItemTopContentBinding
import com.microsoft.mobile.polymer.mishtu.storage.entities.ContentDownload
import com.microsoft.mobile.polymer.mishtu.utils.AppUtils
import com.microsoft.mobile.polymer.mishtu.utils.BNConstants.CONTENT_VIEW_ALL_POSITION
import com.microsoft.mobile.polymer.mishtu.utils.ContentAdapterDiffCallback
import com.msr.bine_sdk.hub.model.DownloadStatus

class TopContentAdapter(
    private val context: Context,
    private val listener: OnMovieClickListener,
    private var width: Int,
    private var height: Int,
    private var marginStart: Int,
    private var isGridView: Boolean,
    private var isViewAllVisible: Boolean,
    private var titleVisible:Boolean = true,
    private var gradientVisible:Boolean = true,
    private var playVisible:Boolean = true) :
    ListAdapter<ContentDownload, RecyclerView.ViewHolder>(ContentAdapterDiffCallback()) {

    private val TYPE_FOOTER = 1
    private val TYPE_ITEM = 0

    var hasActiveSubscription = false
    @SuppressLint("NotifyDataSetChanged")
    set(value) {
        field = value
        notifyDataSetChanged()
    }

    var hubConnected = false
        @SuppressLint("NotifyDataSetChanged")
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    var packPromotion = false
        @SuppressLint("NotifyDataSetChanged")
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(
            parent: ViewGroup, viewType: Int
    ): RecyclerView.ViewHolder {
        if(viewType==TYPE_ITEM){
        val binding: ListItemTopContentBinding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.context), R.layout.list_item_top_content, parent, false
        )
        return ItemViewHolder(binding)
        }
        val binding: ListItemFooterBinding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.context), R.layout.list_item_footer, parent, false)
        return ViewHolderFooter(binding)
    }

    override fun onBindViewHolder(holder:RecyclerView.ViewHolder, position: Int) {
        val dataModel = getItem(position)
        if (holder is ItemViewHolder) {
            holder.bind(dataModel)
        } else if (holder is ViewHolderFooter) {
            holder.bind(dataModel)
        }
    }

    override fun getItemViewType(position: Int): Int {
        super.getItemViewType(position)
        if (isViewAllVisible && position == CONTENT_VIEW_ALL_POSITION) {
            return TYPE_FOOTER
        }
        return TYPE_ITEM
    }

    override fun getItemCount(): Int {
        if(isViewAllVisible && currentList.size > 5) return 5
        return currentList.size
    }

    inner class ViewHolderFooter(var binding: ListItemFooterBinding) : RecyclerView.ViewHolder(
            binding.root
    ) {
        fun bind(content: ContentDownload) {
            val mlp = binding.root.layoutParams as MarginLayoutParams
            if (isGridView) {
                mlp.width = LayoutParams.MATCH_PARENT
            } else {
                mlp.width = width
            }

            mlp.height = height
            mlp.setMargins(marginStart, 0, 0, 0)

            binding.bannerParent.clipToOutline = true
            AppUtils.loadImageWithRoundedCorners(context,
                content.getThumbnailImage(),
                binding.banner)

            binding.root.setOnClickListener {
                listener.onViewAllCLicked(content)
            }
            
        }
    }

    inner class ItemViewHolder(var binding: ListItemTopContentBinding) : RecyclerView.ViewHolder(
            binding.root
    ) {
        fun bind(content: ContentDownload) {
            val mlp = binding.parent.layoutParams as MarginLayoutParams
            if (isGridView) {
                mlp.width = LayoutParams.MATCH_PARENT
            } else {
                mlp.width = width
            }
            mlp.setMargins(marginStart, 0, 0, 0)

            val thumbSize = binding.topContentThumbnail.layoutParams as MarginLayoutParams
            thumbSize.height = height

            binding.title.text = content.getContentTitle()

            binding.bannerParent.clipToOutline = true

            AppUtils.loadImageWithRoundedCorners(context,
                content.getThumbnailImage(),
                binding.topContentThumbnail)

            binding.root.setOnClickListener {
                listener.onMovieClicked(content)
            }

            binding.playButton.setOnClickListener {
                listener.onMovieClicked(content)
            }

            binding.infoButton.setOnClickListener {
                listener.onMovieClicked(content)
            }

            binding.title.visibility = if(titleVisible) View.VISIBLE else View.GONE
            binding.gradientView.visibility = if(gradientVisible) View.VISIBLE else View.GONE
            binding.playButton.visibility = if(playVisible) View.VISIBLE else View.GONE
            binding.playButtonText.visibility = if(playVisible) View.VISIBLE else View.GONE
            if(packPromotion){
                binding.title.setTextColor(context.resources.getColor(R.color.white))
            }
            if (playVisible) {
                if (content.free || hasActiveSubscription) {
                    if (hubConnected)
                        binding.playButton.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_btn_download))
                    else binding.playButton.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.bn_ic_play))
                    binding.infoButton.visibility = GONE
                }
                else {
                    binding.infoButton.visibility = VISIBLE
                    binding.playButton.visibility = GONE
                }
                binding.playButtonText.text = ""
            }
            updateDownloadStatus(content)
        }

        private fun updateDownloadStatus(content: ContentDownload) {
            when (content.downloadStatus) {
                DownloadStatus.QUEUED.value -> {
                    binding.playButtonText.text = context.getString(R.string.queued)
                    binding.playButton.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_download_cancel))
                }
                DownloadStatus.IN_PROGRESS.value -> {
                    binding.playButtonText.text = context.getString(R.string.button_play_now)
                    binding.playButton.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.bn_ic_play))
                }
                DownloadStatus.DOWNLOADED.value -> {
                    binding.playButtonText.text = context.getString(R.string.button_play_now)
                    binding.playButton.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.bn_ic_play))
                }
            }
        }
    }

    interface OnMovieClickListener {
        fun onMovieClicked(content: ContentDownload)
        fun onViewAllCLicked(content: ContentDownload)
    }
}