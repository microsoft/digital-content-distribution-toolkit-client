package com.microsoft.mobile.polymer.mishtu.ui.adapter

import android.content.Context
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.microsoft.mobile.polymer.mishtu.R

import com.microsoft.mobile.polymer.mishtu.databinding.ListItemFwCategoryBinding
import com.microsoft.mobile.polymer.mishtu.utils.BNConstants

class FireworkCategoryAdapter(var context: Context, var itemClickListener: ItemClickListener) :
        RecyclerView.Adapter<FireworkCategoryAdapter.FwCategoryViewHolder>(){

    override fun getItemCount(): Int {
        return BNConstants.FwCategories.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FwCategoryViewHolder {
        val itemBinding: ListItemFwCategoryBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context),
                R.layout.list_item_fw_category,
                parent, false)
        return FwCategoryViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: FwCategoryViewHolder, position: Int) {
        val category = BNConstants.FwCategories[position]
        holder.bindData(context, category, itemClickListener)
    }

    class FwCategoryViewHolder(var binding: ListItemFwCategoryBinding) :
            RecyclerView.ViewHolder(binding.root) {
        fun bindData(
            context: Context,
            service: String,
            itemClickListener: ItemClickListener
        ) {
            val list = service.split(":")

            val res: Resources = context.resources
            val id: Int = res.getIdentifier(list[1], "drawable",
                    context.packageName)
            /*val src = BitmapFactory.decodeResource(res, id)
            val dr = RoundedBitmapDrawableFactory.create(res, src)
            dr.cornerRadius = 16.0f*/
            binding.fwCategoryImage.setImageDrawable(ContextCompat.getDrawable(context, id))

            val stringId: Int = context.resources.getIdentifier(list[1], "string",
                    context.packageName)
            binding.fwCategoryTitle.text = context.getString(stringId)

            binding.fwCategoryImage.setOnClickListener {
                itemClickListener.onCategoryClicked(list[0], list[2])
            }
        }
    }

    interface ItemClickListener {
        fun onCategoryClicked(categoryName: String, categoryID: String)
    }
}