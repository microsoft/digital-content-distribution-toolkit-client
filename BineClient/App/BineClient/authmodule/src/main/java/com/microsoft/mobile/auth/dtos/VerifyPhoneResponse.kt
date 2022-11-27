package com.microsoft.mobile.auth.dtos

data class VerifyPhoneResponse(val mode: String,
                               val accountStatus: String,
                               val authType: String,
                               val remainingAttempts: Int,
                               val lockEndTime: Long,
                               val authDetails: String,
                               val smsProvider: String,
                               val isVoiceOtpEnabled: Boolean
)