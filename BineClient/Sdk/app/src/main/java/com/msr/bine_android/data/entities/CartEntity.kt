package com.msr.bine_android.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Cart")
data class CartEntity(@PrimaryKey val id: String) {
}