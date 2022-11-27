package com.msr.bine_sdk.cloud.models

import com.google.gson.annotations.SerializedName

data class ContentProvider (
        @SerializedName("contentProviderId")
        val id: String,
        val name:String,
        val logoUrl: String,
        val isActive: Boolean)