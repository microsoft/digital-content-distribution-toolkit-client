package com.microsoft.mobile.polymer.mishtu.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.microsoft.mobile.polymer.mishtu.R
import com.microsoft.mobile.polymer.mishtu.databinding.ListItemContentSeriesBinding
import androidx.recyclerview.widget.ListAdapter
import com.microsoft.mobile.polymer.mishtu.databinding.ListItemFooterBinding

import com.microsoft.mobile.polymer.mishtu.storage.entities.ContentDownload
import com.microsoft.mobile.polymer.mishtu.utils.*
import com.msr.bine_sdk.hub.model.DownloadStatus
import java.util.*

class SeriesAdapter(
        private val context: Context,
        private val onMovieClickListener: OnMovieClickListener,
        private var attributeMap: Map<String, Any>,
        private val subscriptionManager: SubscriptionManager,
) : ListAdapter<ContentDownload, RecyclerView.ViewHolder>(
    ContentAdapterDiffCallback()) {

    private val TYPE_FOOTER = 1
    private val TYPE_ITEM = 0

    var isTrailers = false

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int,
    ): RecyclerView.ViewHolder {
        if(viewType==TYPE_ITEM) {
            val binding: ListItemContentSeriesBinding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.list_item_content_series,
                parent,
                false
            )
            return ViewHolder(binding)
        }
        val binding: ListItemFooterBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context), R.layout.list_item_footer, parent, false)
        return ViewHolderFooter(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val dataModel = getItem(position)
        if (holder is SeriesAdapter.ViewHolder) {
            holder.bind(dataModel)
        } else if (holder is SeriesAdapter.ViewHolderFooter) {
            holder.bind(dataModel)
        }

    }


    override fun getItemCount(): Int {
        if(attributeMap["isViewAllVisible"] as Boolean  && currentList.size > 5) return 5

        return currentList.size
    }

    override fun getItemViewType(position: Int): Int {
         super.getItemViewType(position)
        if (attributeMap["isViewAllVisible"] as Boolean && position == BNConstants.CONTENT_VIEW_ALL_POSITION) {
            return TYPE_FOOTER
        }
        return TYPE_ITEM
    }

    var hubConnected = false
        @SuppressLint("NotifyDataSetChanged")
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    inner class ViewHolder(private var itemRowBinding: ListItemContentSeriesBinding) :
        RecyclerView.ViewHolder(
            itemRowBinding.root
        ) {
        fun bind(content: ContentDownload) {
            val mlp = itemRowBinding.root.layoutParams as ViewGroup.MarginLayoutParams
            if (attributeMap["isGridView"] as Boolean) {
                mlp.width = ViewGroup.LayoutParams.MATCH_PARENT
            } else {
                mlp.width = attributeMap["width"] as Int
            }

            mlp.height = attributeMap["height"] as Int
            mlp.setMargins(attributeMap["marginStart"] as Int, 0, 0, 0)
            val imageUrl = content.getThumbnailLandscapeImage() ?: content.getThumbnailImage()
            Glide.with(context)
                .load(imageUrl)
                .dontAnimate().placeholder(R.drawable.bg_shimmer_gradient)
                .into(itemRowBinding.topContentThumbnail)

            val seasonNumber = content.season?.lowercase()?.replace("season", "")
            val episodeNumber = content.episode?.lowercase()?.replace("episode", "")
            itemRowBinding.episodeNumber.text =
                "${context.getString(R.string.season_suffix)}${seasonNumber} ${context.getString(R.string.episode_suffix)}${episodeNumber} • ${content.yearOfRelease}"

            itemRowBinding.episodeTitle.text = content.title
            itemRowBinding.episodeDescription.text = content.shortDescription



            if (!isTrailers) {
                val playbackSeconds = (content.playBackPosition ?: 0) / 1000
                val durationSeconds = content.durationInMts * 60
                val total = ((playbackSeconds.toFloat() / durationSeconds)) * 100
                itemRowBinding.progressBar.progress = total.toInt()
            } else {
                itemRowBinding.progressBar.visibility = View.GONE
            }

            /*itemRowBinding.parent.setOnClickListener {
                onItemClickListener?.onItemCLicked(content)
            }*/
            itemRowBinding.bannerParent.clipToOutline = true

            itemRowBinding.bannerParent.setOnClickListener {
                onMovieClickListener.onMovieClicked(content)
            }

            if (content.free || subscriptionManager.hasValidSubscriptionForContent(content.contentProviderId, content.contentId)) {
                if (hubConnected)
                    itemRowBinding.playButton.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_btn_download))
                else
                    itemRowBinding.playButton.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.bn_ic_play))
            }else{
                itemRowBinding.playButton.visibility = View.GONE
            }
            AppUtils.updateDownloadStatus(context, content, itemRowBinding.playButton)
        }
    }

    inner class ViewHolderFooter(var binding: ListItemFooterBinding) : RecyclerView.ViewHolder(
        binding.root
    ) {
        fun bind(content: ContentDownload) {
            val mlp = binding.root.layoutParams as ViewGroup.MarginLayoutParams
            if (attributeMap["isGridView"] as Boolean) {
                mlp.width = ViewGroup.LayoutParams.MATCH_PARENT
            } else {
                mlp.width = attributeMap["width"]  as Int
            }

            mlp.height = attributeMap["footerHeight"] as Int
            mlp.setMargins(attributeMap["marginStart"] as Int, 0, 0, 0)

            binding.bannerParent.clipToOutline = true
            AppUtils.loadImageWithRoundedCorners(context,
                content.getThumbnailImage(),
                binding.banner)

            binding.root.setOnClickListener {
                if(content.name != null && content.season != null)
                onMovieClickListener.onViewAllCLicked(currentList[0])
            }

        }
    }
    interface OnMovieClickListener {
        fun onMovieClicked(content: ContentDownload)
        fun onViewAllCLicked(content: ContentDownload)
    }
}
