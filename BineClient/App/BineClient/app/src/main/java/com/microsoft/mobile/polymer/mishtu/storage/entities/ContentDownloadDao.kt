package com.microsoft.mobile.polymer.mishtu.storage.entities

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.microsoft.mobile.polymer.mishtu.BuildConfig
import java.io.Serializable
import java.util.*
import kotlin.collections.ArrayList

@Dao
interface ContentDownloadDao {
    @Transaction
    @Query("SELECT Content.content_id as contentId, " +
            "Content.provider_id as contentProviderId, " +
            "Content.title as title, " +
            "Content.additional_desc_1 as additionalDescription1, " +
            "Content.additional_desc_2 as additionalDescription2, " +
            "Content.additional_title_1 as additionalTitle1, " +
            "Content.additional_title_2 as additionalTitle2, " +
            "Content.long_description as longDescription, " +
            "Content.short_description as shortDescription, " +
            "Content.duration as durationInMts, " +
            "Content.video_file_size as videoTarFileSize," +
            "Content.audio_file_size as audioTarFileSize, " +
            "Content.yor as yearOfRelease, " +
            "Content.ageAppropriateness as ageAppropriateness, " +
            "Content.genre as genre, " +
            "Content.language as language, " +
            "Content.artists as artists, " +
            "Content.attachments as contentAttachments, " +
            "Content.dash_url as dashUrl, " +
            "Content.name as name, " +
            "Content.season as season, " +
            "Content.episode as episode, " +
            "Content.free as free, " +
            "Content.is_movie as isMovie, " +
            "Downloads.download_progress as downloadProgress, " +
            "Downloads.download_status as downloadStatus, " +
            "Downloads.download_url as downloadUrl " +
            "FROM Content LEFT JOIN Downloads " +
            "ON Content.content_id = Downloads.content_id " +
            "WHERE Content.hubId = (:hubId) AND (IFNULL(Downloads.download_status, 0) = 1 OR IFNULL(Downloads.download_status, 0) = 2) " +
            "ORDER BY Downloads.download_status")
    fun getDownloadingContent(hubId: String): LiveData<List<ContentDownload>>

    @Transaction
    @Query("SELECT Content.content_id as contentId, " +
            "Content.provider_id as contentProviderId, " +
            "Content.title as title, " +
            "Content.additional_desc_1 as additionalDescription1, " +
            "Content.additional_desc_2 as additionalDescription2, " +
            "Content.additional_title_1 as additionalTitle1, " +
            "Content.additional_title_2 as additionalTitle2, " +
            "Content.long_description as longDescription, " +
            "Content.short_description as shortDescription, " +
            "Content.duration as durationInMts, " +
            "Content.video_file_size as videoTarFileSize," +
            "Content.audio_file_size as audioTarFileSize, " +
            "Content.yor as yearOfRelease, " +
            "Content.ageAppropriateness as ageAppropriateness, " +
            "Content.genre as genre, " +
            "Content.language as language, " +
            "Content.artists as artists, " +
            "Content.attachments as contentAttachments, " +
            "Content.dash_url as dashUrl, " +
            "Content.name as name, " +
            "Content.season as season, " +
            "Content.episode as episode, " +
            "Content.free as free, " +
            "Content.is_movie as isMovie, " +
            "Downloads.download_progress as downloadProgress, " +
            "Downloads.download_status as downloadStatus, " +
            "Downloads.download_url as downloadUrl " +
            "FROM Content JOIN Downloads " +
            "ON Content.content_id = Downloads.content_id " +
            "WHERE Content.hubId = (:hubId) AND IFNULL(Downloads.download_status, 0) = 1 " +
            "ORDER BY Downloads.download_status DESC")
    fun getInProgressContent(hubId: String): LiveData<List<ContentDownload>>

    @Transaction
    @Query("SELECT Content.content_id as contentId, " +
            "Content.provider_id as contentProviderId, " +
            "Content.title as title, " +
            "Content.additional_desc_1 as additionalDescription1, " +
            "Content.additional_desc_2 as additionalDescription2, " +
            "Content.additional_title_1 as additionalTitle1, " +
            "Content.additional_title_2 as additionalTitle2, " +
            "Content.long_description as longDescription, " +
            "Content.short_description as shortDescription, " +
            "Content.duration as durationInMts, " +
            "Content.video_file_size as videoTarFileSize," +
            "Content.audio_file_size as audioTarFileSize, " +
            "Content.yor as yearOfRelease, " +
            "Content.ageAppropriateness as ageAppropriateness, " +
            "Content.genre as genre, " +
            "Content.language as language, " +
            "Content.artists as artists, " +
            "Content.attachments as contentAttachments, " +
            "Content.dash_url as dashUrl, " +
            "Content.name as name, " +
            "Content.season as season, " +
            "Content.episode as episode, " +
            "Content.free as free, " +
            "Content.is_movie as isMovie, " +
            "Downloads.download_progress as downloadProgress, " +
            "Downloads.download_status as downloadStatus, " +
            "Downloads.download_url as downloadUrl " +
            "FROM Content JOIN Downloads " +
            "ON Content.content_id = Downloads.content_id " +
            "AND Downloads.download_status = 3 " +
            "GROUP BY Content.name ")
    fun getDownloadedContent(): LiveData<List<ContentDownload>>

    @Transaction
    @Query("SELECT Content.content_id as contentId, " +
            "Content.provider_id as contentProviderId, " +
            "Content.title as title, " +
            "Content.additional_desc_1 as additionalDescription1, " +
            "Content.additional_desc_2 as additionalDescription2, " +
            "Content.additional_title_1 as additionalTitle1, " +
            "Content.additional_title_2 as additionalTitle2, " +
            "Content.long_description as longDescription, " +
            "Content.short_description as shortDescription, " +
            "Content.duration as durationInMts, " +
            "Content.video_file_size as videoTarFileSize," +
            "Content.audio_file_size as audioTarFileSize, " +
            "Content.yor as yearOfRelease, " +
            "Content.ageAppropriateness as ageAppropriateness, " +
            "Content.genre as genre, " +
            "Content.language as language, " +
            "Content.artists as artists, " +
            "Content.attachments as contentAttachments, " +
            "Content.dash_url as dashUrl, " +
            "Content.name as name, " +
            "Content.season as season, " +
            "Content.episode as episode, " +
            "Content.free as free, " +
            "Content.is_movie as isMovie, " +
            "Downloads.download_progress as downloadProgress, " +
            "Downloads.download_status as downloadStatus, " +
            "Downloads.download_url as downloadUrl " +
            "FROM Content JOIN Downloads " +
            "ON Content.content_id = Downloads.content_id " +
            "AND Downloads.download_status = 3 " +
            "AND Content.provider_id = (:contentProviderId) " +
            "AND Content.name = (:seriesName) " +
            "ORDER BY Content.season, Content.episode, Content.is_header DESC, Content.broadcast_date " )
    fun getDownloadedEpisodes(seriesName: String, contentProviderId: String): LiveData<List<ContentDownload>>

    @Transaction
    @Query("SELECT Content.content_id as contentId, " +
            "Content.provider_id as contentProviderId, " +
            "Content.title as title, " +
            "Content.long_description as longDescription, " +
            "Content.short_description as shortDescription, " +
            "Content.additional_desc_1 as additionalDescription1, " +
            "Content.additional_desc_2 as additionalDescription2, " +
            "Content.additional_title_1 as additionalTitle1, " +
            "Content.additional_title_2 as additionalTitle2, " +
            "Content.duration as durationInMts, " +
            "Content.video_file_size as videoTarFileSize," +
            "Content.audio_file_size as audioTarFileSize, " +
            "Content.yor as yearOfRelease, " +
            "Content.ageAppropriateness as ageAppropriateness, " +
            "Content.genre as genre, " +
            "Content.language as language, " +
            "Content.artists as artists, " +
            "Content.attachments as contentAttachments, " +
            "Content.dash_url as dashUrl, " +
            "Content.name as name, " +
            "Content.season as season, " +
            "Content.episode as episode, " +
            "Content.free as free, " +
            "Content.is_movie as isMovie, " +
            "Downloads.download_progress as downloadProgress, " +
            "Downloads.download_status as downloadStatus, " +
            "Downloads.download_url as downloadUrl " +
            "FROM Content JOIN Downloads " +
            "ON Content.content_id = Downloads.content_id " +
            "AND Downloads.download_status = 3")
    fun downloadedContent(): List<ContentDownload>

    //Get All Movies
    @Transaction
    @Query("SELECT Content.content_id as contentId, " +
            "Content.provider_id as contentProviderId, " +
            "Content.title as title, " +
            "Content.additional_desc_1 as additionalDescription1, " +
            "Content.additional_desc_2 as additionalDescription2, " +
            "Content.additional_title_1 as additionalTitle1, " +
            "Content.additional_title_2 as additionalTitle2, " +
            "Content.long_description as longDescription, " +
            "Content.short_description as shortDescription, " +
            "Content.duration as durationInMts, " +
            "Content.video_file_size as videoTarFileSize," +
            "Content.audio_file_size as audioTarFileSize, " +
            "Content.yor as yearOfRelease, " +
            "Content.ageAppropriateness as ageAppropriateness, " +
            "Content.genre as genre, " +
            "Content.language as language, " +
            "Content.artists as artists, " +
            "Content.attachments as contentAttachments, " +
            "Content.dash_url as dashUrl, " +
            "Content.name as name, " +
            "Content.season as season, " +
            "Content.episode as episode, " +
            "Content.free as free, " +
            "Content.is_movie as isMovie, " +
            "Content.broadcast_date as broadcastDate, " +
            "Content.contentAdvisory as contentAdvisory, " +
            "Downloads.download_progress as downloadProgress, " +
            "Downloads.download_status as downloadStatus, " +
            "Downloads.download_url as downloadUrl " +
            "FROM Content LEFT JOIN Downloads " +
            "ON Content.content_id = Downloads.content_id " +
            "WHERE IFNULL(Downloads.download_status, 0) != 3 " +
            "AND Content.is_movie = (:isMovie) " +
            "AND Content.provider_id = (:contentProviderId) " +
            "AND CASE WHEN Content.is_movie = 0 " +
            "THEN Content.season = 'season1' AND Content.episode = 'episode1' " +
            "ELSE Content.season = '' END " +
            "ORDER BY  Content.is_header DESC, Content.broadcast_date " +
            "LIMIT (:limit)")
    fun getAllMovies(limit: Int, isMovie: Int, contentProviderId: String): LiveData<List<ContentDownload>>


    //Get All Movies
    @Transaction
    @Query("SELECT Content.content_id as contentId, " +
            "Content.provider_id as contentProviderId, " +
            "Content.title as title, " +
            "Content.long_description as longDescription, " +
            "Content.short_description as shortDescription, " +
            "Content.additional_desc_1 as additionalDescription1, " +
            "Content.additional_desc_2 as additionalDescription2, " +
            "Content.additional_title_1 as additionalTitle1, " +
            "Content.additional_title_2 as additionalTitle2, " +
            "Content.duration as durationInMts, " +
            "Content.video_file_size as videoTarFileSize," +
            "Content.audio_file_size as audioTarFileSize, " +
            "Content.yor as yearOfRelease, " +
            "Content.ageAppropriateness as ageAppropriateness, " +
            "Content.genre as genre, " +
            "Content.language as language, " +
            "Content.artists as artists, " +
            "Content.attachments as contentAttachments, " +
            "Content.dash_url as dashUrl, " +
            "Content.name as name, " +
            "Content.season as season, " +
            "Content.episode as episode, " +
            "Content.free as free, " +
            "Content.is_movie as isMovie, " +
            "Content.broadcast_date as broadcastDate, " +
            "Content.contentAdvisory as contentAdvisory, " +
            "Downloads.download_progress as downloadProgress, " +
            "Downloads.download_status as downloadStatus, " +
            "Downloads.download_url as downloadUrl " +
            "FROM Content LEFT JOIN Downloads " +
            "ON Content.content_id = Downloads.content_id " +
            "WHERE hubId = (:hubId) " +
            "AND Content.is_movie = (:isMovie) " +
            "AND IFNULL(Downloads.download_status, 0) != 3 " +
            "AND Content.provider_id = (:contentProviderId) " +
            "AND CASE WHEN Content.is_movie = 0 " +
            "THEN Content.season = 'season1' AND Content.episode = 'episode1' " +
            "ELSE Content.season = '' END " +
            "ORDER BY Content.is_header DESC, Content.broadcast_date " +
            "LIMIT (:limit)")
    fun getAllMoviesForHubId(hubId: String, limit: Int, isMovie: Int, contentProviderId: String): LiveData<List<ContentDownload>>

    @Transaction
    @Query("SELECT Content.content_id as contentId, " +
            "Content.provider_id as contentProviderId, " +
            "Content.title as title, " +
            "Content.long_description as longDescription, " +
            "Content.short_description as shortDescription, " +
            "Content.additional_desc_1 as additionalDescription1, " +
            "Content.additional_desc_2 as additionalDescription2, " +
            "Content.additional_title_1 as additionalTitle1, " +
            "Content.additional_title_2 as additionalTitle2, " +
            "Content.duration as durationInMts, " +
            "Content.video_file_size as videoTarFileSize," +
            "Content.audio_file_size as audioTarFileSize, " +
            "Content.yor as yearOfRelease, " +
            "Content.ageAppropriateness as ageAppropriateness, " +
            "Content.genre as genre, " +
            "Content.language as language, " +
            "Content.artists as artists, " +
            "Content.attachments as contentAttachments, " +
            "Content.dash_url as dashUrl, " +
            "Content.name as name, " +
            "Content.season as season, " +
            "Content.episode as episode, " +
            "Content.free as free, " +
            "Content.is_movie as isMovie, " +
            "Content.broadcast_date as broadcastDate, " +
            "Content.contentAdvisory as contentAdvisory, " +
            "Downloads.download_progress as downloadProgress, " +
            "Downloads.download_status as downloadStatus, " +
            "Downloads.download_url as downloadUrl " +
            "FROM Content LEFT JOIN Downloads " +
            "ON Content.content_id = Downloads.content_id " +
            "WHERE hubId = (:hubId) AND free = (:isFree) " +
            "AND Content.is_movie = (:isMovie) " +
            "AND Content.provider_id = (:contentProviderId) " +
            "AND CASE WHEN Content.is_movie = 0 " +
            "THEN Content.season = 'season1' AND Content.episode = 'episode1' " +
            "ELSE Content.season = '' END " +
            "ORDER BY Content.is_header DESC, Content.broadcast_date " +
            "LIMIT (:limit)")
    fun getMoviesForHubId(hubId: String, isFree: Int, limit: Int, isMovie: Int, contentProviderId: String): LiveData<List<ContentDownload>>

    @Transaction
    @Query("SELECT Content.content_id as contentId, " +
            "Content.provider_id as contentProviderId, " +
            "Content.title as title, " +
            "Content.long_description as longDescription, " +
            "Content.short_description as shortDescription, " +
            "Content.additional_desc_1 as additionalDescription1, " +
            "Content.additional_desc_2 as additionalDescription2, " +
            "Content.additional_title_1 as additionalTitle1, " +
            "Content.additional_title_2 as additionalTitle2, " +
            "Content.duration as durationInMts, " +
            "Content.video_file_size as videoTarFileSize," +
            "Content.audio_file_size as audioTarFileSize, " +
            "Content.yor as yearOfRelease, " +
            "Content.ageAppropriateness as ageAppropriateness, " +
            "Content.genre as genre, " +
            "Content.language as language, " +
            "Content.artists as artists, " +
            "Content.attachments as contentAttachments, " +
            "Content.dash_url as dashUrl, " +
            "Content.name as name, " +
            "Content.season as season, " +
            "Content.episode as episode, " +
            "Content.free as free, " +
            "Content.is_movie as isMovie, " +
            "Content.broadcast_date as broadcastDate, " +
            "Content.contentAdvisory as contentAdvisory, " +
            "Downloads.download_progress as downloadProgress, " +
            "Downloads.download_status as downloadStatus, " +
            "Downloads.download_url as downloadUrl " +
            "FROM Content LEFT JOIN Downloads " +
            "ON Content.content_id = Downloads.content_id " +
            "WHERE free = (:isFree) " +
            "AND Content.is_movie = (:isMovie) " +
            "AND Content.provider_id = (:contentProviderId) " +
            "AND CASE WHEN Content.is_movie = 0 " +
            "THEN Content.season = 'season1' AND Content.episode = 'episode1' " +
            "ELSE Content.season = '' END " +
            "ORDER BY Content.is_header DESC, Content.broadcast_date " +
            "LIMIT (:limit)")
    fun getMovies(isFree: Int, limit: Int,isMovie: Int, contentProviderId: String): LiveData<List<ContentDownload>>

    @Transaction
    @Query("SELECT Content.content_id as contentId, " +
            "Content.provider_id as contentProviderId, " +
            "Content.title as title, " +
            "Content.long_description as longDescription, " +
            "Content.short_description as shortDescription, " +
            "Content.additional_desc_1 as additionalDescription1, " +
            "Content.additional_desc_2 as additionalDescription2, " +
            "Content.additional_title_1 as additionalTitle1, " +
            "Content.additional_title_2 as additionalTitle2, " +
            "Content.duration as durationInMts, " +
            "Content.video_file_size as videoTarFileSize," +
            "Content.audio_file_size as audioTarFileSize, " +
            "Content.yor as yearOfRelease, " +
            "Content.ageAppropriateness as ageAppropriateness, " +
            "Content.genre as genre, " +
            "Content.language as language, " +
            "Content.artists as artists, " +
            "Content.attachments as contentAttachments, " +
            "Content.dash_url as dashUrl, " +
            "Content.name as name, " +
            "Content.season as season, " +
            "Content.episode as episode, " +
            "Content.free as free, " +
            "Content.is_movie as isMovie, " +
            "Content.broadcast_date as broadcastDate, " +
            "Downloads.download_progress as downloadProgress, " +
            "Downloads.download_status as downloadStatus, " +
            "Downloads.download_url as downloadUrl " +
            "FROM Content LEFT JOIN Downloads " +
            "ON Content.content_id = Downloads.content_id " +
            "WHERE Content.is_movie = 0 " +
            "AND Content.provider_id = (:contentProviderId) " +
            "AND Content.name = (:seriesName) " +
            "And Content.season = (:seasonName) " +
            "ORDER BY Content.season, Content.episode" )
    fun getEpisodesOfSeason(seriesName: String, seasonName: String, contentProviderId: String): LiveData<List<ContentDownload>>

    @Transaction
    @Query("SELECT Content.content_id as contentId, " +
            "Content.provider_id as contentProviderId, " +
            "Content.title as title, " +
            "Content.long_description as longDescription, " +
            "Content.short_description as shortDescription, " +
            "Content.additional_desc_1 as additionalDescription1, " +
            "Content.additional_desc_2 as additionalDescription2, " +
            "Content.additional_title_1 as additionalTitle1, " +
            "Content.additional_title_2 as additionalTitle2, " +
            "Content.duration as durationInMts, " +
            "Content.video_file_size as videoTarFileSize," +
            "Content.audio_file_size as audioTarFileSize, " +
            "Content.yor as yearOfRelease, " +
            "Content.ageAppropriateness as ageAppropriateness, " +
            "Content.genre as genre, " +
            "Content.language as language, " +
            "Content.artists as artists, " +
            "Content.attachments as contentAttachments, " +
            "Content.dash_url as dashUrl, " +
            "Content.name as name, " +
            "Content.season as season, " +
            "Content.episode as episode, " +
            "Content.free as free, " +
            "Content.is_movie as isMovie, " +
            "Content.broadcast_date as broadcastDate, " +
            "Downloads.download_progress as downloadProgress, " +
            "Downloads.download_status as downloadStatus, " +
            "Downloads.download_url as downloadUrl " +
            "FROM Content LEFT JOIN Downloads " +
            "ON Content.content_id = Downloads.content_id " +
            "WHERE Content.is_movie = 0 " +
            "AND Content.provider_id = (:contentProviderId) " +
            "AND Content.name = (:seriesName) " +
            "ORDER BY Content.season, Content.episode" )
    suspend fun getAllEpisodesOfSeries(seriesName: String, contentProviderId: String): List<ContentDownload>?

    @Transaction
    @Query("SELECT DISTINCT Content.season as season "+
            "FROM Content "+
            "WHERE Content.is_movie = 0 " +
            "AND Content.provider_id = (:contentProviderId) " +
            "AND Content.name = (:seriesName) " +
            "ORDER BY Content.season " )
    fun getAllfSeasonOfSeries(seriesName: String, contentProviderId: String): LiveData<List<String>>
}


class ContentDownload(val contentId: String,
                      val contentProviderId: String,
                      val title: String,
                      val downloadUrl: String?,
                      val downloadStatus: Int,
                      val downloadProgress: Int,
                      val shortDescription: String?,
                      val longDescription: String?,
                      val additionalDescription1: String?,
                      val additionalDescription2: String?,
                      val additionalTitle1: String?,
                      val additionalTitle2: String?,
                      val genre: String,
                      val yearOfRelease: String?,
                      val language: String,
                      val durationInMts: Float,
                      val artists: List<com.msr.bine_sdk.cloud.models.Content.Artist> = ArrayList(),
                      val dashUrl: String,
                      val free: Boolean,
                      val contentAttachments: List<com.msr.bine_sdk.cloud.models.Content.Attachment> = ArrayList(),
                      val name: String?,
                      val season: String?,
                      val episode: String?,
                      val isMovie: Boolean,
                      val ageAppropriateness: String?,
                      val contentAdvisory: String?,
                      internal val videoTarFileSize: Int?,
                      internal val audioTarFileSize: Int?,
                      var playBackPosition: Long?): Serializable {
    fun getThumbnailImage(): String? {
        for (attachment in contentAttachments) {
            if (attachment.type.equals("Thumbnail", true)) {
                return BuildConfig.CDN_BASE_URL + contentProviderId + "-cdn/" + contentId + "/" + attachment.name
            }
        }
        return null
    }

    fun getThumbnailLandscapeImage(): String? {
        for (attachment in contentAttachments) {
            if (attachment.type.equals("LandscapeThumbnail", true)) {
                return BuildConfig.CDN_BASE_URL + contentProviderId + "-cdn/" + contentId + "/" + attachment.name
            }
        }
        return null
    }

    fun getSize(): Int {
        return (videoTarFileSize ?: 0) + (audioTarFileSize ?: 0)
    }

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

    fun getContentTitle(): String {
        return if(isMovie) title else {additionalTitle2 ?: title}
    }
}