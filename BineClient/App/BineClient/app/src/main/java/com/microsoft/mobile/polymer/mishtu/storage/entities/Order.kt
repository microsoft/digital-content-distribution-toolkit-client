package com.microsoft.mobile.polymer.mishtu.storage.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Order(
    @PrimaryKey
    @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "user_id") val userId: String,
    @ColumnInfo(name = "user_name") val userName: String?,
    @ColumnInfo(name = "retailer_id") val retailerId: String?,
    @ColumnInfo(name = "retailer_name") val retailerName: String?,
    @ColumnInfo(name = "status") val orderStatus: String,
    @ColumnInfo(name = "created_date") val orderCreatedDate: String,
    @ColumnInfo(name = "provider_id") var contentProviderId: String,
    @ColumnInfo(name = "subscription_id") var subscriptionId: String,
    @ColumnInfo(name = "price") var price: Int,
    @ColumnInfo(name = "duration") var duration: Int)
