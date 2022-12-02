// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.microsoft.mobile.polymer.mishtu.storage.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Downloads")
class Downloads (
    @PrimaryKey
    @ColumnInfo(name = "content_id") val contentId: String,
    @ColumnInfo(name = "download_url") val downloadUrl: String,
    @ColumnInfo(name = "download_status") val downloadStatus: Int,
    @ColumnInfo(name = "download_progress") val downloadProgress: Int)