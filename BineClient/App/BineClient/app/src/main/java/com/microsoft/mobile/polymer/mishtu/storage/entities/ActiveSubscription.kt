package com.microsoft.mobile.polymer.mishtu.storage.entities

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.microsoft.mobile.polymer.mishtu.kaizala_utils.TimestampUtils
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

@Entity
data class ActiveSubscription(
    @PrimaryKey
    @ColumnInfo(name ="c_id") val providerId: String,
    @ColumnInfo(name ="amount_collected") val amountCollected: Int,
    @ColumnInfo(name ="plan_start_date") val planStartDate: String,
    @ColumnInfo(name ="plan_end_date") val planEndDate: String,
    @Embedded val subscription: SubscriptionPack) {

    fun isExpired(): Boolean {
        val dateFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        val endDate: Date? = dateFormat.parse(planEndDate)
        endDate ?: return false
        return Date(System.currentTimeMillis()).after(endDate)
    }

    fun getDaysToExpire(): Long {
        val planEndDate = TimestampUtils.getDateFromUTCString(planEndDate)
        return TimestampUtils.getDaysDiff(planEndDate.time, System.currentTimeMillis())
    }
}
