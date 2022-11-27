package com.msr.bine_sdk.cloud.models

import com.msr.bine_sdk.models.Error

data class ContentListResponse(
        val data: List<Content>?,
        val details: String?,
        val error: Error?,
        var continuationToken: String
)