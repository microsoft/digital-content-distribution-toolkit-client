// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.microsoft.mobile.polymer.mishtu.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.content.res.Resources
import android.graphics.BitmapFactory

import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.microsoft.mobile.polymer.mishtu.R
import com.microsoft.mobile.polymer.mishtu.databinding.ListItemServicesBinding
import com.microsoft.mobile.polymer.mishtu.utils.BNConstants

class ServiceGridAdapter(var context: Context, private var itemClickListener: ItemClickListener) :
        RecyclerView.Adapter<ServiceGridAdapter.ServiceViewHolder>(){

    override fun getItemCount(): Int {
        return BNConstants.Services.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceViewHolder {
        val itemBinding: ListItemServicesBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()),
                R.layout.list_item_services,
                parent, false)
        return ServiceViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: ServiceViewHolder, position: Int) {
        val folder = BNConstants.Services[position]
        holder.bindData(context, folder, itemClickListener, position)
    }

    class ServiceViewHolder(var binding: ListItemServicesBinding) :
            RecyclerView.ViewHolder(binding.root) {
        fun bindData(context: Context,
                     service: String,
                     itemClickListener: ItemClickListener, position: Int) {
            val list = service.split(":")

            val res: Resources = context.resources
            val id: Int = res.getIdentifier(list[1], "drawable",
                    context.packageName)
            val src = BitmapFactory.decodeResource(res, id)
            val dr = RoundedBitmapDrawableFactory.create(res, src)
            dr.cornerRadius = context.resources.getDimension(R.dimen.dp_12)
            binding.discoverServiceImage.setImageDrawable(dr)

            val stringId: Int = context.resources.getIdentifier(list[1], "string",
                    context.packageName)
            binding.discoverServiceTitle.text = context.getString(stringId)

            val stringDescId: Int = context.resources.getIdentifier(list[2], "string",
                    context.packageName)
            binding.discoverServiceDescription.text = context.getString(stringDescId)

            binding.discoverServiceImage.setOnClickListener {
                itemClickListener.onServiceClicked(service, position)
            }
        }
    }

    interface ItemClickListener {
        fun onServiceClicked(service: String, position: Int)
    }
}
