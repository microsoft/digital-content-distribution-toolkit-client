// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.msr.bine_sdk.cloud.models

data class ContentAvailabilityCountRequest(val contentId: String, val deviceIds: Array<String>) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ContentAvailabilityCountRequest

        if (contentId != other.contentId) return false
        if (!deviceIds.contentEquals(other.deviceIds)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = contentId.hashCode()
        result = 31 * result + deviceIds.contentHashCode()
        return result
    }
}

data class ContentAvailabilityRequest(val contentId: String, val deviceIds: Array<String>, val includeActiveContentCount: Boolean ){
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ContentAvailabilityRequest

        if (contentId != other.contentId) return false
        if (!deviceIds.contentEquals(other.deviceIds)) return false
        if(includeActiveContentCount != other.includeActiveContentCount) return false

        return true
    }

    override fun hashCode(): Int {
        var result = contentId.hashCode()
        result = 31 * result + deviceIds.contentHashCode()
        return result
    }
}