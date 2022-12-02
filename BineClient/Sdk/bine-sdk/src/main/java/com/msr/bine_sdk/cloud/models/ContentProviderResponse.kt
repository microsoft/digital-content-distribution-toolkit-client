// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.msr.bine_sdk.cloud.models

import com.msr.bine_sdk.models.Error

data class ContentProviderResponse(val contentProviders: List<ContentProvider>?,
                                   val details: String?,
                                   val error: Error?)
