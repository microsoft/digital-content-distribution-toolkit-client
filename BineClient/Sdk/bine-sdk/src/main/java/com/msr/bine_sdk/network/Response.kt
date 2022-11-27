package com.msr.bine_sdk.network

import com.google.gson.annotations.SerializedName

data class Error(
    val status: Boolean,
    @SerializedName("detail")
    val details: String,
    val code: Int
)
