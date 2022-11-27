package com.microsoft.mobile.polymer.mishtu.utils

import com.microsoft.mobile.polymer.mishtu.storage.SharedPreferenceStore
import com.microsoft.mobile.polymer.mishtu.storage.entities.ActiveSubscription
import com.microsoft.mobile.polymer.mishtu.storage.repositories.SubscriptionRepository
import kotlinx.coroutines.*

class SubscriptionManager internal constructor(private val subscriptionRepo: SubscriptionRepository){

    private val contentProviderToSubscriptionPacksMap = HashMap<String, List<ActiveSubscription>?>()

    fun hasSvodActiveSubscription(contentProviderId: String): Boolean {
        return !SharedPreferenceStore.getInstance().get(SharedPreferenceStore.KEY_ACTIVE_SUBSCRIPTION_ID_SVOD+"_$contentProviderId").isNullOrEmpty()
    }

    fun hasValidSubscriptionForContent(contentProviderId: String, contentId: String): Boolean{
        if(hasSvodActiveSubscription(contentProviderId)){
            return true
        }
        if(contentProviderToSubscriptionPacksMap.isEmpty()){
            val task = CoroutineScope(Dispatchers.Default).launch {
                populateContentProviderToSubscriptionPacksMap()
            }
            runBlocking {
                task.join()
            }
        }

        val activeSubscriptionPackList = contentProviderToSubscriptionPacksMap[contentProviderId]

        if (activeSubscriptionPackList != null) {
            for(activeSub in activeSubscriptionPackList){
                if(!activeSub.isExpired() &&
                        !activeSub.subscription.contentIdList.isNullOrEmpty()
                        && activeSub.subscription.contentIdList!!.contains(contentId)){
                    return true
                }
            }
        }
        return false
    }

    fun hasValidSubscriptionForContent(contentProviderId: String, contentId: String,
                                       activeSubscriptionList: List<ActiveSubscription>?): Boolean {
        if(activeSubscriptionList.isNullOrEmpty()){
            return false
        }
        if (hasSvodActiveSubscription(contentProviderId)) {
            return true
        }
        for(activeSub in activeSubscriptionList){
            if(!activeSub.subscription.contentIdList.isNullOrEmpty() &&
                activeSub.subscription.contentIdList!!.contains(contentId) &&
                !activeSub.isExpired()
            ){
                return true
            }
        }
        return false
    }

    private suspend fun populateContentProviderToSubscriptionPacksMap() {
            val allCp = subscriptionRepo.getAllContentProvider()
            for(cp in allCp){
                val subscriptionPackList = subscriptionRepo.getActiveSubscriptionForContentProviderId(cp.id)
                contentProviderToSubscriptionPacksMap[cp.id] = subscriptionPackList
            }
        }

    fun hasActiveOrder(contentProviderId: String): Boolean {
        val allKeys = SharedPreferenceStore.getInstance().getAll()?.keys
        if (allKeys != null) {
            val prefix = SharedPreferenceStore.KEY_ORDER_ID + "_$contentProviderId"
            for (key in allKeys) {
                if (key.startsWith(prefix) && !SharedPreferenceStore.getInstance().get(key).isNullOrEmpty()) {
                    return true
                }
            }
        }
        return false
    }
}