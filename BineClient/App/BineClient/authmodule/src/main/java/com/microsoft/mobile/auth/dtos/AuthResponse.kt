// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.microsoft.mobile.auth.dtos

sealed class AuthResponse<out T : Any> {
    data class VerifyPhone(val accountStatus: String?, val details:String?, val error: AuthError?) {
        constructor(accountStatus: String, details: String?) : this(accountStatus, details, null)
        constructor(details:String?, error: AuthError?) : this(null, details, error)
    }
    data class VerifyPin(val userId: String?, val authToken: String?, val isNewUSer: Boolean?, val details:String?, val error: AuthError?) {
        constructor(userId: String, authToken: String, isNewUSer: Boolean) : this(userId, authToken, isNewUSer, null, null)
        constructor(details:String?, error: AuthError) : this(null, null, null, details, error)
    }

    data class RefreshToken(val authToken: String?, val code: Int?, val details:String?, val error: AuthError?) {
        constructor(authToken: String?) : this(authToken, null, null, null)
        constructor(code: Int?, details:String?, error: AuthError) : this( null, code, details, error)
    }

    data class FCMToken(val success: Boolean?, val code: Int?, val details:String?, val error: AuthError?) {
        constructor(success: Boolean?) : this( success, null, null, null)
        constructor(code: Int?, details:String?, error: AuthError) : this( null, code, details, error)
    }
}
