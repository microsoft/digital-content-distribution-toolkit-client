// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.microsoft.mobile.polymer.mishtu.ui.adapter

import android.R.attr.left
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.microsoft.mobile.polymer.mishtu.R
import com.microsoft.mobile.polymer.mishtu.databinding.ListItemOfferPacksBinding
import com.microsoft.mobile.polymer.mishtu.storage.SharedPreferenceStore
import com.microsoft.mobile.polymer.mishtu.storage.entities.Content
import com.microsoft.mobile.polymer.mishtu.storage.entities.SubscriptionPack
import com.microsoft.mobile.polymer.mishtu.ui.views.AutoScrollContentView
import com.microsoft.mobile.polymer.mishtu.utils.AppUtils


class OffersListAdapter(
    private var context: Context,
    private var offerClickListener: OfferClickListener,
): RecyclerView.Adapter<OffersListAdapter.ViewHolder>() {
    private val contents = arrayListOf<Content>()
    private val subscriptionPacks = arrayListOf<SubscriptionPack>()
    private var totalCoins = 0
    private var recyclerView: RecyclerView? = null
    private var visiblePosition = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OffersListAdapter.ViewHolder {
        val binding: ListItemOfferPacksBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context), R.layout.list_item_offer_packs, parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OffersListAdapter.ViewHolder, position: Int) {
        val dataModel = subscriptionPacks[position]
        holder.bind(dataModel, position)
    }

    override fun getItemCount(): Int {
        return subscriptionPacks.size
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setContent(content: List<Content>) {
        contents.clear()
        contents.addAll(content)
        notifyDataSetChanged()
    }

    fun setSubscriptions(subscriptionPack: List<SubscriptionPack>) {
        subscriptionPacks.clear()
        subscriptionPacks.addAll(subscriptionPack)
        notifyDataSetChanged()
    }

    fun setTotalCoins(coins: Int) {
        totalCoins = coins
        notifyDataSetChanged()
    }

    inner class ViewHolder(var binding: ListItemOfferPacksBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(subscriptionPack: SubscriptionPack, position: Int) {
            var cpName = SharedPreferenceStore.getInstance().get(SharedPreferenceStore.PREFIX_CONTENT_PROVIDER+subscriptionPack.contentProviderId)
            binding.offersPackDuration.text = String.format(context.getString(R.string.n_days_pack), subscriptionPack.durationDays)
            AppUtils.loadImage(context, AppUtils.getContentProviderSquareLogoURL(subscriptionPack.contentProviderId),binding.providerLogo)

            binding.autoScrollView.setData(if(subscriptionPack.subscriptionType == SubscriptionPack.PackType.SVOD.value)
            {
                contents.filter { con-> con.contentProviderId == subscriptionPack.contentProviderId }.distinctBy { it.name }
            }
            else{
                contents.filter { con-> subscriptionPack.contentIdList!!.contains(con.contentId) }.distinctBy { it.name }
            })

            val isEnabled = totalCoins >= subscriptionPack.redemptionValue
            binding.offerDisabled.visibility =
                if (isEnabled) View.GONE
                else View.VISIBLE

            binding.coinsButton.text = subscriptionPack.redemptionValue.toString()
            //binding.coinsButton.isEnabled = canRedeem

            if (isEnabled) {
                binding.autoScrollView.addOnClickListener(object :
                    AutoScrollContentView.OnClickListener {
                    override fun onClick() {
                        //showOfferDetails(subscriptionPack)
                        visiblePosition = position
                        notifyDataSetChanged()
                    }
                })

                binding.root.setOnClickListener {
                    showOfferDetails(subscriptionPack)
                }
                binding.coinsButton.setOnClickListener {
                    redeemOffer(subscriptionPack)
                }

                if (position == visiblePosition) {
                    offerClickListener.startAutoScroll(binding.autoScrollView)
                }
                else {
                    offerClickListener.stopAutoScroll(binding.autoScrollView)
                }
            }
        }
    }


    private fun showOfferDetails(subscriptionPack: SubscriptionPack) {
        offerClickListener.showOfferClicked(subscriptionPack)
    }

    private fun redeemOffer(subscriptionPack: SubscriptionPack) {
        offerClickListener.onRedeemClicked(subscriptionPack)
    }

    interface OfferClickListener {
        fun onRedeemClicked(subscriptionPack: SubscriptionPack)
        fun showOfferClicked(subscriptionPack: SubscriptionPack)
        fun startAutoScroll(autoScrollContentView: AutoScrollContentView)
        fun stopAutoScroll(autoScrollContentView: AutoScrollContentView)
    }
}