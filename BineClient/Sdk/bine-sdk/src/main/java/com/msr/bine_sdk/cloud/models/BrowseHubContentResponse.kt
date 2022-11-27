package com.msr.bine_sdk.cloud.models

data class BrowseHubContentResponse(var data: Array<String>, var continuationToken: String) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BrowseHubContentResponse

        if (!data.contentEquals(other.data)) return false
        if (continuationToken != other.continuationToken) return false

        return true
    }

    override fun hashCode(): Int {
        var result = data.contentHashCode()
        result = 31 * result + continuationToken.hashCode()
        return result
    }
}
