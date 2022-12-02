// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.microsoft.mobile.polymer.mishtu.ui.adapter


import android.content.Context
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.microsoft.mobile.polymer.mishtu.R
import com.microsoft.mobile.polymer.mishtu.databinding.ItemLanaguageBottomSheetBinding
import com.microsoft.mobile.polymer.mishtu.kaizala_utils.LanguageUtils
import java.util.*
import kotlin.collections.ArrayList


class BottomSheetLanguagesListAdapter(
    private val context: Context,
    private val listener: OnLanguageItemClickListener,
    private var selectedLanguage: String = "",
    private var languageFilterList: ArrayList<String>,
    private var languageList: ArrayList<String>,
    ) : RecyclerView.Adapter<BottomSheetLanguagesListAdapter.ViewHolder>(), Filterable {
    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): ViewHolder {
        val binding: ItemLanaguageBottomSheetBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_lanaguage_bottom_sheet,
            parent,
            false
        )

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val dataModel = languageFilterList[position]
        holder.bind(dataModel, position)
    }

    override fun getItemCount(): Int {
        return languageFilterList.size
    }

    inner class ViewHolder(var binding: ItemLanaguageBottomSheetBinding) :
        RecyclerView.ViewHolder(
            binding.root
        ) {
        fun bind(slotModel: String, position: Int) {

            val list = slotModel.split(":")
            val res: Resources = context.resources
            val id: Int = res.getIdentifier(
                list[1], "drawable",
                context.packageName
            )
            binding.icon.setImageDrawable(ContextCompat.getDrawable(context, id))

            val languageCode = LanguageUtils.getLanguageCode(list[0])
            binding.title.text = LanguageUtils.getAppDisplayLanguage(languageCode)//context.resources.getString(stringId)
            binding.subTitle.text = list[0]

            if (selectedLanguage == languageCode) {
                binding.check.visibility = View.VISIBLE
                binding.parent.setBackgroundColor(ContextCompat.getColor(context, R.color.languageSelected))
            } else {
                binding.check.visibility = View.GONE
                binding.parent.setBackgroundColor(ContextCompat.getColor(context, R.color.white))
            }

            binding.parent.setOnClickListener {
                selectedLanguage = languageCode
                listener.onLanguageClicked(languageCode)
                notifyDataSetChanged()
            }
        }
    }

    interface OnLanguageItemClickListener {
        fun onLanguageClicked(code: String)
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                val charString = charSequence.toString()
                if (charString.isEmpty()) {
                    languageFilterList = languageList
                } else {
                    val filteredList: ArrayList<String> = ArrayList<String>()
                    for (hub in languageList) {
                        // name match condition. this might differ depending on your requirement
                        // here we are looking for name or phone number match
                        if (hub.lowercase(Locale.getDefault())
                                .contains(charString.lowercase(Locale.getDefault()))) {
                            filteredList.add(hub)
                        }
                    }
                    languageFilterList = filteredList
                }
                val filterResults = FilterResults()
                filterResults.values = languageFilterList
                return filterResults
            }

            override fun publishResults(charSequence: CharSequence, filterResults: FilterResults) {
                languageFilterList = filterResults.values as ArrayList<String>
                notifyDataSetChanged()
            }
        }
    }

    fun setSelectedLang(selectedLanguage: String) {
        this.selectedLanguage = selectedLanguage
        notifyDataSetChanged()
    }
}