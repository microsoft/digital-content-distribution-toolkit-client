// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.msr.bine_sdk.auth.models

import com.msr.bine_sdk.cloud.models.Token
import com.msr.bine_sdk.models.Error

data class LoginResponse(

    var hub_token: Token?,

    var app_token: Token?,

    var user: String?,

    var status:Boolean?,

    var details: String?,

    var statusCode: Int?,

    var error: Error?)
{
    constructor( status:Boolean, details: String, networkError: Error) :
            this(null,
                    null,
                    null,
                    status,
                    details,
                    0,
                    networkError)

    constructor( status:Boolean) :
            this(null,
                    null,
                    null,
                    status,
                    null,
                    200,
                    null)
    constructor() :
            this(null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null)

}