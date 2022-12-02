// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.microsoft.mobile.polymer.mishtu.ui.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.microsoft.mobile.polymer.mishtu.R
import com.microsoft.mobile.polymer.mishtu.databinding.ViewPackPromotionBannerBinding
import com.microsoft.mobile.polymer.mishtu.storage.entities.ContentDownload
import com.microsoft.mobile.polymer.mishtu.ui.activity.ViewAllContentActivity
import com.microsoft.mobile.polymer.mishtu.ui.adapter.TopContentAdapter
import com.microsoft.mobile.polymer.mishtu.ui.fragment.ContentFragment
import com.microsoft.mobile.polymer.mishtu.utils.AppUtils
import com.microsoft.mobile.polymer.mishtu.utils.OnItemClickListener

class PackPromotionBanner @JvmOverloads constructor(
    context: Context,
    val contentProviderId: String,
    val type: ContentFragment.TYPE,
    attrs: AttributeSet? = null,
    defStyle: Int = 0) : FrameLayout(context, attrs, defStyle), TopContentAdapter.OnMovieClickListener {

    private var clickListener: OnClickListener? = null
    private lateinit var layoutManager: LinearLayoutManager
    lateinit var adapter: TopContentAdapter
    private lateinit var binding: ViewPackPromotionBannerBinding
    companion object{
        val CALLED_FROM = "PackPromotionBanner"
    }

    init {
        initView()
    }

    private fun initView() {
        val inflater = context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        binding = ViewPackPromotionBannerBinding.inflate(inflater, this, true)

        adapter = TopContentAdapter(
            context,
            this,
            context.resources.getDimension(R.dimen.dp_140).toInt(),
            context.resources.getDimension(R.dimen.dp_200).toInt(),
            context.resources.getDimension(R.dimen.dp_12).toInt(),
            isGridView = false,
            isViewAllVisible = true,
            titleVisible = true,
            gradientVisible = true,
            playVisible = true
        )
        adapter.packPromotion = true
        layoutManager = LinearLayoutManager(context)
        layoutManager.orientation = RecyclerView.HORIZONTAL
        binding.recyclerView.layoutManager = layoutManager
        binding.packPromoButton.setOnClickListener { clickListener?.onPayAtStoreClicked() }
        binding.recyclerView.adapter = adapter

        val cardViewMarginParams = binding.parentCardView.layoutParams as MarginLayoutParams
        cardViewMarginParams.setMargins(0, 0, 0, resources.getDimension(R.dimen.dp_8).toInt())
        binding.parentCardView.layoutParams = cardViewMarginParams
    }

    fun addOnClickListener(listener: OnClickListener) {
        clickListener = listener
    }

    fun setData(bookingModels: List<ContentDownload>, contentProviderId: String) {
        if (bookingModels.isEmpty()) {
            binding.parentCardView.visibility = View.GONE
            return
        }
        binding.parentCardView.visibility = View.VISIBLE
        AppUtils.loadImage(context, AppUtils.getContentProviderSquareLogoURL(contentProviderId)
            ,binding.packPromoOttLogo)
        if (bookingModels.isNotEmpty()) {
            AppUtils.loadImageWithRoundedCorners(context,
                bookingModels[0].getThumbnailImage(),
                binding.packImage1)
            if (bookingModels.size > 1) {
                AppUtils.loadImageWithRoundedCorners(context,
                    bookingModels[1].getThumbnailImage(),
                    binding.packImage2)
                if (bookingModels.size > 2) {
                    AppUtils.loadImageWithRoundedCorners(context,
                        bookingModels[2].getThumbnailImage(),
                        binding.packImage3)
                }
            }
        }
        adapter.submitList(bookingModels)
    }

    fun setHubConnected(connected: Boolean) {
        adapter.hubConnected = connected
    }

    fun setActiveSubscription(active: Boolean) {
        adapter.hasActiveSubscription = active
        if (active) {
            binding.packPromoButton.visibility = View.GONE
            val title = if(type == ContentFragment.TYPE.MOVIES) context.getString(R.string.pack_promo_active_pack)
            else context.getString(R.string.pack_promo_active_pack_series)
            binding.packPromoPackTitle.text = title
        }
        else {
            binding.packPromoButton.visibility = View.VISIBLE
            val title = if(type == ContentFragment.TYPE.MOVIES) context.getString(R.string.more_than_100)
            else context.getString(R.string.more_than_100_series)
            binding.packPromoPackTitle.text = title
        }
    }

    override fun onMovieClicked(content: ContentDownload) {
        clickListener?.onItemCLicked(content)
    }

    override fun onViewAllCLicked(content: ContentDownload) {
        val additionalAttrMap = HashMap<String,Any?>()
        additionalAttrMap[ViewAllContentActivity.EXTRA_CONTENT] = content
        additionalAttrMap[ViewAllContentActivity.EXTRA_CALLED_FROM] = PackPromotionBanner.CALLED_FROM
        additionalAttrMap[ViewAllContentActivity.EXTRA_HUB_CONNECTED] = adapter.hubConnected
        val title = if(content.isMovie) context.getString(R.string.bn_paid_movies) else context.getString(R.string.bn_paid_series)
        AppUtils.startViewAllContentActivity(
            context,
            false,
            adapter.hubConnected,
            title,
            adapter.hasActiveSubscription
        ,additionalAttrMap)
    }

    interface OnClickListener: OnItemClickListener {
        fun onPayAtStoreClicked()
    }
}