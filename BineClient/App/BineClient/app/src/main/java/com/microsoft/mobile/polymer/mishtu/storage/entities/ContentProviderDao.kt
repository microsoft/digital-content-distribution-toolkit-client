package com.microsoft.mobile.polymer.mishtu.storage.entities

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ContentProviderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg provider: ContentProvider)

    @Query("DELETE FROM ContentProvider")
    fun deleteAll()

    @Transaction
    @Query("SELECT * FROM ContentProvider")
    suspend fun getAllProviders(): List<ContentProvider>

    @Transaction
    @Query("SELECT * FROM ContentProvider WHERE name = :providerName")
    suspend fun getProviderByName(providerName: String): ContentProvider?
}