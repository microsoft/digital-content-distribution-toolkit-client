package com.msr.bine_sdk.cloud.models

import java.io.Serializable

data class Content(val contentId: String,
                   val contentProviderContentId: String,
                   val contentProviderId: String,
                   val title: String,
                   val shortDescription: String,
                   val longDescription: String,
                   val additionalDescription1: String,
                   val additionalDescription2: String,
                   val additionalTitle1: String,
                   val additionalTitle2: String,
                   val genre: String,
                   val yearOfRelease: String,
                   val language: String,
                   val durationInMts: Float,
                   val rating: String,
                   val mediaFileName: String,
                   val dashUrl: String,
                   val isFreeContent: Boolean,
                   val isHeaderContent: Boolean,
                   val isExclusiveContent: Boolean,
                   val people: List<Artist>,
                   val hierarchy: String?,
                   val attachments: List<Attachment>,
                   val createdDate: String,
                   val ageAppropriateness:String?,
                   val contentAdvisory: String?,
                   val audioTarFileSize: Int,
                   val videoTarFileSize: Int,
                   val broadcastedBy: ContentBroadcastedBy?
)
{
    data class Artist(var name: String, var role: String): Serializable
    data class Attachment(val name: String, val type: String): Serializable
    data class ContentBroadcastedBy(val broadcastRequest: BroadcastRequest): Serializable
    data class BroadcastRequest(val startDate: String, val endDate: String): Serializable

    fun size(): Int {
        return audioTarFileSize + videoTarFileSize
    }

    fun getThumbnailLandscapeImage(): String {
        if (attachments.isNotEmpty() && attachments.size > 1) {
            if (attachments[1].type.equals("Thumbnail", true)) {
                return contentProviderId + "-cdn/" + contentId + "/" + attachments[1].name
            }
        }
        return ""
    }
}
