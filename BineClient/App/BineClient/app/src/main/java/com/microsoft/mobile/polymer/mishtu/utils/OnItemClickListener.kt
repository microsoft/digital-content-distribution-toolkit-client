// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.microsoft.mobile.polymer.mishtu.utils

import com.microsoft.mobile.polymer.mishtu.storage.entities.ContentDownload

interface OnItemClickListener {
    fun onItemCLicked(content: ContentDownload)
}