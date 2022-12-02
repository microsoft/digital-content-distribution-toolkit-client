// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.microsoft.mobile.auth.dtos

enum class AuthError {
    INVALID_PHONE,
    INVALID_OTP,
    EXCEED_ATTEMPT,
    API_ERROR,
    UNKNOWN_ERROR,
    NETWORK_ERROR
}