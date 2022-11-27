package com.msr.bine_sdk.cloud.models

import com.msr.bine_sdk.models.Error

data class InitResponse(
    var userId:String?,
    var details: String?,
    var error: Error?
)
