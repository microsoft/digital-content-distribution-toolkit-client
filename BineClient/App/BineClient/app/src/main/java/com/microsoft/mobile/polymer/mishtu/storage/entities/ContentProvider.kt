package com.microsoft.mobile.polymer.mishtu.storage.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.microsoft.mobile.polymer.mishtu.BuildConfig

@Entity(tableName = "ContentProvider")
data class ContentProvider(
    @PrimaryKey
    @ColumnInfo(name = "provider_id") val id: String,
    @ColumnInfo(name = "name") val name:String,
    @ColumnInfo(name = "logo_url") val logoUrl: String?,
    @ColumnInfo(name = "is_active") val isActive: Boolean)


