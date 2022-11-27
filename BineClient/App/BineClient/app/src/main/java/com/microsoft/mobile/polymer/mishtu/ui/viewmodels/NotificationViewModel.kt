package com.microsoft.mobile.polymer.mishtu.ui.viewmodels

import android.app.Activity
import androidx.lifecycle.ViewModel
import com.microsoft.mobile.polymer.mishtu.BuildConfig
import com.microsoft.mobile.polymer.mishtu.storage.SharedPreferenceStore

import com.microsoft.mobile.polymer.mishtu.storage.repositories.NotificationBadgeHelper
import com.microsoft.mobile.polymer.mishtu.storage.repositories.OrderRepository
import com.microsoft.mobile.polymer.mishtu.storage.repositories.SubscriptionRepository
import com.microsoft.mobile.polymer.mishtu.ui.activity.BaseActivity
import com.microsoft.mobile.polymer.mishtu.utils.BadgeEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(private val notificationBadgeHelper: NotificationBadgeHelper,
                                                private val subscriptionRepository: SubscriptionRepository,
                                                private val orderRepository: OrderRepository): ViewModel() {

    /**
     * Get all unread notification i.e SharedPref[NotificationBadgeHelper.BadgeType] != ""
     * And post the event via EventBus
     */
    fun getUnreadNotifications(activity: Activity) {
        notificationBadgeHelper.clearNotification(NotificationBadgeHelper.BadgeType.PROFILE)

        for (badge in NotificationBadgeHelper.BadgeType.values()) {
            when (badge) {
                NotificationBadgeHelper.BadgeType.NEW_OFFER -> {}
                NotificationBadgeHelper.BadgeType.PACK_EXPIRED -> checkForExpiredSubscription(activity)
                else -> notificationBadgeHelper.getNotification(badge)?.toInt()?.let {
                    EventBus.getDefault().post(BadgeEvent(badge, it, true))
                }
            }
        }
    }

    /**
     * Get specific unread notification
     */
    fun getUnreadNotifications(type: NotificationBadgeHelper.BadgeType): Int? {
        notificationBadgeHelper.getNotification(type)?.toInt()?.let {
            return it
        }
        return null
    }

    /**
     * Clear notification i.e set SharedPref[NotificationBadgeHelper.BadgeType] == ""
     */
    fun clearNotification(badgeType: NotificationBadgeHelper.BadgeType) {
        if (badgeType == NotificationBadgeHelper.BadgeType.NEW_OFFER) {
            clearOfferUnlocked()
        }
        notificationBadgeHelper.clearNotification(badgeType)
    }

    /**
     * 1. If coins were fetched
     * 2. Fetch offers
     * 3. Fetch list of previously unlocked offers
     * 4. Identify new offers unlocked not in #3
     */
    private suspend fun getNewUnlockedOffers(): ArrayList<String>? {
        val coinsString = SharedPreferenceStore.getInstance().get(SharedPreferenceStore.KEY_REWARDS_COINS)
        if (coinsString.isNullOrEmpty()) return null
        coinsString.toInt().let { coins ->
            val subscriptions =
                subscriptionRepository.getAllSubscriptionPacks()
            val unlockedRewards = notificationBadgeHelper.getUnlockedOffers()
            val newUnlockedRewards = arrayListOf<String>()
            subscriptions?.let {
                for (sub in it) {
                    if (sub.isReadable &&
                        coins >= sub.redemptionValue &&
                        !unlockedRewards.contains(sub.id)
                    ) {
                        newUnlockedRewards.add(sub.id)
                    }
                }
                return newUnlockedRewards
            }
        }
        return null
    }

    /**
     * Save the offer list as badge for them is already shown
     */
    private fun clearOfferUnlocked() {
        CoroutineScope(Dispatchers.Default).launch {
            getNewUnlockedOffers()?.let {
                notificationBadgeHelper.saveUnlockedOffers(it)
            }
        }
    }

    /**
     * Get new unlocked offers & mark it
     */
    private fun checkForOfferUnlocked() {
        CoroutineScope(Dispatchers.Default).launch {
            getNewUnlockedOffers()?.let {
                notificationBadgeHelper.markNotification(
                    NotificationBadgeHelper.BadgeType.NEW_OFFER,
                    it.size
                )
            }
        }
    }

    /**
     * Get subscription expired & mark it
     */
    private fun checkForExpiredSubscription(activity: Activity) {
        CoroutineScope(Dispatchers.Default).launch {
            var validPackExist = false
            var count: Int = 0
            for (subs in (activity as BaseActivity).cpIdToActiveSubscriptionMap.values) {
                if (subs.isExpired()) {
                    count++
                }
            }
            if (count > 0) {
                notificationBadgeHelper.markNotification(
                    NotificationBadgeHelper.BadgeType.PACK_EXPIRED,
                    count
                )
            } else {
                validPackExist = true
            }
            checkForOfferUnlocked()
        }
    }
}