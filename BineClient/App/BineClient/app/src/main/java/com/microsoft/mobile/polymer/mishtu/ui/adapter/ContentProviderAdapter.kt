package com.microsoft.mobile.polymer.mishtu.ui.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.microsoft.mobile.polymer.mishtu.BuildConfig
import com.microsoft.mobile.polymer.mishtu.R
import com.microsoft.mobile.polymer.mishtu.databinding.ItemContentProviderBinding
import com.microsoft.mobile.polymer.mishtu.storage.entities.ContentProvider
import com.microsoft.mobile.polymer.mishtu.ui.fragment.sheet.BottomSheetContentProvider
import com.microsoft.mobile.polymer.mishtu.utils.AppUtils
import java.util.ArrayList

class ContentProviderAdapter(
    val context: Context,
    val onItemClickListener: onItemClickListner,
    val bottomSheetContentProvider: BottomSheetContentProvider?
): RecyclerView.Adapter<ContentProviderAdapter.ViewHolder>() {

    private val dataModelList = ArrayList<ContentProvider>()


    inner class ViewHolder(private var itemContentProviderBinding: ItemContentProviderBinding) :
        RecyclerView.ViewHolder(itemContentProviderBinding.root){

            fun bind(dataModel: ContentProvider) {
                Log.d("contentProviderList3",dataModel.name)
                AppUtils.loadImage(context, AppUtils.getContentProviderSquareLogoURL(dataModel.id), itemContentProviderBinding.imageView)
                itemContentProviderBinding.root.setOnClickListener {
                    if(bottomSheetContentProvider != null){
                        bottomSheetContentProvider.dismiss()
                    }
                    onItemClickListener.onItemClicked(dataModel.id)
                }
            }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemContentProviderBinding =
            DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_content_provider,parent,false)

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val dataModel = dataModelList[position]
        holder.bind(dataModel)
    }

    override fun getItemCount(): Int {
        return dataModelList.size;
    }

    fun setData(dataList: List<ContentProvider>) {
        Log.d("contentProviderList2", dataList.toString())
        dataModelList.clear()
        dataModelList.addAll(dataList);
        notifyDataSetChanged()
    }
    interface onItemClickListner{
        fun onItemClicked(contentProvider: String)
    }
}