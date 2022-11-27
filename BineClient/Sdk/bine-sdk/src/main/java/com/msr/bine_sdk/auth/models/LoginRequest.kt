package com.msr.bine_sdk.auth.models


data class LoginRequest(
        var phoneNumber: String,
        var otp: String)