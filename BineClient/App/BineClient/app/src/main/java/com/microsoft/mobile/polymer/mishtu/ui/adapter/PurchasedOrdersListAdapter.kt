package com.microsoft.mobile.polymer.mishtu.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.microsoft.mobile.polymer.mishtu.R
import com.microsoft.mobile.polymer.mishtu.databinding.ListItemOrderBottomSheetBinding
import com.microsoft.mobile.polymer.mishtu.storage.entities.Content
import com.microsoft.mobile.polymer.mishtu.utils.AppUtils
import com.microsoft.mobile.polymer.mishtu.utils.BOConverter
import com.msr.bine_sdk.cloud.models.Order

import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class PurchasedOrdersListAdapter(
    private val context: Context,
    private val listener: OnOrderItemClickedListener,
    private var ordersFilterList: List<Order>,
    private var ordersList: List<Order>,
    private var isSelectable: Boolean
        ) : RecyclerView.Adapter<PurchasedOrdersListAdapter.ViewHolder>(), Filterable {

    private var selectedIndex = -1
    val contentIdToContentMap = HashMap<String,Content>()

    @SuppressLint("NotifyDataSetChanged")
    fun setContent(content: HashMap<String, Content>) {
        contentIdToContentMap.clear()
        contentIdToContentMap.putAll(content)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: ListItemOrderBottomSheetBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.list_item_order_bottom_sheet,
            parent,
            false
        )

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val dataModel = ordersFilterList[position]
        holder.bind(dataModel, position)
    }

    override fun getItemCount(): Int {
        return ordersFilterList.size
    }

    inner class ViewHolder(var binding: ListItemOrderBottomSheetBinding) :
        RecyclerView.ViewHolder(
            binding.root
        ) {
        fun bind(slotModel: Order, position: Int) {
            binding.check.setOnClickListener {
                listener.onOrderItemClicked(slotModel)
                selectedIndex = position
                notifyDataSetChanged()
            }

            binding.title.text = slotModel.orderItems[0].subscription.title
            AppUtils.loadImage(context, AppUtils.getContentProviderSquareLogoURL(slotModel.orderItems[0].subscription.contentProviderId),binding.icon)
            if (isSelectable) {
                if (position == selectedIndex) {
                        binding.check.setImageResource(R.drawable.bn_ic_checked)
                }
                else binding.check.setImageResource(R.drawable.bn_ic_unchecked)
            }
            else {
                binding.check.setImageResource(R.drawable.bn_ic_right_arrow)
            }
            val subTitle = populateSubTitle(slotModel)

            binding.title.setOnClickListener {
                listener.orderTitleClicked(slotModel, subTitle)
            }
            binding.subTitle.setOnClickListener {
                listener.orderTitleClicked(slotModel, subTitle)
            }
        }

        private fun populateSubTitle(slotModel: Order): String? {
            var returnVal: String? = null
            try {
                val subs = BOConverter.bnSubscriptionPackToBO(slotModel.orderItems[0].subscription)
                var movieCount = 0
                val seriesSet = HashSet<String>()
                subs.contentIdList?.forEach { conId ->
                    val conObj = contentIdToContentMap[conId]
                    conObj?.let { con ->
                        if (con.isMovie) {
                            movieCount++
                        } else {
                            con.name?.let { it1 -> seriesSet.add(it1) }
                        }
                    }
                }
                val subTitleString = if (movieCount > 0) {
                    if (seriesSet.size > 0)
                        context.getString(R.string.includes_x_movies_series, movieCount, seriesSet.size)
                    else
                        context.getString(R.string.includes_x_movies, movieCount)
                } else if (seriesSet.size > 0) {
                    context.getString(R.string.includes_x_series, seriesSet.size)
                } else {
                    context.getString(R.string.watch_100s_of_movies_amp_serials)
                }

                binding.subTitle.text = subTitleString
                returnVal = subTitleString

            } catch (e: Exception) {
                Log.d("Exception ", e.toString())
                binding.subTitle.text = ""
            }
            return returnVal

        }
    }

    interface OnOrderItemClickedListener {
        fun onOrderItemClicked(order: Order)
        fun orderTitleClicked(order: Order, subTitle: String?)
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                val charString = charSequence.toString()
                ordersFilterList = if (charString.isEmpty()) {
                    ordersList
                } else {
                    val filteredList: ArrayList<Order> = ArrayList()
                    for (order in ordersList) {
                        for (orderItem in order.orderItems) {
                            if (orderItem.subscription.title.uppercase(Locale.getDefault()).contains(
                                    charString.uppercase(Locale.ROOT)
                                ) ||
                                    order.orderCreatedDate.uppercase(Locale.getDefault()).contains(
                                        charString.uppercase(Locale.ROOT)
                                    ))
                                filteredList.add(order)
                        }
                    }
                    filteredList
                }
                val filterResults = FilterResults()
                filterResults.values = ordersFilterList
                return filterResults
            }

            override fun publishResults(charSequence: CharSequence, filterResults: FilterResults) {
                ordersFilterList = filterResults.values as ArrayList<Order>
                notifyDataSetChanged()
            }
        }
    }
}