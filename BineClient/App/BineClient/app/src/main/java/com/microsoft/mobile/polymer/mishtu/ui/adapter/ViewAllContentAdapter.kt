// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.microsoft.mobile.polymer.mishtu.ui.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

import com.microsoft.mobile.polymer.mishtu.R
import com.microsoft.mobile.polymer.mishtu.databinding.ListItemContentSeriesBinding
import com.microsoft.mobile.polymer.mishtu.databinding.ListItemCotentViewAllBinding
import com.microsoft.mobile.polymer.mishtu.storage.entities.Content
import com.microsoft.mobile.polymer.mishtu.storage.entities.ContentDownload
import com.microsoft.mobile.polymer.mishtu.ui.fragment.sheet.BottomSheetContentDetails
import com.microsoft.mobile.polymer.mishtu.utils.*

class ViewAllContentAdapter(
        private val context: Context,
        private val onItemClickListener: OnItemClickListener?,
        private val subscriptionManager: SubscriptionManager,
        private val hubConnected: Boolean,
        private val calledFrom: String
) : ListAdapter<ContentDownload, RecyclerView.ViewHolder>(ContentAdapterDiffCallback()) {

    private val TYPE_SERIES = 1
    private val TYPE_MOVIES = 0

    override fun onCreateViewHolder(
            parent: ViewGroup, viewType: Int
    ): RecyclerView.ViewHolder {
        if(viewType == TYPE_MOVIES) {
            val binding: ListItemCotentViewAllBinding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.list_item_cotent_view_all,
                parent,
                false
            )
            return ItemViewHolder(binding)
        }
        val binding: ListItemContentSeriesBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.list_item_content_series,
            parent,
            false
        )
        return ViewHolderSeries(binding)
    }

    override fun getItemViewType(position: Int): Int {
         super.getItemViewType(position)
        if(calledFrom.equals(BottomSheetContentDetails.CALLED_FROM)){
            return TYPE_SERIES
        }
        return TYPE_MOVIES
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val dataModel = getItem(position)
        if (holder is ItemViewHolder) {
            Log.d("AppU","here")
            holder.bind(dataModel)
        }
        else if(holder is ViewHolderSeries)
        {
            Log.d("AppU","here2")
            holder.bind(dataModel)
        }
    }

    inner class ItemViewHolder(var binding: ListItemCotentViewAllBinding) : RecyclerView.ViewHolder(
            binding.root
    ) {
        fun bind(content: Any) {
            var name: String? = ""
            var thumbnail: String? = ""
            binding.bannerParent.clipToOutline = true
            val packActive = when(content){
                is Content -> subscriptionManager.hasValidSubscriptionForContent(content.contentProviderId, content.contentId)
                is ContentDownload -> subscriptionManager.hasValidSubscriptionForContent(content.contentProviderId, content.contentId)
                else -> false
            }

            (content as? ContentDownload)?.let {
                name = content.getContentTitle()
                thumbnail = it.getThumbnailImage()

                binding.root.setOnClickListener {
                    onItemClickListener?.onItemCLicked(content)
                }

                binding.freeTag.visibility = if (content.free) View.VISIBLE else View.GONE
                AppUtils.loadImage(context, AppUtils.getContentProviderSquareLogoURL(content.contentProviderId),binding.providerLogo)
                if (content.free || packActive) {
                    if (hubConnected)
                        binding.playButton.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_btn_download))
                    else binding.playButton.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.bn_ic_play))
                    binding.infoButton.visibility = ViewGroup.GONE
                }
                else {
                    showInfoIcon(content)
                }
                AppUtils.updateDownloadStatus(context, content, binding.playButton)
            }

            (content as? Content)?.let {
                name = content.getContentTitle()
                thumbnail = it.getThumbnailImage()

                binding.root.setOnClickListener {
                    onItemClickListener?.onItemCLicked(
                        BOConverter.getContentDownloadFromContent(
                            content
                        )
                    )
                }
                if (content.free || packActive) {
                    binding.playButton.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.bn_ic_play))
                    binding.infoButton.visibility = ViewGroup.GONE
                }
                else {
                    showInfoIcon(BOConverter.getContentDownloadFromContent(content))
                }
            }
            binding.title.text = name
            if(!packActive){
                binding.title.setTextColor(context.resources.getColor(R.color.colorWhite))
            }

            AppUtils.loadImageWithRoundedCorners(
                context,
                thumbnail,
                binding.banner
            )
        }
    }

    inner class ViewHolderSeries(var binding: ListItemContentSeriesBinding) :
        RecyclerView.ViewHolder(
            binding.root
        ) {
        fun bind(content: Any) {
            binding.bannerParent.clipToOutline = true
            val mlp = binding.root.layoutParams as ViewGroup.MarginLayoutParams
                mlp.width = ViewGroup.LayoutParams.MATCH_PARENT

            mlp.height = context.resources.getDimension(R.dimen.dp_173).toInt()
            mlp.setMargins(context.resources.getDimension(R.dimen.dp_10).toInt(), 0, 0, 0)

            (content as? ContentDownload)?.let {
                bindFromContentDownload(binding, content)
            }
            (content as? Content)?.let {
                bindFromContentDownload(binding, BOConverter.getContentDownloadFromContent(it))
            }
        }

        private fun bindFromContentDownload(binding: ListItemContentSeriesBinding, content: ContentDownload) {
            var name: String? = ""
            var thumbnail: String? = ""
            name = content.title
            thumbnail = content.getThumbnailImage()
            binding.root.setOnClickListener {
                onItemClickListener?.onItemCLicked(content)
            }
            val packActive = subscriptionManager.hasValidSubscriptionForContent(content.contentProviderId, content.contentId)
            if (content.free || packActive) {
                if (hubConnected)
                    binding.playButton.setImageDrawable(ContextCompat.getDrawable(context,
                        R.drawable.ic_btn_download))
                else binding.playButton.setImageDrawable(ContextCompat.getDrawable(context,
                    R.drawable.bn_ic_play))
                //binding.infoButton.visibility = ViewGroup.GONE
            } else {
                binding.playButton.visibility = View.GONE
                //showInfoIcon(content)
            }
            AppUtils.updateDownloadStatus(context, content, binding.playButton)
            val seasonNumber = content.season?.lowercase()?.replace("season", "")
            val episodeNumber = content.episode?.lowercase()?.replace("episode", "")
            binding.episodeNumber.text =
                "S${seasonNumber} E${episodeNumber} • ${content.yearOfRelease}"

            binding.episodeTitle.text = content.title
            binding.episodeDescription.text = content.shortDescription

            val playbackSeconds = (content.playBackPosition ?: 0) / 1000
            val durationSeconds = content.durationInMts * 60
            val total = ((playbackSeconds.toFloat() / durationSeconds)) * 100
            binding.progressBar.progress = total.toInt()

            /*if (!isPackActive) {
                binding.episodeDescription.setTextColor(context.resources.getColor(R.color.colorWhite))
            }*/
            Glide.with(context)
                .load(thumbnail)
                .dontAnimate().placeholder(R.drawable.bg_shimmer_gradient)
                .into(binding.topContentThumbnail)
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
}