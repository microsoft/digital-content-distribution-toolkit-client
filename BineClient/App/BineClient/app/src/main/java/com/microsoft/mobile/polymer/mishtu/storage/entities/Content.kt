package com.microsoft.mobile.polymer.mishtu.storage.entities

import androidx.room.*
import com.microsoft.mobile.polymer.mishtu.BuildConfig
import com.msr.bine_sdk.cloud.models.Content.*
import java.io.Serializable
import java.util.*

import kotlin.collections.ArrayList


@Entity(tableName = "Content")
data class Content (
    @PrimaryKey
    @ColumnInfo(name = "content_id") val contentId: String,
    @ColumnInfo(name = "provider_id") val contentProviderId: String,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "short_description") val shortDescription: String?,
    @ColumnInfo(name = "long_description") val longDescription: String?,
    @ColumnInfo(name = "additional_desc_1") val additionalDescription1: String?,
    @ColumnInfo(name = "additional_desc_2") val additionalDescription2: String?,
    @ColumnInfo(name = "additional_title_1") val additionalTitle1: String?,
    @ColumnInfo(name = "additional_title_2") val additionalTitle2: String?,
    @ColumnInfo(name = "genre") val genre: String,
    @ColumnInfo(name = "yor") val yearOfRelease: String?,
    @ColumnInfo(name = "language") val language: String,
    @ColumnInfo(name = "duration") val durationInMts: Float,
    @ColumnInfo(name = "rating") val rating: String?,
    @ColumnInfo(name = "artists") val artists: List<Artist> = ArrayList(),
    @ColumnInfo(name = "media_filepath") val mediaFilePath: String?,
    @ColumnInfo(name = "dash_url") val dashUrl: String,
    @ColumnInfo(name = "hierarchy") val hierarchy: String?,
    @ColumnInfo(name = "free") val free: Boolean,
    @ColumnInfo(name = "is_header") val isHeaderContent: Boolean,
    @ColumnInfo(name = "is_exclusive") val isExclusiveContent: Boolean,
    @ColumnInfo(name = "attachments") val contentAttachments: List<Attachment> = ArrayList(),
    @ColumnInfo(name = "created_date") val createdDate: Date,
    @ColumnInfo(name = "name") val name: String?,
    @ColumnInfo(name = "season") val season: String?,
    @ColumnInfo(name = "episode") val episode: String?,
    @ColumnInfo(name = "is_movie") val isMovie: Boolean,
    @ColumnInfo(name = "ageAppropriateness") val ageAppropriateness: String?,
    @ColumnInfo(name = "contentAdvisory") val contentAdvisory: String?,
    @ColumnInfo(name = "video_file_size") val videoTarFileSize: Int?,
    @ColumnInfo(name = "audio_file_size") val audioTarFileSize: Int?,
    @ColumnInfo(name = "hubId") val hubId: String?,
    @ColumnInfo(name = "broadcast_date") val broadcastDate: Date): Serializable {

    fun hasTrailers(): Boolean {
        for (attachment in contentAttachments) {
            if (attachment.type.equals("Teaser", true)) {
                return true
            }
        }
        return false
    }

    fun getTrailer(): String {
        for (attachment in contentAttachments) {
            if (attachment.type.equals("Teaser", true)) {
                return BuildConfig.CDN_BASE_URL + contentProviderId + "-cdn/" + contentId + "/" + attachment.name
            }
        }
        return ""
    }

    fun getDisplayCast(): Pair<String?, String?>? {
        if (artists.isNotEmpty()) {
            var artistString = ""
            var directorString = ""
            for ((name, role) in artists) {
                if (role == "Actor") artistString =
                    if (artistString.isEmpty()) name else "$artistString, $name" else if (role == "Director") directorString =
                    if (directorString.isEmpty()) name else "$directorString, $name"
            }
            return Pair(artistString, directorString)
        }
        return null
    }

    fun getThumbnailImage(): String {
        for (attachment in contentAttachments) {
            if (attachment.type.equals("Thumbnail", true)) {
                return BuildConfig.CDN_BASE_URL + contentProviderId + "-cdn/" + contentId + "/" + attachment.name
            }
        }
        return ""
    }

    fun getThumbnailLandscapeImage(): String {
        for (attachment in contentAttachments) {
            if (attachment.type.equals("LandscapeThumbnail", true)) {
                return BuildConfig.CDN_BASE_URL + contentProviderId + "-cdn/" + contentId + "/" + attachment.name
            }
        }
        return ""
    }

    fun getSize(): Int {
        return (videoTarFileSize ?: 0) + (audioTarFileSize ?: 0)
    }

    fun getContentTitle(): String {
        return if(isMovie) title else {additionalTitle2 ?: title}
    }
}