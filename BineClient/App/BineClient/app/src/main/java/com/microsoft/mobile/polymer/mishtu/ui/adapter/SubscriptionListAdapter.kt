package com.microsoft.mobile.polymer.mishtu.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.microsoft.mobile.polymer.mishtu.R
import com.microsoft.mobile.polymer.mishtu.databinding.ListItemSubscriptionBinding
import com.microsoft.mobile.polymer.mishtu.storage.entities.Content
import com.microsoft.mobile.polymer.mishtu.storage.entities.SubscriptionPack
import com.microsoft.mobile.polymer.mishtu.ui.views.AutoScrollContentView
import com.microsoft.mobile.polymer.mishtu.utils.AppUtils
import java.lang.Exception
import java.util.HashSet

import kotlin.collections.ArrayList

class SubscriptionListAdapter (
        private val context: Context,
        private val subscriptionList: ArrayList<SubscriptionPack>,
        private val subscriptionListAdapterListener: SubscriptionListAdapterListener?) : RecyclerView.Adapter<SubscriptionListAdapter.ViewHolder>() {

    private var canBuyPack = true
    private var visiblePosition = 0
    private val contentIdToContentMap = HashMap<String, Content>()

    @SuppressLint("NotifyDataSetChanged")
    fun setContent(content: HashMap<String, Content>) {
        contentIdToContentMap.clear()
        contentIdToContentMap.putAll(content)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: ListItemSubscriptionBinding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.list_item_subscription,
                parent,
                false
        )
        return ViewHolder(binding)
    }

    fun setSubscriptionList(subscriptionList: List<SubscriptionPack>) {
        Log.d("SubscriptionRepository", "${subscriptionList.size}")
        this.subscriptionList.clear()
        this.subscriptionList.addAll(subscriptionList)
        notifyDataSetChanged()
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (subscriptionList.isEmpty()) return
        val dataModel = subscriptionList[position]
        holder.bind(dataModel, position)
    }

    override fun getItemCount(): Int {
        return subscriptionList.size
    }

    inner class ViewHolder(var binding: ListItemSubscriptionBinding) :
            RecyclerView.ViewHolder(
                    binding.root
            ) {
        fun bind(slotModel: SubscriptionPack, position: Int) {
            binding.subscriptionTitle.text = slotModel.title //String.format(context.resources.getString(R.string.bn_pack_rs), slotModel.price)
            val contentCount = populateSubTitle(slotModel)
            binding.subscriptionSubTitle.text = contentCount +", "+ String.format(context.resources.getString(R.string.bn_pack_duration), slotModel.durationDays)

            AppUtils.loadImage(context, AppUtils.getContentProviderSquareLogoURL(slotModel.contentProviderId),
            binding.subscriptionIcon)

            binding.subscriptionIcon.visibility = View.VISIBLE
            binding.buttonSelectPack.visibility = View.VISIBLE
            binding.buttonSelectPack.isEnabled = canBuyPack
            binding.buttonSelectPack.text = String.format(context.resources.getString(R.string.buy_rs_pack), slotModel.price)
            binding.buttonSelectPack.setOnClickListener {
                subscriptionListAdapterListener?.onSelect(slotModel)
            }
            setAutoScrollData(slotModel)
            binding.subscriptionMoviesAutoscroll.addOnClickListener(object : AutoScrollContentView.OnClickListener{
                override fun onClick() {
                    visiblePosition = position
                    notifyDataSetChanged()
                }
            })


            if(visiblePosition == position){
                subscriptionListAdapterListener?.startAutoScroll(binding.subscriptionMoviesAutoscroll)
            }else{
                subscriptionListAdapterListener?.stopAutoScroll(binding.subscriptionMoviesAutoscroll)
            }
            binding.subscriptionTitle.setOnClickListener {
                subscriptionListAdapterListener?.aboutPack(slotModel,contentCount)
            }
            binding.subscriptionSubTitle?.setOnClickListener {
                subscriptionListAdapterListener?.aboutPack(slotModel,contentCount)
            }
        }
    }

    private fun ViewHolder.setAutoScrollData(slotModel: SubscriptionPack) {
        slotModel.contentList?.let {
            binding.subscriptionMoviesAutoscroll.setData(it)
        }
    }

    private fun populateSubTitle(sub: SubscriptionPack): String {
        var subTitleString = ""
        try {
            var movieCount = 0
            val seriesSet = HashSet<String>()
            sub.contentIdList?.forEach { conId ->
                val conObj = contentIdToContentMap[conId]
                conObj?.let { con ->
                    if (con.isMovie) {
                        movieCount++
                    } else {
                        con.name?.let { it1 -> seriesSet.add(it1) }
                    }
                }
            }
            subTitleString = if (movieCount > 0) {
                if (seriesSet.size > 0)
                    context.getString(R.string.watch_x_movies_series, movieCount, seriesSet.size)
                else
                    context.getString(R.string.watch_x_movies, movieCount)
            } else if (seriesSet.size > 0) {
                context.getString(R.string.watch_x_series, seriesSet.size)
            } else {
                context.getString(R.string.watch_100s_of_movies_amp_serials_and)
            }
        } catch (e: Exception) {
            Log.d("Exception ", e.toString())
        }
        return subTitleString
    }


    interface SubscriptionListAdapterListener {
        fun onSelect(subscriptionPack: SubscriptionPack)
        fun startAutoScroll(autoScrollContentView: AutoScrollContentView)
        fun stopAutoScroll(autoScrollContentView: AutoScrollContentView)
        fun aboutPack(subscriptionPack: SubscriptionPack, subTitle: String)
    }
}