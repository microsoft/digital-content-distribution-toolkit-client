package com.microsoft.mobile.polymer.mishtu.storage.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "SubscriptionPack")
data class SubscriptionPack (
    @PrimaryKey
    @ColumnInfo(name = "id") var id: String,
    @ColumnInfo(name = "provider_id") var contentProviderId: String,
    @ColumnInfo(name = "type") var type: String,
    @ColumnInfo(name = "title") var title: String,
    @ColumnInfo(name = "duration_days") var durationDays: Int,
    @ColumnInfo(name = "price") var price: Int,
    @ColumnInfo(name = "start_date") var startDate: String,
    @ColumnInfo(name = "end_date") var endDate: String,
    @ColumnInfo(name = "subscription_type") var subscriptionType: String,
    @ColumnInfo(name = "contentId_list") var contentIdList: ArrayList<String?>? = null,
    @ColumnInfo(name = "is_redeemable") var isReadable: Boolean,
    @ColumnInfo(name = "redemption_value") var redemptionValue: Int,
    @Ignore var contentList: ArrayList<Content>? = ArrayList()
    )

    : Serializable {
        constructor():this("","","","",0,0,"","","",null,false,0,null)
        enum class PackType(val value: String){
            SVOD("SVOD"),
            TVOD("TVOD")
        }
}

