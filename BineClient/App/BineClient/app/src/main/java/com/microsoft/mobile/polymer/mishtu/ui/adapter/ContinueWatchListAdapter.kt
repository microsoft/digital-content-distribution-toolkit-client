// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.microsoft.mobile.polymer.mishtu.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.microsoft.mobile.polymer.mishtu.R
import com.microsoft.mobile.polymer.mishtu.databinding.ListItemContineWatchBinding

import com.microsoft.mobile.polymer.mishtu.storage.entities.ContentDownload
import com.microsoft.mobile.polymer.mishtu.utils.AppUtils
import com.microsoft.mobile.polymer.mishtu.utils.OnItemClickListener
import com.microsoft.mobile.polymer.mishtu.utils.SubscriptionManager
import java.util.*

class ContinueWatchListAdapter(
        private val context: Context,
        private val subscriptionManager: SubscriptionManager,
        private val onItemClickListener: OnItemClickListener?,
) : RecyclerView.Adapter<ContinueWatchListAdapter.ViewHolder>() {

    var isTrailers = false
    private val dataModelList = ArrayList<ContentDownload>()

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int,
    ): ViewHolder {
        val binding: ListItemContineWatchBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context), R.layout.list_item_contine_watch, parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val dataModel = dataModelList[position]
        holder.bind(dataModel)
    }

    override fun getItemCount(): Int {
        return dataModelList.size
    }

    fun setDataList(data: List<ContentDownload>) {
        dataModelList.clear()
        dataModelList.addAll(data)
        notifyDataSetChanged()
    }

    inner class ViewHolder(private var itemRowBinding: ListItemContineWatchBinding) :
        RecyclerView.ViewHolder(
            itemRowBinding.root
        ) {
        fun bind(content: ContentDownload) {
            try {
                val imageUrl = content.getThumbnailLandscapeImage() ?: content.getThumbnailImage()
                Glide.with(context)
                    .load(imageUrl)
                    .dontAnimate().placeholder(R.drawable.bg_shimmer_gradient)
                    .into(itemRowBinding.banner)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            itemRowBinding.title.text = content.title
            if (!content.isMovie) {
                itemRowBinding.episodeNumber.visibility = View.VISIBLE
                val seasonNumber = content.season?.lowercase()?.replace("season", "")
                val episodeNumber = content.episode?.lowercase()?.replace("episode", "")
                itemRowBinding.episodeNumber.text =
                    "S${seasonNumber} E${episodeNumber}"
            }
            AppUtils.loadImage(context,
                AppUtils.getContentProviderSquareLogoURL(content.contentProviderId),
                itemRowBinding.providerLogo)
            if (!isTrailers) {
                val playbackSeconds = (content.playBackPosition ?: 0) / 1000
                val durationSeconds = content.durationInMts * 60
                val total = ((playbackSeconds.toFloat() / durationSeconds)) * 100
                itemRowBinding.progressBar.progress = total.toInt()
            } else {
                itemRowBinding.progressBar.visibility = View.GONE
            }

            itemRowBinding.parent.setOnClickListener {
                onItemClickListener?.onItemCLicked(content)
            }
            itemRowBinding.bannerParent.clipToOutline = true

            if (!content.free && !subscriptionManager.hasValidSubscriptionForContent(content.contentProviderId, content.contentId)) {
                itemRowBinding.viewDisable.visibility = View.VISIBLE
            } else {
                itemRowBinding.viewDisable.visibility = View.GONE
            }
        }
    }
}