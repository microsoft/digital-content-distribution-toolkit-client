// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.microsoft.mobile.polymer.mishtu.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.microsoft.mobile.polymer.mishtu.R
import com.microsoft.mobile.polymer.mishtu.databinding.ListItemCotentViewAllBinding
import com.microsoft.mobile.polymer.mishtu.storage.entities.Content
import com.microsoft.mobile.polymer.mishtu.utils.AppUtils
import java.lang.Appendable

class SubscriptionMovieAdapter(private val context: Context,
                               private val contentList: ArrayList<Content>,
                               private val subscriptionMovieAdapterListener: SubscriptionMovieAdapterListener): RecyclerView.Adapter<SubscriptionMovieAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: ListItemCotentViewAllBinding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.list_item_cotent_view_all,
                parent,
                false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(contentList[position])
    }

    override fun getItemCount(): Int {
       return contentList.size
    }

    fun setDataList(contentList: List<Content>){
        this.contentList.clear()
        this.contentList.addAll(contentList)
        notifyDataSetChanged()
    }

    inner class ViewHolder(var binding: ListItemCotentViewAllBinding):RecyclerView.ViewHolder(binding.root){
        fun bind(slotModel: Content){
            val mlpBanner = binding.banner.layoutParams as ViewGroup.MarginLayoutParams
            mlpBanner.height = context.resources.getDimension(R.dimen.dp_95).toInt()
            mlpBanner.width = context.resources.getDimension(R.dimen.dp_73).toInt()
            binding.banner.setImageDrawable(context.getDrawable(R.drawable.rounded_corner_shape_4dp))
            AppUtils.loadImageWithRoundedCorners(context,slotModel.getThumbnailImage(),binding.banner)

            

            binding.providerLogo.visibility = View.GONE
            binding.freeTag.visibility = View.GONE
            binding.playButton.visibility = View.GONE
            binding.title.visibility = View.GONE
            binding.infoButton.visibility = View.VISIBLE

            binding.infoButton.setOnClickListener {
                subscriptionMovieAdapterListener.onItemClicked(slotModel)
            }
            binding.parent.setOnClickListener {
                subscriptionMovieAdapterListener.onItemClicked(slotModel)
            }

        }
    }



    interface SubscriptionMovieAdapterListener{
        fun onItemClicked(content: Content)
    }

}