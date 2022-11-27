package com.microsoft.mobile.polymer.mishtu.storage.entities

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface OrderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg order: Order)

    @Query("DELETE FROM `Order` WHERE provider_id = :providerId")
    fun deleteOrderForProviderId(providerId: String)

    @Query("DELETE FROM `Order` WHERE subscription_id = :subscriptionId")
    fun deleteOrderForSubscriptionId(subscriptionId: String)

    @Query("DELETE FROM `Order` WHERE id = :orderId")
    fun deleteOrderByOrderId(orderId: String)

    @Query("DELETE FROM `Order`")
    fun deleteAll()

    @Transaction
    @Query("SELECT * FROM `Order` WHERE id = :oderId")
    suspend fun getOrderForId(oderId: String): Order?

    @Transaction
    @Query("SELECT * FROM `Order` WHERE provider_id = :providerId")
    suspend fun getOrderForProviderId(providerId: String): List<Order>?

    @Transaction
    @Query("SELECT * FROM `Order` WHERE `Order`.provider_id = (:contentProviderId)")
    fun getOrderLiveData(contentProviderId: String): LiveData<List<Order>?>
}