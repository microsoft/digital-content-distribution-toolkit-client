package com.microsoft.mobile.polymer.mishtu.ui.adapter


import android.content.Context
import android.content.res.Resources
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.microsoft.mobile.polymer.mishtu.R

import com.microsoft.mobile.polymer.mishtu.databinding.ListItemFooterBinding
import com.microsoft.mobile.polymer.mishtu.databinding.ListItemGenresBinding
import com.microsoft.mobile.polymer.mishtu.utils.BNConstants


class GeneresListAdapter(
    private val context: Context,
    private val width: Int,
    private val height: Int,
    private val marginStart: Int,
    private val isGridView: Boolean,
    private var isViewAllVisible: Boolean,
    private val listener: OnGenresItemClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val TYPE_FOOTER = 1
    private val TYPE_ITEM = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == TYPE_ITEM) {
            val binding: ListItemGenresBinding = DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context), R.layout.list_item_genres, parent, false)
            return GeneresViewHolder(binding)
        }

        val binding: ListItemFooterBinding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.context), R.layout.list_item_footer, parent, false)
        return ViewHolderFooter(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val dataModel = BNConstants.Genres[position]
        if (holder is GeneresViewHolder) {
            holder.bind(dataModel, position)
        } else if (holder is ViewHolderFooter) {
            holder.bind()
        }
    }

    override fun getItemViewType(position: Int): Int {
        super.getItemViewType(position)
        if (isViewAllVisible && position == BNConstants.CONTENT_VIEW_ALL_POSITION) {
            return TYPE_FOOTER
        }
        return TYPE_ITEM
    }


    override fun getItemCount(): Int {
        if (isViewAllVisible) {
            return BNConstants.Genres.take(5).size
        }
        return BNConstants.Genres.size
    }

    inner class GeneresViewHolder(var binding: ListItemGenresBinding) : RecyclerView.ViewHolder(
            binding.root
    ) {
        fun bind(slotModel: String, position: Int) {
            Log.e("here", "ddd")
            val mlp = binding.root.layoutParams as ViewGroup.MarginLayoutParams
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
            val gradientPos = Math.abs(position - 11)
            val gradientId : Int = res.getIdentifier("ic_gradient_$gradientPos", "drawable",
                context.packageName)
            binding.banner.setImageDrawable(ContextCompat.getDrawable(context, gradientId))

            binding.titleIcon.setImageDrawable(ContextCompat.getDrawable(context, id))

            val stringId: Int = context.resources.getIdentifier(list[1], "string",
                    context.packageName)

            binding.language.text = context.resources.getString(stringId)

            binding.root.setOnClickListener {
                listener.onGenresClicked(list[0])
            }
        }
    }

    inner class ViewHolderFooter(var binding: ListItemFooterBinding) : RecyclerView.ViewHolder(
            binding.root
    ) {
        fun bind() {
            Log.e("here", "ddd")
            val mlp = binding.root.layoutParams as ViewGroup.MarginLayoutParams
            if (isGridView) {
                mlp.width = ViewGroup.LayoutParams.MATCH_PARENT
            } else {
                mlp.width = width
            }

            mlp.height = height
            mlp.setMargins(marginStart, 0, 0, 0)

            binding.banner.setImageResource(R.drawable.ic_gradient_14)
            binding.gradientView.background = null
            binding.root.setOnClickListener {
                listener.onViewAllCLicked()
            }
        }
    }

    interface OnGenresItemClickListener {
        fun onGenresClicked(genres: String)
        fun onViewAllCLicked()
    }
}