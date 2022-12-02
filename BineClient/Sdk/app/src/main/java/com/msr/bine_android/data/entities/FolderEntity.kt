// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.msr.bine_android.data.entities

import androidx.room.*

@Entity(tableName = "Folders",
        foreignKeys = [
            ForeignKey(entity = ContentEntity::class, parentColumns = ["id"], childColumns = ["content_id"])
        ],
        indices = [Index("content_id")])
data class FolderEntity(
        @PrimaryKey @ColumnInfo(name = "id") val folderId: String,
        @ColumnInfo(name = "content_id") val contentId: String,
        @ColumnInfo(name = "title") val title: String,
        @ColumnInfo(name = "sequence") val sequence: Int,
        @ColumnInfo(name = "description") val description: String,
        @ColumnInfo(name = "video_filename") val videoFileName: String,
        @ColumnInfo(name = "audio_filename") val audioFileName: String,
        @ColumnInfo(name = "thumbnail") val thumbnail: String,
        @ColumnInfo(name = "size") val size: Float,
        @ColumnInfo(name = "duration") val duration: Int,
        @ColumnInfo(name = "path") val path: String,
        @ColumnInfo(name = "mpd_filename") val mpdFileName: String,
        @ColumnInfo(name = "folder_url") val folderUrl: String)
