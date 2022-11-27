package com.microsoft.mobile.polymer.mishtu.storage.repositories

import android.util.Log
import com.google.gson.Gson
import com.microsoft.mobile.polymer.mishtu.BuildConfig
import com.microsoft.mobile.polymer.mishtu.kaizala_utils.TimestampUtils
import com.microsoft.mobile.polymer.mishtu.storage.SharedPreferenceStore
import com.microsoft.mobile.polymer.mishtu.storage.snappyDB.LocalStorageException
import com.microsoft.mobile.polymer.mishtu.storage.snappyDB.NoSqlDBException
import com.microsoft.mobile.polymer.mishtu.storage.snappyDB.SnappyDB
import com.microsoft.mobile.polymer.mishtu.utils.BNConstants
import com.microsoft.mobile.polymer.mishtu.utils.BOConverter
import com.microsoft.mobile.polymer.mishtu.utils.RewardsEvent
import com.msr.bine_sdk.BineAPI
import com.msr.bine_sdk.cloud.IncentivesManager
import com.msr.bine_sdk.cloud.models.IncentivePlan
import com.msr.bine_sdk.hub.model.ConnectedHub
import org.greenrobot.eventbus.EventBus
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class IncentivesRepository internal constructor(private val mDB: SnappyDB) {
    companion object {
        private const val planFetchDate = "PlanFetchDate"
        private const val dailyLaunchRecordedDate = "DailyLaunchRecordedDate"
        private const val incentivePlan = "IncentivePlan"
        private const val firstLaunchSent = "FirstLaunchSent"
        const val showStreamingReward = "StreamingRewards"

        const val KEY_INCENTIVE_EVENT = "IncentiveEvent"
    }

    private var sharedPreferenceStore: SharedPreferenceStore = SharedPreferenceStore.getInstance()
    private val formatter = SimpleDateFormat(BNConstants.formatter, Locale.getDefault())

    suspend fun triggerAppStartEvents(): Boolean {
        var plan = getIncentivePlan()
        if (plan == null) {
            plan = fetchIncentivesPlan()
        }

        plan?.let {
            val isFirstLaunchSent = sharedPreferenceStore.get(firstLaunchSent)
            if (isFirstLaunchSent.isNullOrEmpty()) {
                val recorded = triggerAppInstallEvents(it)
                if (recorded) {
                    return recorded
                }
            }
            return triggerDailyLaunchEvent(it)
        }
        return false
    }

    suspend fun triggerContentStreamingEvent(contentId: String): Boolean {
        var plan = getIncentivePlan()
        if (plan == null) {
            plan = fetchIncentivesPlan()
        }
        val eventList = planContainsEvent(plan, BOConverter.EventType.CONTENT_STREAMED, null)
        if(eventList.isNotEmpty()) {
            eventList[0]?.let { eventDetail ->
                return recordEvent(BOConverter.EventType.CONTENT_STREAMED,
                        eventDetail.formula.firstOperand,
                        Date(),
                        contentId,
                        eventDetail.eventSubType,
                        true,null)
            }
        }
        return false
    }

    suspend fun triggerDownloadCompleteEvent(contentId: String): Boolean {
        var plan = getIncentivePlan()
        if (plan == null) {
            plan = fetchIncentivesPlan()
        }
        val eventList = planContainsEvent(plan, BOConverter.EventType.DOWNLOAD_COMPLETE, null)
        if (eventList.isNotEmpty()) {
            eventList[0]?.let { eventDetail ->
                return recordEvent(BOConverter.EventType.DOWNLOAD_COMPLETE,
                        eventDetail.formula.firstOperand,
                        Date(),
                        contentId,
                        eventDetail.eventSubType,
                        true,connectedHubDevice()?.id)
            }
        }else{
            return recordEvent(BOConverter.EventType.DOWNLOAD_COMPLETE,
                    0,
                    Date(),
                    contentId,
                    null,
                    false, connectedHubDevice()?.id)
        }
        return false
    }
    private fun connectedHubDevice(): ConnectedHub? {
        val hubString =
                SharedPreferenceStore.getInstance().get(SharedPreferenceStore.KEY_CONNECTED_HUB)
        if (!hubString.isNullOrEmpty()) {
            return Gson().fromJson(hubString, ConnectedHub::class.java)
        }
        return null
    }

    fun reset() {
        sharedPreferenceStore.save(dailyLaunchRecordedDate, "")
        sharedPreferenceStore.save(planFetchDate, "")
        sharedPreferenceStore.save(incentivePlan, "")
        sharedPreferenceStore.save(firstLaunchSent, "")
        sharedPreferenceStore.save(showStreamingReward, "")
        resetUnSyncedRecords()
    }

    fun expenseEventExist(): Boolean {
        val plan = getIncentivePlan()
        if (plan == null) {
            return false
        } else {
            val eventList = planContainsEvent(plan, BOConverter.EventType.CONSUMER_EXPENSE_SUBSCRIPTION_REDEEM, null)
            if (eventList.isNotEmpty()) {
                eventList[0]?.let {
                    return true
                }
            }
        }
        return false
    }

    fun planContainsEvent(eventType: BOConverter.EventType, contentProviderId: String?): List<IncentivePlan.PlanDetail?> {
        val plan = getIncentivePlan()
        return if (plan == null) {
            ArrayList()
        }
        else {
            planContainsEvent(plan, eventType, contentProviderId)
        }
    }

    suspend fun getPlanDetails(eventType: BOConverter.EventType): IncentivePlan.PlanDetail? {
        var plan = getIncentivePlan()
        if (plan == null) {
            plan = fetchIncentivesPlan()
        }
        val eventList = planContainsEvent(plan, eventType, null)
        if(eventList.isNotEmpty()) {
            eventList[0]?.let {
                return it
            }
        }
        return null
    }

    // Private
    private suspend fun fetchIncentivesPlan(): IncentivePlan? {
        if (shouldFetchPlan()) {
            val plan = BineAPI.Incentives().getIncentivePlan()
            plan.result?.let {
                saveIncentivePlan(it)
                return it
            } ?:
            return null
        }
        return null
    }

    private suspend fun recordEvent(eventType: BOConverter.EventType,
                                    firstOperand: Int,
                                    date: Date, id: String?,
                                    contentProviderId: String?,
                                    shouldTrigger: Boolean,
                                    deviceId: String?): Boolean {
        val event: IncentivesManager.IncentiveEventType = when (eventType) {
            BOConverter.EventType.FIRST_SIGN_IN-> IncentivesManager.IncentiveEventType.APP_INSTALL
            BOConverter.EventType.APP_ONCE_OPEN -> IncentivesManager.IncentiveEventType.APP_LAUNCH
            BOConverter.EventType.CONTENT_STREAMED -> IncentivesManager.IncentiveEventType.CONTENT_STREAMED
            BOConverter.EventType.ON_BOARDING_RATING -> IncentivesManager.IncentiveEventType.RATE_RETAILER
            BOConverter.EventType.DOWNLOAD_COMPLETE -> IncentivesManager.IncentiveEventType.DOWNLOAD_COMPLETE

            else -> IncentivesManager.IncentiveEventType.APP_LAUNCH
        }

        val response = BineAPI.Incentives().recordEvent(event, date, id, deviceId)
        response.result?.let {
            if (it) {
                postEventSuccess(eventType, firstOperand,contentProviderId, shouldTrigger)
            }
            return true
        } ?:
        response.details?.contains("INC_ERR_0032")?.let {
            postEventSuccess(eventType, firstOperand,contentProviderId, false)
            return false
        }?:
        response.error?.let {
            saveFailedEvent(eventType, firstOperand, date, id, contentProviderId)
            return false
        }
        return false
    }

    private fun postEventSuccess(eventType: BOConverter.EventType,
                                 firstOperand: Int,
                                 contentProviderId: String?,
                                 shouldTrigger: Boolean) {
        when(eventType) {
            BOConverter.EventType.ON_BOARDING_RATING -> {}
            BOConverter.EventType.FIRST_SIGN_IN -> {
                sharedPreferenceStore.save(firstLaunchSent, "true")
                if (shouldTrigger) {
                    //Setting the dailyLaunchRecordedDate as DailyLaunch need not show on installed date
                    sharedPreferenceStore.save(dailyLaunchRecordedDate, formatter.format(Date()))
                }
            }
            BOConverter.EventType.APP_ONCE_OPEN -> {
                sharedPreferenceStore.save(dailyLaunchRecordedDate, formatter.format(Date()))
            }
            BOConverter.EventType.CONTENT_STREAMED -> {}
            BOConverter.EventType.CONSUMER_EXPENSE_SUBSCRIPTION_REDEEM -> {}
            BOConverter.EventType.CONSUMER_INCOME_ORDER_COMPLETED -> {}
            else -> {}
        }

        if (shouldTrigger) {
            EventBus.getDefault().post(RewardsEvent(firstOperand, eventType, contentProviderId))
        }
    }

    //Schedule Events
    private suspend fun triggerAppInstallEvents(plan: IncentivePlan): Boolean {
        //If contains app launch event 1. Shared Pref 2. DB record = return next
        val planList = planContainsEvent(plan, BOConverter.EventType.FIRST_SIGN_IN, null)
        if (planList.isNotEmpty()) {
            planList[0]?.let {
                return recordEvent(BOConverter.EventType.FIRST_SIGN_IN,
                        it.formula.firstOperand,
                        Date(),
                        null,
                        it.eventSubType,
                        true,null)
            }
        }
        return false
    }

    private suspend fun triggerDailyLaunchEvent(plan: IncentivePlan): Boolean {
        val eventList = planContainsEvent(plan, BOConverter.EventType.APP_ONCE_OPEN, null)
        if (eventList.isNotEmpty()) {
            eventList[0]?.let { eventDetail ->
                //If contains daily launch for date in SharedPrefs = return next
                val lastRecordedDate = sharedPreferenceStore.get(dailyLaunchRecordedDate)
                if (lastRecordedDate.isNullOrEmpty()) {
                    return recordEvent(BOConverter.EventType.APP_ONCE_OPEN,
                            eventDetail.formula.firstOperand,
                            Date(),
                            null,
                            eventDetail.eventSubType,
                            true,null)
                } else {
                    formatter.parse(lastRecordedDate).let {
                        if (TimestampUtils.isDateBeforeToday(it)) {
                            return recordEvent(BOConverter.EventType.APP_ONCE_OPEN,
                                    eventDetail.formula.firstOperand,
                                    Date(),
                                    null,
                                    eventDetail.eventSubType,
                                    true,null)
                        }
                    }
                }
            }
        }
        return false
    }

    private fun planContainsEvent(plan: IncentivePlan?, eventType: BOConverter.EventType, contentProviderId: String?):
            List<IncentivePlan.PlanDetail?> {
        val planList = ArrayList<IncentivePlan.PlanDetail>()
        plan?.planDetails?.let {
            for (planDetail in it) {
                if (planDetail.eventType == eventType.string) {
                    if(contentProviderId != null){
                        if((planDetail.eventSubType == null) || contentProviderId == planDetail.eventSubType) {
                            planList.add(planDetail)
                        }
                    }else{
                        planList.add(planDetail)
                    }

                }
            }
        }
        return planList
    }

    private fun shouldFetchPlan(): Boolean {
        val plan = getIncentivePlan()
        //Check if plan exist - NO: Fetch
        if (plan == null) return true
        else {
            val date = formatter.parse(plan.endDate)
            date?.let {
                //Check if plan expired - Yes: Fetch
                if (Date().after(date)) {
                    //Clear Plan
                    saveIncentivePlan(null)
                    return true
                }

                //Check if Fetched today - No: Fetch
                else {
                    val fetchDateStr = sharedPreferenceStore.get(planFetchDate)
                    if (fetchDateStr.isNullOrEmpty()) return true
                    val fetchDate = formatter.parse(fetchDateStr)
                    fetchDate?.let {
                        val difference: Long = Date().time - it.time
                        val hours = difference / (60 * 60 * 1000)
                        if (hours >= 24) return true
                    }
                }
            }
        }
        return false
    }

    private fun saveIncentivePlan(plan: IncentivePlan?) {
        if (plan == null) {
            sharedPreferenceStore.save(incentivePlan, "")
            sharedPreferenceStore.save(planFetchDate, "")
        }
        val jsonString = Gson().toJson(plan)
        sharedPreferenceStore.save(incentivePlan, jsonString)
        sharedPreferenceStore.save(planFetchDate, formatter.format(Date()))
    }

    private fun getIncentivePlan(): IncentivePlan? {
        val jsonString = sharedPreferenceStore.get(incentivePlan)
        return Gson().fromJson(jsonString, IncentivePlan::class.java)
    }

    private fun saveFailedEvent(eventType: BOConverter.EventType, firstOperand: Int, date: Date, id: String?, contentProviderId: String?) {
        try {
            //IncentiveEvent/Type/Date/id
            mDB.putInt(KEY_INCENTIVE_EVENT + "/"
                    + eventType.string + "/"
                    + formatter.format(date)
                    + "/" + id +"/"+contentProviderId , firstOperand)
        } catch (e: NoSqlDBException) {
            e.printStackTrace()
            throw LocalStorageException(e)
        }
    }

    suspend fun syncFailedEvent() {
        for (key in mDB.findKeysByPrefix(KEY_INCENTIVE_EVENT)) {
            val firstOperand = mDB.getInt(key)
            val eventProperties = key.split("/")
            val date = formatter.parse(eventProperties[2]) as Date
            val id = if (eventProperties[3].isEmpty()) null else eventProperties[3]
            val contentProviderId = if (eventProperties[4].isEmpty()) null else eventProperties[4]
            if (eventProperties.isNotEmpty() && eventProperties.size == 4) {
                val recorded = BOConverter.EventType.fromValue(eventProperties[1])?.let {
                    recordEvent(
                        it,
                        firstOperand,
                        date,
                        id,
                        contentProviderId,
                        false,null)
                }
                if (recorded == true) mDB.deleteKey(key)
                else {
                    val difference: Long = Date().time - date.time
                    val hours = difference / (24 * 60 * 60 * 1000)
                    if (hours >= 30)  mDB.deleteKey(key)
                }
            }
        }
    }

    private fun resetUnSyncedRecords() {
        for (key in mDB.findKeysByPrefix(KEY_INCENTIVE_EVENT)) {
            mDB.deleteKey(key)
        }
    }

    fun setAppInstallSharedPrefs(isNewUser: Boolean) {
        sharedPreferenceStore.save(firstLaunchSent, if (isNewUser) "" else "true")
    }
}