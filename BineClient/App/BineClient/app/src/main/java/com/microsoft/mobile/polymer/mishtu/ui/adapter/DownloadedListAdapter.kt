package com.microsoft.mobile.polymer.mishtu.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.microsoft.mobile.polymer.mishtu.R
import com.microsoft.mobile.polymer.mishtu.databinding.ListItemDownloadedBinding
import com.microsoft.mobile.polymer.mishtu.storage.SharedPreferenceStore
import com.microsoft.mobile.polymer.mishtu.storage.entities.ContentDownload
import com.microsoft.mobile.polymer.mishtu.utils.AppUtils
import com.microsoft.mobile.polymer.mishtu.utils.SubscriptionManager
import com.msr.bine_sdk.hub.model.DownloadStatus

class DownloadedListAdapter(
        private val context: Context,
        private val onClickListener: DownloadsListClickListener?,
        private val subscriptionManager: SubscriptionManager
) :
    RecyclerView.Adapter<DownloadedListAdapter.ViewHolder>() {
    private val dataModelList = ArrayList<ContentDownload>()
    private var viewEpisodes: Boolean = true

    @SuppressLint("NotifyDataSetChanged")
    fun setDataList(data: List<ContentDownload>,viewEpisodes: Boolean ) {
        this.viewEpisodes = viewEpisodes
        dataModelList.clear()
        dataModelList.addAll(data)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DownloadedListAdapter.ViewHolder {
        val binding: ListItemDownloadedBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context), R.layout.list_item_downloaded, parent, false
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


    inner class ViewHolder(private var itemRowBinding: ListItemDownloadedBinding) : RecyclerView.ViewHolder(
        itemRowBinding.root
    ) {
        fun bind(content: ContentDownload) {
            try {
                val imageUrl = content.getThumbnailLandscapeImage() ?: content.getThumbnailImage()
                Glide.with(context)
                    .load(imageUrl)
                    .dontAnimate().placeholder(R.drawable.bg_shimmer_gradient)
                    .apply(RequestOptions.bitmapTransform(RoundedCorners(25)))
                    .into(itemRowBinding.banner)
                AppUtils.loadImage(context, AppUtils.getContentProviderSquareLogoURL(content.contentProviderId),itemRowBinding.ottLogo)
                itemRowBinding.banner.setOnClickListener {
                    if (viewEpisodes && !content.isMovie) {
                        onClickListener?.onSeriesClicked(content)
                    } else {
                        onClickListener?.onPlayClicked(content)
                    }
                }
                when(content.downloadStatus) {
                    DownloadStatus.QUEUED.value -> {
                        itemRowBinding.deleteButton.visibility = View.INVISIBLE
                        itemRowBinding.playButton.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_download_cancel))
                        itemRowBinding.downloadingProgressLayout.visibility = View.INVISIBLE
                        itemRowBinding.playButton.setOnClickListener {
                            onClickListener?.onCancelClicked(content)
                        }
                        itemRowBinding.description.text = context.getString(R.string.queued)
                        itemRowBinding.description.setTextColor(ContextCompat.getColor(context, R.color.snack_bar_background))
                        if(!content.isMovie){
                            showEpisodeInfo(content)
                        }
                        else{
                            itemRowBinding.episodeInfo.visibility = View.GONE
                        }
                    }
                    DownloadStatus.IN_PROGRESS.value -> {
                        itemRowBinding.deleteButton.visibility = View.INVISIBLE
                        itemRowBinding.downloadingProgressLayout.visibility = View.VISIBLE
                        itemRowBinding.downloadingProgress.progress = content.downloadProgress
                        itemRowBinding.downloadingProgressText.text = String.format(
                            context.getString(R.string.text_download_status),
                            content.downloadProgress, "%")
                        itemRowBinding.playButton.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.bn_ic_play))
                        itemRowBinding.playButton.setOnClickListener {
                            onClickListener?.onPlayClicked(content)
                        }
                        itemRowBinding.downloadingProgress.setOnClickListener {
                            onClickListener?.onCancelClicked(content)
                        }
                        itemRowBinding.description.text = context.getString(R.string.button_play_now)
                        itemRowBinding.description.setTextColor(ContextCompat.getColor(context, R.color.snack_bar_background))
                        if(!content.isMovie){
                            showEpisodeInfo(content)
                        }else{
                            itemRowBinding.episodeInfo.visibility = View.GONE
                        }
                    }
                    DownloadStatus.DOWNLOADED.value -> {
                        itemRowBinding.episodeInfo.visibility = View.GONE
                        itemRowBinding.deleteButton.visibility = View.VISIBLE
                        itemRowBinding.downloadingProgressLayout.visibility = View.INVISIBLE
                        itemRowBinding.deleteButton.setOnClickListener {
                            if (viewEpisodes && !content.isMovie) {
                                onClickListener?.onDeleteClicked(content, true)
                            } else {
                                onClickListener?.onDeleteClicked(content, false)
                            }
                        }
                        itemRowBinding.playButton.setOnClickListener {
                            if (viewEpisodes && !content.isMovie) {
                                onClickListener?.onSeriesClicked(content)
                            } else {
                                onClickListener?.onPlayClicked(content)
                            }
                        }
                        val expiredSub = !subscriptionManager.hasValidSubscriptionForContent(content.contentProviderId,content.contentId)

                        if ((content.isMovie || (!viewEpisodes && !content.isMovie)) && expiredSub && !content.free) {
                            itemRowBinding.downloadItemExpired.visibility = View.VISIBLE
                            itemRowBinding.description.text =
                                context.getString(R.string.pack_expired)
                        } else {
                            itemRowBinding.downloadItemExpired.visibility = View.GONE
                            if (viewEpisodes && !content.isMovie) {
                                itemRowBinding.description.text = context.getString(R.string.view_all_seasons)
                                itemRowBinding.root.setOnClickListener {
                                    onClickListener?.onSeriesClicked(content)
                                }
                            } else {
                                """${content.durationInMts.toInt()} ${context.getString(R.string.minutes)} • ${
                                    AppUtils.toMB(
                                        content.getSize().toLong()
                                    )
                                }""".also { itemRowBinding.description.text = it }
                                itemRowBinding.root.setOnClickListener(null)
                            }
                            itemRowBinding.description.setTextColor(ContextCompat.getColor(context,
                                R.color.light_text_color))
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            if(!viewEpisodes && !content.isMovie){
                itemRowBinding.title.text = getEpisodeInfoOfSeries(content)
            }else {
                itemRowBinding.title.text = content.title
            }
        }

        private fun showEpisodeInfo(content: ContentDownload) {
            itemRowBinding.episodeInfo.visibility = View.VISIBLE
            itemRowBinding.episodeInfo.text = getEpisodeInfoOfSeries(content)
        }
    }

    private fun getEpisodeInfoOfSeries(content: ContentDownload) =
        content.season?.lowercase()?.replace("season", context.getString(R.string.season_suffix)) + " " + content.episode?.lowercase()
            ?.replace("episode", context.getString(R.string.episode_suffix))

    interface DownloadsListClickListener {
        fun onPlayClicked(content: ContentDownload)
        fun onDeleteClicked(content: ContentDownload, deleteAllEpisodes: Boolean)
        fun onCancelClicked(content: ContentDownload)
        fun onSeriesClicked(content: ContentDownload)
    }
}