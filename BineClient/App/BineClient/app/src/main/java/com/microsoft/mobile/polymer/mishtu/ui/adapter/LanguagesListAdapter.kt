// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.microsoft.mobile.polymer.mishtu.ui.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import android.view.ViewGroup.MarginLayoutParams
import androidx.recyclerview.widget.RecyclerView

import android.content.res.Resources
import androidx.core.content.ContextCompat
import com.microsoft.mobile.polymer.mishtu.R
import com.microsoft.mobile.polymer.mishtu.databinding.ListItemFooterBinding

import com.microsoft.mobile.polymer.mishtu.databinding.ListItemLanguageBinding
import com.microsoft.mobile.polymer.mishtu.kaizala_utils.LanguageUtils
import com.microsoft.mobile.polymer.mishtu.utils.BNConstants

class LanguagesListAdapter(
    private val context: Context,
    private val width: Int,
    private val height: Int,
    private val marginStart: Int,
    private val isGridView: Boolean,
    private var isViewAllVisible: Boolean,
    private val listener: OnLanguageItemClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_FOOTER = 1
    private val TYPE_ITEM = 0

    override fun onCreateViewHolder(
            parent: ViewGroup, viewType: Int
    ): RecyclerView.ViewHolder {
        if (viewType == TYPE_ITEM) {
            val binding: ListItemLanguageBinding = DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.list_item_language,
                    parent,
                    false
            )
            return LangViewHolder(binding)
        }

        val binding: ListItemFooterBinding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.context), R.layout.list_item_footer, parent, false)
        return ViewHolderFooter(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val dataModel = BNConstants.Languages[position]
        if (holder is LangViewHolder) {
            holder.bind(dataModel, position)
        } else if (holder is ViewHolderFooter) {
            holder.bind()
        }
    }

    override fun getItemCount(): Int {
        if (isViewAllVisible) {
            return BNConstants.Languages.take(5).size
        }
        return BNConstants.Languages.size
    }

    override fun getItemViewType(position: Int): Int {
        super.getItemViewType(position)
        if (isViewAllVisible && position == BNConstants.CONTENT_VIEW_ALL_POSITION) {
            return TYPE_FOOTER
        }
        return TYPE_ITEM
    }

    inner class ViewHolderFooter(var binding: ListItemFooterBinding) : RecyclerView.ViewHolder(
            binding.root
    ) {
        fun bind() {
            Log.e("here", "ddd")
            val mlp = binding.root.layoutParams as MarginLayoutParams
            if (isGridView) {
                mlp.width = ViewGroup.LayoutParams.MATCH_PARENT
            } else {
                mlp.width = width
                mlp.height = height
            }

            mlp.setMargins(marginStart, 0, 0, 0)
            binding.banner.setImageResource(R.drawable.ic_gradient_14)
            binding.gradientView.background = null
            binding.root.setOnClickListener {
                listener.onViewAllCLicked()
            }
        }
    }


    inner class LangViewHolder(var binding: ListItemLanguageBinding) : RecyclerView.ViewHolder(
            binding.root
    ) {
        fun bind(slotModel: String, position: Int) {
            val mlp = binding.root.layoutParams as MarginLayoutParams
            if (isGridView) {
                mlp.width = ViewGroup.LayoutParams.MATCH_PARENT
            } else {
                mlp.width = width
            }
            mlp.height = height
            mlp.setMargins(marginStart, 0, 0, 0)
            val list = slotModel.split(":")

            val res: Resources = context.resources
            val id: Int = res.getIdentifier(list[1], "drawable",
                    context.packageName)
            val gradientId : Int = res.getIdentifier("ic_gradient_$position", "drawable",
                context.packageName)
            binding.banner.setImageDrawable(ContextCompat.getDrawable(context, gradientId))
            binding.titleIcon.setImageDrawable(ContextCompat.getDrawable(context, id))

            binding.language.text = LanguageUtils.getAppDisplayLanguage(BNConstants.LanguagesCode[position])//context.resources.getString(stringId)

            binding.root.setOnClickListener {
                listener.onLanguageClicked(list[0])
            }
        }
    }


    interface OnLanguageItemClickListener {
        fun onLanguageClicked(language: String)
        fun onViewAllCLicked()
    }
}