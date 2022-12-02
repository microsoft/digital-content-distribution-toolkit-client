// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.microsoft.mobile.polymer.mishtu.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.microsoft.mobile.polymer.mishtu.R
import com.microsoft.mobile.polymer.mishtu.databinding.ListItemNotificationBinding
import com.microsoft.mobile.polymer.mishtu.storage.entities.snappy.Notification

import com.microsoft.mobile.polymer.mishtu.storage.repositories.NotificationBadgeHelper

class NotificationsAdapter(var context: Context,
                           private val notifications: List<Notification>,
                           private val onClickListener: OnNotificationClickListener
)
    : RecyclerView.Adapter<NotificationsAdapter.NotificationViewHolder>() {

    class NotificationViewHolder(var binding: ListItemNotificationBinding) :
            RecyclerView.ViewHolder(binding.root) {
        fun bindData(context: Context,
                     notification: Notification,
                     onClickListener: OnNotificationClickListener,
                     isLast: Boolean) {

            binding.notificationTitle.text = notification.title
            binding.notificationSubtext.text = notification.subTitle

            /*when(notification.type) {
                NotificationBadgeHelper.NOTIFICATION_TYPE_NEW_CONTENT -> {
                    binding.notificationButton.text = context.getString(R.string.button_watch_now)
                    Glide.with(context)
                            .load(notification.imageURL)
                            .dontAnimate().placeholder(R.drawable.bg_shimmer_gradient)
                            .into(binding.notificationIcon)
                    binding.notificationButton.setOnClickListener { onClickListener.onWatchContentClicked(notification) }
                }
                NotificationBadgeHelper.NOTIFICATION_TYPE_ORDER_CONFIRMED -> {
                    binding.notificationIcon.setImageResource(R.drawable.bn_ic_order_notification)
                    binding.notificationButton.text = context.getString(R.string.button_view_subscription)
                    binding.notificationButton.setOnClickListener { onClickListener.viewSubscriptionClicked() }
                }
                NotificationBadgeHelper.NOTIFICATION_TYPE_NEW_OFFER -> {
                    binding.notificationButton.text = context.getString(R.string.button_view_offers)
                    binding.notificationButton.setOnClickListener { onClickListener.viewOffersClicked() }
                }
                NotificationBadgeHelper.NOTIFICATION_TYPE_EXPIRED -> {
                    binding.notificationIcon.setImageResource(R.drawable.bn_ic_eros_squre)
                    binding.notificationButton.text = context.getString(R.string.button_renew_pack)
                    binding.notificationTitle.text = context.getString(R.string.pack_expired_title)
                    binding.notificationSubtext.text = context.getString(R.string.pack_expired_description)
                    binding.notificationButton.setOnClickListener { onClickListener.onRenewPackClicked() }
                }
            }*/

            if (isLast) binding.notificationSeparator.visibility = View.GONE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val itemBinding: ListItemNotificationBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context),
                R.layout.list_item_notification,
                parent, false)
        return NotificationViewHolder(itemBinding)
    }

    override fun getItemCount(): Int {
       return notifications.size
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notificationBO = notifications[position]
        holder.bindData(context, notificationBO, onClickListener, position == notifications.size-1)
    }

    interface OnNotificationClickListener {
        fun onWatchContentClicked(notification: Notification)
        fun onRenewPackClicked()
        fun viewOffersClicked()
        fun viewSubscriptionClicked()
    }
}