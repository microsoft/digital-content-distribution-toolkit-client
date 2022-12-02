// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.microsoft.mobile.polymer.mishtu.ui.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.microsoft.mobile.polymer.mishtu.R
import com.microsoft.mobile.polymer.mishtu.databinding.ListItemTopContentBinding
import com.microsoft.mobile.polymer.mishtu.storage.entities.Content
import com.microsoft.mobile.polymer.mishtu.utils.AppUtils


class AutoScrollContentAdapter(
    private val dataModelList: List<Content>,
    private val context: Context,
    private var width: Int,
    private var height: Int,
    private var margin: Int
) : RecyclerView.Adapter<AutoScrollContentAdapter.ItemViewHolder>() {
    override fun onCreateViewHolder(
            parent: ViewGroup, viewType: Int
    ): AutoScrollContentAdapter.ItemViewHolder {
        val binding: ListItemTopContentBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context), R.layout.list_item_top_content, parent, false
        )
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder:AutoScrollContentAdapter.ItemViewHolder, position: Int) {
        val dataModel = dataModelList[position]
            holder.bind(dataModel)
    }

    override fun getItemCount(): Int {
        return dataModelList.size
    }

    inner class ItemViewHolder(var binding: ListItemTopContentBinding) : RecyclerView.ViewHolder(
            binding.root
    ) {
        fun bind(content: Content) {
            val mlp = binding.parent.layoutParams as MarginLayoutParams
            mlp.width = width
            mlp.setMargins(context.resources.getDimension(R.dimen.dp_12).toInt(),
                margin, 0,
                margin)

            val thumbSize = binding.topContentThumbnail.layoutParams as MarginLayoutParams
            thumbSize.height = height

            binding.title.visibility = View.GONE
            binding.playButton.visibility = View.GONE
            binding.gradientView.visibility = View.GONE

            AppUtils.loadImageWithRoundedCorners(context,
                content.getThumbnailImage(),
                binding.topContentThumbnail)
        }
    }

    fun setItemDimen(width: Int, height: Int) {
        this.width = width
        this.height = height
        notifyDataSetChanged()
    }
}