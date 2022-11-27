package com.msr.bine_android.data.entities

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.Gson

@Entity(tableName = "Content")
data class ContentEntity(@PrimaryKey val id: String,
                         @ColumnInfo(name = "hit_count") val hitCount: Int,
                         @ColumnInfo(name = "payment_type") val paymentType: Int,
                         @ColumnInfo(name = "content_json") val recommendedContentJson: String?) {

    fun getRecommendedFolders(): Array<ContentEntity>? {
        return Gson().fromJson(recommendedContentJson, Array<ContentEntity>::class.java)
    }
}