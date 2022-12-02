// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.microsoft.mobile.polymer.mishtu.storage.repositories

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.microsoft.mobile.polymer.mishtu.storage.SharedPreferenceStore
import com.microsoft.mobile.polymer.mishtu.storage.entities.*

import com.msr.bine_sdk.BineAPI
import com.msr.bine_sdk.models.Error
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

import java.util.*
import kotlin.collections.ArrayList

import com.microsoft.mobile.polymer.mishtu.utils.BNConstants
import com.microsoft.mobile.polymer.mishtu.utils.BOConverter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.lang.Exception
import java.text.SimpleDateFormat

class SubscriptionRepository internal constructor(private val subscriptionPackDao: SubscriptionPackDao,
                                                  private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
                                                  private val contentProviderDao: ContentProviderDao,
                                                  private val activeSubscriptionDao: ActiveSubscriptionDao ) {

    companion object {
        const val LOG_TAG = "SubscriptionRepository"
        const val KEY_SUBSCRIPTION_PACK_DATE_MODEL = "SubscriptionPackDate"
    }
    val formatter = SimpleDateFormat(BNConstants.formatter, Locale.getDefault())
    var allSubscriptionPack = MutableLiveData<List<SubscriptionPack>>()

    private suspend fun fetchSubscription(contentProviderId: String): Pair<List<SubscriptionPack>?, Error?> {
        val response = BineAPI.CMS().getSubscriptions(contentProviderId)
        response.result?.let {
            val  dateKey = "$KEY_SUBSCRIPTION_PACK_DATE_MODEL/${contentProviderId}"
            SharedPreferenceStore.getInstance().save(dateKey, formatter.format(Date()))
            Log.d("Subscriptions", "Fetch"+ it.size)
            return Pair(saveSubscriptions(it), null)
        } ?:
        response.error?.let {
            Log.d("Subscriptions", "Error")
            return Pair(null, it)
        }
        Log.d("Subscriptions", "Error.UNKNOWN_ERROR")
        return Pair(null, Error.UNKNOWN_ERROR)
    }

    private fun saveSubscriptions(subscriptionPack: List<com.msr.bine_sdk.cloud.models.SubscriptionPack>)
            : List<SubscriptionPack> {
        val subscriptionPackBOs = ArrayList<SubscriptionPack>()
        if(subscriptionPack.isNotEmpty())
        subscriptionPackDao.delete(subscriptionPack[0].contentProviderId)
        for (pack in subscriptionPack) {
            Log.d(LOG_TAG, "Saving - ${pack.id}")
            val subscriptionBO = BOConverter.bnSubscriptionPackToBO(pack)
            subscriptionPackDao.insert(subscriptionBO)
            subscriptionPackBOs.add(subscriptionBO)
        }
        return subscriptionPackBOs
    }

    private suspend fun fetchSubscriptionsIfTime(contentProviderId: String?): Boolean {
        if (contentProviderId == null) return false

        val shouldFetch = shouldFetch(contentProviderId)
        if (shouldFetch) {
            val subs = fetchSubscription(contentProviderId)
            subs.first?.let { return true }
        }
        return false
    }

    /*suspend fun fetchSubscriptionsIfTimeForAllContentProviders() {
        val list = contentProviderDao.getAllProviders()
        for(cp in list){
            fetchSubscriptionsIfTime(cp.id)
        }
    }*/

    suspend fun getAllContentProvider(): List<ContentProvider> {
        return contentProviderDao.getAllProviders()
    }

    suspend fun getSubscriptions(contentProviderId: String): Pair<List<SubscriptionPack>?, Error?> {
        val shouldFetch = shouldFetch(contentProviderId)
        Log.d("SubsRepo ",contentProviderId.toString() +"_"+ shouldFetch.toString())
        if (!shouldFetch) {
            val subscriptionPackBOs = subscriptionPackDao.getSubscriptionByProviderId(contentProviderId)
            if (subscriptionPackBOs.isNotEmpty()) {
                Log.d("Subscriptions", "Repo"+ subscriptionPackBOs.size)
                return Pair(subscriptionPackBOs, null)
            }
        }

        return fetchSubscription(contentProviderId)
    }

     fun getSubscriptionByProviderId(contentProviderId: String): List<SubscriptionPack> {
        return subscriptionPackDao.getSubscriptionByProviderId(contentProviderId)
    }

    fun fetchAsyncAllSubscriptionsIfTime() {
        CoroutineScope(Dispatchers.Default).launch {
            //Post live data
            fetchAllSubscriptionsIfTime()
        }
    }

    suspend fun fetchAllSubscriptionsIfTime() {
        val list = contentProviderDao.getAllProviders()
        val returnList: MutableList<SubscriptionPack> = ArrayList()
        for (cp in list) {
            val packsOfCP = getSubscriptions(cp.id)
            packsOfCP.first?.let { returnList.addAll(it) }
        }
        allSubscriptionPack.postValue(returnList)
    }

     fun getAllSubscriptionPacks(): List<SubscriptionPack>? {
        return subscriptionPackDao.getAllSubscriptionPack()
    }

    fun getSubscriptionPackByQuery(query: String): List<SubscriptionPack>? {
        return subscriptionPackDao.getSubscriptionByQuery(query)
    }

    private fun shouldFetch(contentProviderId: String): Boolean {
        val  dateKey = "$KEY_SUBSCRIPTION_PACK_DATE_MODEL/${contentProviderId}"
        val dateString = SharedPreferenceStore.getInstance().get(dateKey)
        if (dateString.isNullOrEmpty()) {
            Log.d(LOG_TAG, "DB - Date empty")
            return true
        }
        else {
            try {
                val date = formatter.parse(dateString)
                Log.d(LOG_TAG, "DB - $dateString")
                date?.let {
                    val difference: Long = Date().time - it.time
                    val hours = difference / (60 * 60 * 1000)
                    Log.d(LOG_TAG, "DB Difference- $hours")
                    if (hours >= 24) return true
                }
            }
            catch (e: Exception) {
                return true
            }
        }
        return false
    }

    fun reset() {
        subscriptionPackDao.deleteAll()
    }
    suspend fun getActiveSubscriptionForContentProviderId(contentProviderId: String): List<ActiveSubscription>? {
        return activeSubscriptionDao.getActiveSubscriptionForProviderId(contentProviderId)
    }

    suspend fun getSubscriptionById(subscriptionId: String): SubscriptionPack{
        return subscriptionPackDao.getSubscriptionById(subscriptionId)
    }

    fun getSubscriptionByIdLiveData(subscriptionId: String) = subscriptionPackDao.getSubscriptionByIdLiveData(subscriptionId)



}