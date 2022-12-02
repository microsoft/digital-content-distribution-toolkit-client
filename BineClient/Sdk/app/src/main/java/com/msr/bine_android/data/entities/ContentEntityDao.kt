// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.msr.bine_android.data.entities

import androidx.room.*

@Dao
interface ContentEntityDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg folder: ContentEntity)

    @Transaction
    @Query("SELECT * FROM Content")
    fun getContentWithFolders(): List<ContentFolderEntity>


    @Transaction
    @Query("SELECT * FROM Content  WhERE id = :id")
    suspend fun getContentById(id: String): ContentFolderEntity?

    @Transaction
    @Query("SELECT * FROM Content  WhERE id IN (:ids)")
   suspend fun getContentListByIds(ids: List<String>): List<ContentFolderEntity>
}