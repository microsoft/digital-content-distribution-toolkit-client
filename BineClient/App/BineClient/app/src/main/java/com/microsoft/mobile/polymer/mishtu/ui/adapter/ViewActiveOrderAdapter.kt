package com.microsoft.mobile.polymer.mishtu.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.microsoft.mobile.polymer.mishtu.R
import com.microsoft.mobile.polymer.mishtu.databinding.ListItemViewActiveOrderBinding
import com.microsoft.mobile.polymer.mishtu.storage.entities.Order
import com.microsoft.mobile.polymer.mishtu.storage.entities.SubscriptionPack
import com.microsoft.mobile.polymer.mishtu.ui.fragment.sheet.BottomSheetCancelOrderDialog
import com.microsoft.mobile.polymer.mishtu.utils.AppUtils

class ViewActiveOrderAdapter(val context: Context, val childFragmentManager: FragmentManager) :
        RecyclerView.Adapter<ViewActiveOrderAdapter.ViewHolder>() {

    private val orderList = ArrayList<Order>()
    private val activeSubsList = ArrayList<SubscriptionPack>()

    private val ItemTypeOrderView = 0
    private val ItemTypeExpiredSubsView = 1


    fun setOrderList(data: List<Order>) {
        orderList.clear()
        orderList.addAll(data)
        notifyDataSetChanged()
    }

    fun setActiveSubsList(data: List<SubscriptionPack>) {
        activeSubsList.clear()
        activeSubsList.addAll(data)
        notifyDataSetChanged()
    }


    override fun getItemViewType(position: Int): Int {
        if (position < orderList.size) {
            return ItemTypeOrderView
        }
        return ItemTypeExpiredSubsView
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: ListItemViewActiveOrderBinding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.list_item_view_active_order, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (position < orderList.size) {
            holder.bind(orderList[position])
        } else {
            holder.bind(activeSubsList[position - orderList.size])
        }
    }

    override fun getItemCount(): Int {
        return orderList.size + activeSubsList.size
    }


    inner class ViewHolder(private var itemBinding: ListItemViewActiveOrderBinding) :
            RecyclerView.ViewHolder(itemBinding.root) {

        fun bind(dataModel: Order) {
            val cardViewMarginParams =
                    itemBinding.parentCardView.layoutParams as ViewGroup.MarginLayoutParams
            cardViewMarginParams.setMargins(0, 0, 0, context.resources.getDimension(R.dimen.dp_8).toInt())
            itemBinding.parentCardView.layoutParams = cardViewMarginParams


            itemBinding.homeCardSelectStore.setOnClickListener {
                AppUtils.showNearbyStore(context, null, false, dataModel.contentProviderId, dataModel.subscriptionId)
            }
            itemBinding.homeCardCancel.setOnClickListener {
                cancelOrder(dataModel)
            }


            itemBinding?.homeCardActiveOrder?.visibility =
                    View.VISIBLE
            itemBinding?.homeCardOttName?.text = AppUtils.getContentProviderNameFromId(dataModel.contentProviderId)
            itemBinding?.homeCardActiveSubs?.visibility =
                    View.GONE
            itemBinding?.homeCardPackTitle?.text =
                    String.format(
                            context.resources.getString(R.string.bn_pack_rs),
                            dataModel.price
                    )

        }

        fun bind(dataModel: SubscriptionPack) {
            itemBinding?.homeCardActiveSubs?.visibility =
                    View.VISIBLE
            itemBinding.homeCardActiveOrder.visibility = View.GONE
            itemBinding?.homeCardActiveSubs?.findViewById<TextView>(R.id.home_card_subs_pack_title)?.text =
                    String.format(
                            context.resources.getString(R.string.bn_pack_rs),
                            dataModel.price
                    )
            itemBinding?.homeCardActiveSubs?.findViewById<TextView>(R.id.home_card_subs_ott_name)?.text =
                    AppUtils.getContentProviderNameFromId(dataModel.contentProviderId) ?: ""

            itemBinding?.homeCardActiveSubs?.findViewById<Button>(R.id.home_card_subs_renew)
                    ?.setOnClickListener {
                        AppUtils.startOrderFlow(childFragmentManager, context, dataModel.contentProviderId)
                    }

        }

        private fun cancelOrder(order: Order) {

            val cancelOrderBottomSheet =
                    BottomSheetCancelOrderDialog(order, order.contentProviderId)
            cancelOrderBottomSheet.show(childFragmentManager,
                    cancelOrderBottomSheet.tag)
        }

    }


}