package com.microsoft.mobile.polymer.mishtu.ui.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.microsoft.mobile.polymer.mishtu.R
import com.microsoft.mobile.polymer.mishtu.databinding.ItemActiveSubscriptionBinding
import com.microsoft.mobile.polymer.mishtu.databinding.ItemContentProviderBinding
import com.microsoft.mobile.polymer.mishtu.databinding.ItemExpiredSubscriptionBinding
import com.microsoft.mobile.polymer.mishtu.kaizala_utils.TimestampUtils
import com.microsoft.mobile.polymer.mishtu.storage.SharedPreferenceStore
import com.microsoft.mobile.polymer.mishtu.storage.entities.ActiveSubscription
import com.microsoft.mobile.polymer.mishtu.storage.entities.ContentProvider
import com.microsoft.mobile.polymer.mishtu.ui.activity.BaseActivity
import com.microsoft.mobile.polymer.mishtu.utils.AppUtils
import com.microsoft.mobile.polymer.mishtu.utils.BNConstants
import java.text.SimpleDateFormat
import java.util.*

class ActiveSubscriptionAdapter(
    private val context: Context,
    private val supportFragmentManager: FragmentManager,
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val dataModelList = ArrayList<ActiveSubscription>()
    private val TYPE_ACTIVE = 1
    private val TYPE_EXPIRED = 0

    inner class ViewHolderActive(private var binding: ItemActiveSubscriptionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(sub: ActiveSubscription) {

            binding.subscriptionActivePackTitle.text =
                String.format(context.getString(R.string.bn_pack_rs), sub.subscription.price)

            binding.subscriptionActivePackDescription.text =
                String.format(context.getString(R.string.content_provider_pack),
                    SharedPreferenceStore.getInstance().get(SharedPreferenceStore.PREFIX_CONTENT_PROVIDER+sub.providerId))
            AppUtils.loadImage(context, AppUtils.getContentProviderSquareLogoURL(sub.providerId),binding.subscriptionActiveLogo)
            val dateString = setExpiryDate(sub.planEndDate)
            binding.subscriptionActivePackDate.text =
                String.format(context.getString(R.string.pack_expires_on), dateString)

        }

    }


    inner class ViewHolderExpired(private var binding: ItemExpiredSubscriptionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(sub: ActiveSubscription) {

            binding.subscriptionActivePackTitle.text =
                String.format(context.getString(R.string.bn_pack_rs), sub.subscription.price)
            binding.subscriptionActivePackDescription.text =
                String.format(
                    context.getString(R.string.bn_enjoy_all_movies),
                    sub.subscription.durationDays
                )

            AppUtils.loadImage(context, AppUtils.getContentProviderSquareLogoURL(sub.providerId),binding.subscriptionActiveLogo)


            binding.subscriptionPackDetails.clipToOutline =
                true
            binding.subscriptionExpiredBuyPack
                .setOnClickListener {
                    AppUtils.startOrderFlow(supportFragmentManager,context,
                        sub.providerId)
                }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == TYPE_EXPIRED) {
            val binding: ItemExpiredSubscriptionBinding =
                DataBindingUtil.inflate(LayoutInflater.from(parent.context),
                    R.layout.item_expired_subscription,
                    parent,
                    false)

            return ViewHolderExpired(binding)
        }
        val binding: ItemActiveSubscriptionBinding =
            DataBindingUtil.inflate(LayoutInflater.from(parent.context),
                R.layout.item_active_subscription,
                parent,
                false)

        return ViewHolderActive(binding)
    }

    override fun getItemViewType(position: Int): Int {
        if (dataModelList[position].isExpired()) {
            return TYPE_EXPIRED
        }
        return TYPE_ACTIVE
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val dataModel = dataModelList[position]
        if (holder is ActiveSubscriptionAdapter.ViewHolderActive) {
            holder.bind(dataModel)
        } else if (holder is ActiveSubscriptionAdapter.ViewHolderExpired) {
            holder.bind(dataModel)
        }
    }

    override fun getItemCount(): Int {
        return dataModelList.size;
    }

    fun setData(dataList: List<ActiveSubscription>) {
        dataModelList.clear()
        dataModelList.addAll(dataList);
        notifyDataSetChanged()
    }

    private fun setExpiryDate(expiryDate: String): String {
        val date = TimestampUtils.getDateFromUTCString(expiryDate)
        return SimpleDateFormat(BNConstants.DATE_FORMAT_dd_MM_YY, Locale.getDefault()).format(date)
    }
}