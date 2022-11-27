package com.microsoft.mobile.auth.dtos

data class VerifyPinRequest(val phoneNumber: String,
                            val pin: String,
                            val permissions: List<String>,
                            val userName: String,
                            val deviceId: String)