package com.microsoft.mobile.polymer.mishtu.storage.entities

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface SubscriptionPackDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg pack: SubscriptionPack)

    @Query("DELETE FROM SubscriptionPack")
    fun deleteAll()

    @Query("DELETE FROM SubscriptionPack where provider_id = :contentProviderId")
    fun delete(contentProviderId: String)

    @Transaction
    @Query("SELECT * FROM SubscriptionPack")
    fun getAllSubscriptionPack(): List<SubscriptionPack>?

    @Transaction
    @Query("SELECT * FROM SubscriptionPack WHERE id = :id")
    suspend fun getSubscriptionById(id: String): SubscriptionPack

    @Transaction
    @Query("SELECT * FROM SubscriptionPack WHERE id = :id")
    fun getSubscriptionByIdLiveData(id: String): LiveData<SubscriptionPack?>

    @Transaction
    @Query("SELECT * FROM SubscriptionPack WHERE provider_id = :id order by price")
    fun getSubscriptionByProviderId(id: String): List<SubscriptionPack>

    @Transaction
    @Query("SELECT sub.* FROM SubscriptionPack sub INNER JOIN " +
            "Content con ON con.provider_id = sub.provider_id " +
            "WHERE  UPPER(sub.title) LIKE :query " +
            "or UPPER(con.title) LIKE :query " +
            "or UPPER(con.genre) LIKE :query " +
            "or UPPER(con.artists) LIKE :query " +
            "or con.yor LIKE :query " +
            "or UPPER(con.language) LIKE :query " +
            "GROUP BY sub.id" )
    fun getSubscriptionByQuery(query: String): List<SubscriptionPack>?
}