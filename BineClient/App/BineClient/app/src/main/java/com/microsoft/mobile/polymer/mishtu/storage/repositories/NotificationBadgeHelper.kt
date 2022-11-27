package com.microsoft.mobile.polymer.mishtu.storage.repositories

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.microsoft.mobile.polymer.mishtu.storage.SharedPreferenceStore
import com.microsoft.mobile.polymer.mishtu.utils.BadgeEvent
import org.greenrobot.eventbus.EventBus

/**
 * All Badge type supported would be defined by enum
 * For setting a badge type we do SharedPref[BadgeType] = count(Int value)
 * For resetting a badge type we do SharedPref[BadgeType] = ""
 */
class NotificationBadgeHelper {
    private var sharedPreferenceStore = SharedPreferenceStore.getInstance()

    enum class BadgeType(val value: String) {
        NEW_CONTENT("NewContent"),
        PROFILE("Profile"),
        NEW_OFFER("NewOffer"),
        NEW_DOWNLOADS("NewDownloads"),
        PACK_EXPIRED("SubscriptionExpired"),
        EXPORT_DATA_READY("ExportDataReady")
    }

    companion object {
        const val LOG_TAG = "NotificationBadgeHelper"
    }

    /*fun saveNotification(notification: Notification, type: String) {
        val key = "$KEY_NOTIFICATION/$type"
        Log.d(LOG_TAG, "Saving Notif - $key")
        mDB.putObject(key, notification)
    }

    fun getUnreadNotification(): List<Notification> {
            val notificationList = arrayListOf<Notification>()
            for (key in mDB.findKeysByPrefix(KEY_NOTIFICATION)) {
                val notification = mDB.getObject(key, Notification::class.java)
                if (!notification.isRead) {
                    Log.d(LOG_TAG, "Fetching Notif - ${notification.title}")
                    notificationList.add(notification)
                }
            }
            return notificationList

    }

    fun getAllNotifications(): List<Notification> {
        val notificationList = arrayListOf<Notification>()
        for (key in mDB.findKeysByPrefix(KEY_NOTIFICATION)) {
            val notifications = mDB.getObjectArray(key, Notification::class.java)
            for (notification in notifications)
                notificationList.add(notification)
        }
        return notificationList
    }*/

    fun reset() {

    }

    /**
     * Get SharedPref value of a badge
     */
    fun getNotification(type: BadgeType): String? {
        val value = sharedPreferenceStore.get(type.value)
        if(!value.isNullOrEmpty()) return value
        return null
    }

    /**
     * Mark a badge type with the provided count value & post via EventBus for UI action
     * Few notification would be initialized in groups
     * eg. offers unlocked
     */
    fun markNotification(type: BadgeType, count: Int) {
        if (count == 0) return
        sharedPreferenceStore.save(type.value, "$count")
        EventBus.getDefault().post(BadgeEvent(type, count, true))

        if (type == BadgeType.EXPORT_DATA_READY || type == BadgeType.PACK_EXPIRED) {
            markNotification(BadgeType.PROFILE)
        }
    }

    /**
     * Mark a badge type
     * Default - would increment by 1 as per old value
     */
    fun markNotification(type: BadgeType) {
        var count = 0
        val sharedPrefCountString = sharedPreferenceStore.get(type.value)
        if (sharedPrefCountString.isNullOrEmpty()) {
            markNotification(type, 1)
            return
        }

        sharedPrefCountString.toInt().let {
            count += it
        }
        markNotification(type, count + 1)
    }

    /**
     * Reset a value of ShredPref[badge] = "" & post via EventBus for UI action
     */
    fun clearNotification(type: BadgeType) {
        sharedPreferenceStore.save(type.value, "")
        EventBus.getDefault().post(BadgeEvent(type, 0, false))
    }

    /**
     * For offers we need to save the ids of unlocked offer
     * in order to figure out newly unlocked offers
     * Eg. SharedPref[] = "offer1Id, offer2"(json) - Indicated badge for two offer already shown
     */
    fun saveUnlockedOffers(offerId: ArrayList<String>) {
        if (offerId.isEmpty()) return
        val rewards = sharedPreferenceStore.get(SharedPreferenceStore.KEY_REWARDS_UNLOCKED)
        val notificationList =
            if (rewards.isNullOrEmpty()) {
                arrayListOf<String>()
            }
            else {
                Gson().fromJson(rewards, object : TypeToken<ArrayList<String?>?>() {}.type)
            }
        notificationList.addAll(offerId)
        sharedPreferenceStore.save(SharedPreferenceStore.KEY_REWARDS_UNLOCKED, Gson().toJson(notificationList))
    }

    /**
     * Return previously saved offerIds
     */
    fun getUnlockedOffers(): ArrayList<String> {
        val rewards = sharedPreferenceStore.get(SharedPreferenceStore.KEY_REWARDS_UNLOCKED)
        return if (rewards.isNullOrEmpty())
            arrayListOf()
        else
            Gson().fromJson(rewards, object : TypeToken<ArrayList<String?>?>() {}.type)
    }
}