// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.microsoft.mobile.polymer.mishtu.storage.entities

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface DownloadsDao {
    @Query("UPDATE Downloads SET download_status = :status, download_progress = :progress WHERE content_id = :contentId")
    fun updateDownloadStatus(contentId: String, status: Int, progress: Int)

    @Query("DELETE FROM Downloads WHERE content_id = :contentId")
    fun deleteDownload(contentId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg download: Downloads)

    @Query("DELETE FROM Downloads")
    fun deleteAll()

    @Query("SELECT * FROM Downloads")
    fun getAllDownloads(): LiveData<List<Downloads>>
}