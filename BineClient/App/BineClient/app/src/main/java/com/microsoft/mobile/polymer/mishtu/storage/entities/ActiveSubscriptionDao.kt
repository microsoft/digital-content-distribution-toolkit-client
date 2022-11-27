package com.microsoft.mobile.polymer.mishtu.storage.entities

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ActiveSubscriptionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg activeSubscription: ActiveSubscription)

    @Query("DELETE FROM ActiveSubscription")
    fun deleteAll()

    @Query("DELETE FROM ActiveSubscription WHERE id = (:subscriptionId)")
    fun delete(subscriptionId: String)

    @Transaction
    @Query("SELECT * FROM ActiveSubscription WHERE c_id = :providerId")
    suspend fun getActiveSubscriptionForProviderId(providerId: String): List<ActiveSubscription>?

    @Transaction
    @Query("SELECT * FROM ActiveSubscription WHERE provider_id = (:contentProviderId)")
    fun getActiveSubscriptionLiveData(contentProviderId: String): LiveData<List<ActiveSubscription>?>

    @Transaction
    @Query("SELECT * FROM ActiveSubscription")
    fun getAllActiveSubscriptionLiveData(): LiveData<List<ActiveSubscription>?>

    @Transaction
    @Query("SELECT * FROM ActiveSubscription Where id = :subscriptionId")
    fun getActiveSubscriptionBySubscriptionIdLiveData(subscriptionId: String): LiveData<ActiveSubscription?>

    @Transaction
    @Query("SELECT * FROM ActiveSubscription Where id = :subscriptionId")
    suspend fun getActiveSubscriptionBySubscriptionId(subscriptionId: String): ActiveSubscription?


}