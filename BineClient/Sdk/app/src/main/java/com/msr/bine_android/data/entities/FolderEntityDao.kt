package com.msr.bine_android.data.entities

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy

@Dao
interface FolderEntityDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vararg folder: FolderEntity)
}