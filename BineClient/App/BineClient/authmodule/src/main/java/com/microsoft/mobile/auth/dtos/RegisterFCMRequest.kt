package com.microsoft.mobile.auth.dtos

data class RegisterFCMRequest(private val fcid: String, private val ct:String, private val tcs: List<String>)
