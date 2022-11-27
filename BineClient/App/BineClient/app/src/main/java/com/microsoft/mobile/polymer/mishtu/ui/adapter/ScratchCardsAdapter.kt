package com.microsoft.mobile.polymer.mishtu.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView

import com.microsoft.mobile.polymer.mishtu.R
import com.microsoft.mobile.polymer.mishtu.databinding.ListItemScratchCardBinding
import com.msr.bine_sdk.cloud.models.IncentiveEvent

class ScratchCardsAdapter(val context: Context,
                          val itemClickListener: ItemClickListener): RecyclerView.Adapter<ScratchCardsAdapter.ViewHolder>() {

    val incentiveEvents: ArrayList<IncentiveEvent> = arrayListOf()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val itemBinding: ListItemScratchCardBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.list_item_scratch_card,
            parent, false)
        return ViewHolder(itemBinding)
    }

    fun setEvents(events: List<IncentiveEvent>) {
        incentiveEvents.clear()
        incentiveEvents.addAll(events)
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindData()
    }

    override fun getItemCount(): Int {
        return incentiveEvents.size
    }

    class ViewHolder(var binding: ListItemScratchCardBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindData(
            /*incentiveEvents: IncentiveEvent,
            itemClickListener: ItemClickListener,
            position: Int*/
        ) {

            /*binding.scratchCoins.text = incentiveEvents.toString()
            binding.cardNotScratchedLayout.visibility =
                if (incentiveEvents.isScratched) View.INVISIBLE else View.VISIBLE
            if (!incentiveEvents.isScratched) {
                binding.root.setOnClickListener {
                    itemClickListener.onCardClicked(incentiveEvents, position)
                }
            }*/
        }
    }

    interface ItemClickListener {
        fun onCardClicked(event: IncentiveEvent, position: Int)
    }
}