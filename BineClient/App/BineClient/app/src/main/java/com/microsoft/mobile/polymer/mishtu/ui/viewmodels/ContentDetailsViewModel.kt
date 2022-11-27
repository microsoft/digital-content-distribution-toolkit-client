package com.microsoft.mobile.polymer.mishtu.ui.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.microsoft.mobile.polymer.mishtu.BuildConfig
import com.microsoft.mobile.polymer.mishtu.kaizala_utils.TimestampUtils
import com.microsoft.mobile.polymer.mishtu.storage.SharedPreferenceStore
import com.microsoft.mobile.polymer.mishtu.storage.entities.ActiveSubscription

import com.microsoft.mobile.polymer.mishtu.storage.entities.ContentDownload
import com.microsoft.mobile.polymer.mishtu.storage.entities.Order
import com.microsoft.mobile.polymer.mishtu.storage.repositories.ContentRepository
import com.microsoft.mobile.polymer.mishtu.storage.repositories.IncentivesRepository
import com.microsoft.mobile.polymer.mishtu.storage.repositories.OrderRepository
import com.microsoft.mobile.polymer.mishtu.storage.repositories.SubscriptionRepository

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ContentDetailsViewModel @Inject constructor(
    private val contentRepository: ContentRepository,
    private val orderRepository: OrderRepository,
    private val  incentivesRepository: IncentivesRepository,
    private val subscriptionRepository: SubscriptionRepository) : ViewModel() {

    var activePackLiveData = MutableLiveData<ActiveSubscription>()
    var activeOrderLiveData = MutableLiveData<Order>()

    /*fun getActiveOrderAndPackDetails(contentProviderId: String) {
        CoroutineScope(Dispatchers.Default).launch {
            val activeSubscription = orderRepository.getActiveSubscription(contentProviderId)
            activeSubscription?.let {
                val planEndDate = TimestampUtils.getDateFromUTCString(it.planEndDate)
                val days = TimestampUtils.getDaysDiff(planEndDate.time, System.currentTimeMillis())
                if (days >= 0) {
                    activePackLiveData.postValue(it)
                    return@launch
                }
            }
            val activeOrder = orderRepository.getActiveOrder(contentProviderId)
            activeOrder?.let {
                activeOrderLiveData.postValue(it)
            }
        }
    }*/

    fun getSubscriptionRepository(): SubscriptionRepository {
        return subscriptionRepository
    }

    fun addContinueWatchList(it: ContentDownload) {
        contentRepository.addWatchList(it)
    }

    fun recordContentWatchEvent(id: String) {
        CoroutineScope(Dispatchers.Default).launch {
            incentivesRepository.triggerContentStreamingEvent(id)
            //if (recorded) SharedPreferenceStore.getInstance().save(IncentivesRepository.showStreamingReward, id)
        }
    }
}