// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.microsoft.mobile.auth.dtos

data class VerifyPhoneRequest(private val phoneNumber: String,
                              private val useVoice: Boolean,
                              private val PinFormatType: String,
                              private val deviceId: String)