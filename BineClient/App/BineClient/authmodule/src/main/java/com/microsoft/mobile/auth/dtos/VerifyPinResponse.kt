package com.microsoft.mobile.auth.dtos

data class VerifyPinResponse(val userId: String,
                             val clientId: String,
                             val isNewUser: Boolean,
                             val authDetails: String,
                             val authVerificationDetail: String,
                             val authenticationToken: String,
                             val tokenCreationTime: Long,
                             val homeDSUrl: String,
                             val homeKMSUrl: String,
                             val homeAPIUrl: String
)