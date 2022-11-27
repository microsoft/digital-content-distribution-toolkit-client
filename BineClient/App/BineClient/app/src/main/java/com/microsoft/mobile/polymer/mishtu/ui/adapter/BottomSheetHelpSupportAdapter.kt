package com.microsoft.mobile.polymer.mishtu.ui.adapter


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView

import com.microsoft.mobile.polymer.mishtu.R
import com.microsoft.mobile.polymer.mishtu.databinding.ListItemHelpSupportBinding
import java.util.*
import kotlin.collections.ArrayList


class BottomSheetHelpSupportAdapter(
        private var issueFilteredList: ArrayList<String>,
        private var issueList: ArrayList<String>,

        ) : RecyclerView.Adapter<BottomSheetHelpSupportAdapter.ViewHolder>(), Filterable {

    private var selectedPosition = -1

    override fun onCreateViewHolder(
            parent: ViewGroup, viewType: Int
    ): ViewHolder {
        val binding: ListItemHelpSupportBinding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.list_item_help_support,
                parent,
                false
        )

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val dataModel = issueFilteredList[position]
        holder.bind(dataModel, position)
    }

    override fun getItemCount(): Int {
        return issueFilteredList.size
    }

    inner class ViewHolder(var binding: ListItemHelpSupportBinding) :
            RecyclerView.ViewHolder(
                    binding.root
            ) {
        fun bind(title: String, position: Int) {
            binding.title.text = title
            if (selectedPosition == position) {
                binding.videoView.visibility = View.VISIBLE
                binding.view.visibility = View.VISIBLE
                binding.play.visibility = View.VISIBLE
                binding.btnChat.visibility = View.VISIBLE
                binding.title.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_bn_ic_arrow_down, 0)
            } else {
                binding.videoView.visibility = View.GONE
                binding.play.visibility = View.GONE
                binding.view.visibility = View.GONE
                binding.btnChat.visibility = View.GONE
                binding.title.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_bn_ic_arrow_down, 0)
            }
            binding.title.setOnClickListener {
                selectedPosition = if(selectedPosition==position){
                    -1
                }else{
                    position
                }
                notifyDataSetChanged()
            }
        }
    }

    interface OnOrderItemClickedListener {
        fun onOrderItemClicked(position: Int)
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                val charString = charSequence.toString()
                if (charString.isEmpty()) {
                    issueFilteredList = issueList
                } else {
                    val filteredList: ArrayList<String> = ArrayList<String>()
                    for (issue in issueList) {
                        // name match condition. this might differ depending on your requirement
                        // here we are looking for name or phone number match
                        if (issue.uppercase(Locale.getDefault())
                                        .contains(charString.lowercase(Locale.getDefault()))) {
                            filteredList.add(issue)
                        }
                    }
                    issueFilteredList = filteredList
                }
                val filterResults = FilterResults()
                filterResults.values = issueFilteredList
                return filterResults
            }

            override fun publishResults(charSequence: CharSequence, filterResults: FilterResults) {
                issueFilteredList = filterResults.values as ArrayList<String>
                notifyDataSetChanged()
            }
        }
    }

}