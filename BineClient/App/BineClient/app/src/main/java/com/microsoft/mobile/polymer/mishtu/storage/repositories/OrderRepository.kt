// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.microsoft.mobile.polymer.mishtu.storage.repositories

import android.util.Log
import androidx.lifecycle.LiveData
import com.microsoft.mobile.polymer.mishtu.offline.DownloadManager
import com.microsoft.mobile.polymer.mishtu.storage.SharedPreferenceStore
import com.microsoft.mobile.polymer.mishtu.storage.entities.*
import com.microsoft.mobile.polymer.mishtu.utils.BOConverter
import com.microsoft.mobile.polymer.mishtu.utils.SubscriptionManager

import com.msr.bine_sdk.BineAPI

class OrderRepository internal constructor(
    private val orderDao: OrderDao,
    private val activeSubscriptionDao: ActiveSubscriptionDao,
    private var downloadManager: DownloadManager,
    private val contentDownloadDao: ContentDownloadDao,
    private val contentProviderDao: ContentProviderDao,
    private val subscriptionPackDao: SubscriptionPackDao,
    private val subscriptionManager: SubscriptionManager

    /*,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO*/
) {
    private fun createOrder(order: Order) {
        orderDao.insert(order)
    }

    private fun deleteOrder(contentProviderId: String) {
        orderDao.deleteOrderForProviderId(contentProviderId)
    }
    private fun deleteOrderBySubscriptionId(subscriptionId: String) {
        orderDao.deleteOrderForSubscriptionId(subscriptionId)
    }

    fun deleteOrderByOrderId(orderId: String) {
        orderDao.deleteOrderByOrderId(orderId)
    }

    suspend fun getActiveOrder(contentProviderId: String): List<Order>? {

           return fetchActiveOrders(contentProviderId)

    }

    suspend fun getActiveOrderByOrderId(orderId: String): Order? {
        return orderDao.getOrderForId(orderId)
    }

    /*private suspend fun fetchOrder(orderId: String): Order? {
        val response = BineAPI.OMS().getOrder(orderId)
        response.result?.let {
            if (it.isNotEmpty()) {
                val orderBO = BOConverter.bnOrderToBO(it[0])
                createOrder(orderBO)
                return orderBO
            }
            return null
        }
        return null
    }*/

    private suspend fun fetchActiveOrders(contentProviderId: String?): List<Order>? {
        val response = BineAPI.OMS().getOrders(arrayListOf("Created"))
        val returnList = ArrayList<Order>()
        orderDao.deleteAll()
        response.result?.let {
            if (it.isNotEmpty()) {
                var activeOrderForCP: Order? = null
                for (order in it) {
                    Log.d("Oredr2", order.toString())
                    val orderBO = BOConverter.bnOrderToBO(order)
                    createOrder(orderBO)

                    if(order.orderItems[0].subscription.subscriptionType == SubscriptionPack.PackType.SVOD.value) {
                        SharedPreferenceStore.getInstance()
                                .save(SharedPreferenceStore.KEY_ORDER_ID + "_${order.orderItems[0].subscription.contentProviderId}",
                                        order.id)
                    }
                    if (!contentProviderId.isNullOrEmpty() && order.orderItems[0].subscription.contentProviderId == contentProviderId) {
                        activeOrderForCP = orderBO
                        returnList.add(activeOrderForCP)
                    }
                }
                return returnList
            } else {
                delAllActiveOrderFromSharedPreferences(null)
                return null
            }
        } ?: return null
    }

    /**
     * 1. Check if there is subscriptionBO in DB for content provider id
     *      a. If exist and plan_date id future(not expired) return it
     *      b. else return null
     * 2. Else Fetch active subscriptions
     */
    suspend fun getActiveSubscription(contentProviderId: String?): List<ActiveSubscription>? {
        if (contentProviderId == null) return null

        val subscriptionsBO =
            activeSubscriptionDao.getActiveSubscriptionForProviderId(contentProviderId)
         val returnList = ArrayList<ActiveSubscription>()
         if (subscriptionsBO != null) {
            for(sub in subscriptionsBO){
                if (sub.isExpired()) {
                    resetKeyForSubscriptionExpired(sub.providerId)
                    /*notificationBadgeHelper.saveNotification(
                        Notification(NotificationBadgeHelper.NOTIFICATION_TYPE_EXPIRED,
                            "Your pack is expired", "To continue watching Eros Now movies and series, please renew your pack",
                            "", false, ""), NotificationBadgeHelper.NOTIFICATION_TYPE_EXPIRED)*/
                    fetchActiveSubscriptions(contentProviderId)// not clearing the db, if get new in fetch call it will update
                } else returnList.add(sub)
            }

        } else {
           returnList.addAll(ArrayList(fetchActiveSubscriptions(contentProviderId)))
        }
        return returnList
    }

    suspend fun getActiveSubscriptionForAllContentProvider() {
        val cpList = contentProviderDao.getAllProviders()
        var count: Int = 0
        for (cp in cpList) {
            val subscriptionsBO = activeSubscriptionDao.getActiveSubscriptionForProviderId(cp.id)
            if (subscriptionsBO != null) {

                for(sub in subscriptionsBO) {
                    if (sub.isExpired()) {
                        resetKeyForSubscriptionExpired(sub.providerId)
                        deleteActiveSubscriptionFromSharedPreferences(sub)
                    }
                }
            }
        }
        fetchActiveSubscriptions(null)
    }

    private fun deleteActiveSubscriptionFromSharedPreferences(sub: ActiveSubscription) {
        if(sub.subscription.subscriptionType == SubscriptionPack.PackType.SVOD.value) {
            SharedPreferenceStore.getInstance()
                    .save(SharedPreferenceStore.KEY_ACTIVE_SUBSCRIPTION_ID_SVOD + "_${sub.providerId}", "")
            return
        }
        val key = SharedPreferenceStore.KEY_ACTIVE_SUBSCRIPTION_ID_TVOD+"_${sub.providerId}"
        val existingTvodSubs = SharedPreferenceStore.getInstance().get(key)
        val updatedTvodSubs = existingTvodSubs?.replace(sub.subscription.id,"")
        if (updatedTvodSubs != null) {
            SharedPreferenceStore.getInstance().save(key, updatedTvodSubs)
        }

    }

    suspend fun getActiveOrderForAllContentProvider() {
        /*val cpList = contentProviderDao.getAllProviders()
        var count: Int = 0
        for (cp in cpList) {
            val orderBO = orderDao.getOrderForProviderId(cp.id)
            if (orderBO == null) {
                count++
            }
        }
        if (count > 0) {*/
            fetchActiveOrders(null)

    }


    // eros now -> expired , not in cache but in db
    suspend fun fetchActiveSubscriptions(contentProviderId: String?): List<ActiveSubscription>? {
        val response = BineAPI.OMS().getActiveSubscriptions()   //  hotstar
        val returnList = ArrayList<ActiveSubscription>()
        response.result?.let {
            if (it.isNullOrEmpty()) {
                SharedPreferenceStore.getInstance()
                        .save(SharedPreferenceStore.KEY_ACTIVE_SUBSCRIPTION_ID_SVOD + "_$contentProviderId","")
                null
            } else {
                var subForContentProvider: ActiveSubscription? = null
                saveActiveSubscriptions(it)
                for (subscription in it) {
                    //Clear order
                    deleteOrderBySubscriptionId(subscription.subscription.id)
                    delAllActiveOrderFromSharedPreferences(subscription)
                    //Save active Subs details
                    saveActiveSubscriptionInSharedPreference(subscription)

                    if (!contentProviderId.isNullOrEmpty() && subscription.subscription.contentProviderId == contentProviderId) {
                        subForContentProvider = BOConverter.bnSubscriptionToBO(subscription)
                        returnList.add(subForContentProvider)
                    }


                    //Check expiry
                    if (subForContentProvider != null && subForContentProvider.isExpired()) {
                        resetKeyForSubscriptionExpired(subForContentProvider.providerId)
                        /*notificationBadgeHelper.saveNotification(
                            Notification(NotificationBadgeHelper.NOTIFICATION_TYPE_EXPIRED,
                                "Your pack is expired", "To continue watching Eros Now movies and series, please renew your pack",
                                "", false, ""), NotificationBadgeHelper.NOTIFICATION_TYPE_EXPIRED)*/
                        deleteActiveSubscriptionFromSharedPreferences(subForContentProvider)
                    }
                }
                return returnList
            }
        }
        return null
    }

    private fun delAllActiveOrderFromSharedPreferences(subscription: com.msr.bine_sdk.cloud.models.Order.Subscription?) {
        if(subscription != null) {
            SharedPreferenceStore.getInstance()
                    .save(SharedPreferenceStore.KEY_ORDER_ID + "_${subscription.subscription.contentProviderId}", "")
            return
        }
        val allKeys = SharedPreferenceStore.getInstance().getAll()
        if(!allKeys.isNullOrEmpty()) {
            val prefix = SharedPreferenceStore.KEY_ORDER_ID
            for (key in allKeys.keys){
                if(key.startsWith(prefix)){
                    SharedPreferenceStore.getInstance().save(key,"")
                }
            }
        }
    }

    private fun saveActiveSubscriptionInSharedPreference(subscription: com.msr.bine_sdk.cloud.models.Order.Subscription) {

        if(subscription.subscription.subscriptionType == SubscriptionPack.PackType.SVOD.value){
            SharedPreferenceStore.getInstance()
                    .save(SharedPreferenceStore.KEY_ACTIVE_SUBSCRIPTION_ID_SVOD + "_${subscription.subscription.contentProviderId}",
                            subscription.subscription.id)
        }
    }

    private fun saveActiveSubscriptions(subscriptions: List<com.msr.bine_sdk.cloud.models.Order.Subscription>) {
        for (subscription in subscriptions) {
            activeSubscriptionDao.delete(subscription.subscription.id)

            val subscriptionBO = BOConverter.bnSubscriptionToBO(subscription)
            activeSubscriptionDao.insert(subscriptionBO)
        }
    }
    private fun resetKeyForSubscriptionExpired(contentProviderId: String) {
        val contentIds = arrayListOf<String>()
        val downloads = contentDownloadDao.downloadedContent()
        for (content in downloads) {
            if (!content.free &&  subscriptionManager.hasValidSubscriptionForContent(contentProviderId, content.contentId)) {
                contentIds.add(content.contentId)
            }
        }
        downloadManager.clearDownloads(contentIds)

    }

    fun reset() {
        val allEntries = SharedPreferenceStore.getInstance().getAll()
        if (allEntries != null) {
            for (x in allEntries.keys) {
                if (x.startsWith(SharedPreferenceStore.KEY_ORDER_ID)) {
                    SharedPreferenceStore.getInstance().save(x, "")
                }
            }
        }
        orderDao.deleteAll()
        activeSubscriptionDao.deleteAll()

    }

    fun getActiveSubscriptionLiveData(contentProviderId: String): LiveData<List<ActiveSubscription>?> {
        return activeSubscriptionDao.getActiveSubscriptionLiveData(contentProviderId)
    }

    fun getActiveSubscriptionBySubscriptionIdLiveData(subscriptionId: String): LiveData<ActiveSubscription?> {
        return activeSubscriptionDao.getActiveSubscriptionBySubscriptionIdLiveData(subscriptionId)
    }

    suspend fun getActiveSubscriptionBySubscriptionId(subscriptionId: String): ActiveSubscription? {
        return activeSubscriptionDao.getActiveSubscriptionBySubscriptionId(subscriptionId)
    }

    fun getAllActiveSubscriptionLiveData(): LiveData<List<ActiveSubscription>?> {
        return activeSubscriptionDao.getAllActiveSubscriptionLiveData()
    }

    fun getOrderLiveData(contentProviderId: String): LiveData<List<Order>?> {
        return orderDao.getOrderLiveData(contentProviderId)
    }

     suspend fun getSubscriptionById(subscriptionId: String): SubscriptionPack {
        return subscriptionPackDao.getSubscriptionById(subscriptionId)
    }

}