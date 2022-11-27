package com.msr.bine_android.data.entities

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.msr.bine_sdk.hub.model.DownloadStatus

//Download Status
// 0 - Not downloaded
// 1 - In progress
// 2 - Queued
// 3 - Complete
// 4 - Failed
@Entity
data class Folder(
    @PrimaryKey val ID: String,
    @ColumnInfo(name = "parent") var parent: String?,
    @ColumnInfo(name = "has_childer") var hasChildren: Boolean,
    @ColumnInfo(name = "metadata_json") var metadataJson: String?,
    @ColumnInfo(name = "download_status") var downloadStatus: Int = DownloadStatus.NOT_DOWNLOADED.value,
    @ColumnInfo(name = "source") var source: Int?,
    @ColumnInfo(name = "download_type") var type: Int?,
    @ColumnInfo(name = "hub_id") var hubId: String?,
    @ColumnInfo(name = "download_progress") var progress: Int,
    @ColumnInfo(name = "download_count") var downloadCount: Int) {
}