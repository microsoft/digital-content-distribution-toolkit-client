package com.microsoft.mobile.polymer.mishtu.storage.entities

import androidx.lifecycle.LiveData
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ContentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg folder: Content)

    @Query("DELETE FROM Content")
    fun deleteAll()

    @Transaction
    @Query("SELECT * FROM Content ORDER BY broadcast_date DESC")
    fun getAllContent(): LiveData<List<Content>>

    @Transaction
    @Query("SELECT * FROM Content WHERE free = 0 ORDER BY broadcast_date DESC")
    fun getPaidContent(): LiveData<List<Content>>

    //DEPRECATED
    @Transaction
    @Query("SELECT * FROM Content WHERE free = 1 ORDER BY created_date DESC")
    fun getFreeContent(): LiveData<List<Content>>

    //Get All Series
    @Transaction
    @Query("SELECT * FROM Content WHERE is_movie = 0 GROUP BY name ORDER BY created_date DESC")
    fun getAllSeries(): LiveData<List<Content>>

    //DEPRECATED
    //Get All Movies
    @Transaction
    @Query("SELECT * FROM Content WHERE is_movie = 1 ORDER BY created_date DESC")
    fun getAllMovies(): LiveData<List<Content>>

    //DEPRECATED
    @Transaction
    @Query("SELECT * FROM Content WHERE content_id = :id")
    suspend fun getContentById(id: String): Content?

    @Transaction
    @Query("SELECT * FROM Content WHERE content_id IN (:ids)")
    suspend fun getContentListByIds(ids: List<String>): List<Content>

    @Transaction
    @Query("SELECT * FROM Content WHERE free = 0 and  provider_id = :id")
    suspend fun getPaidContentListByContentProviderId(id: String): List<Content>?



    @Transaction
    @Query("SELECT * FROM Content WHERE name = :name ORDER BY broadcast_date DESC")
    suspend fun getSeriesContent(name: String): List<Content>

    @Query("SELECT * FROM Content WHERE UPPER(language) = UPPER(:language) ORDER BY broadcast_date DESC")
    fun getContentForLanguage(language: String): List<Content>

    @Query("SELECT * FROM Content WHERE UPPER(genre) LIKE UPPER(:genre) ORDER BY broadcast_date DESC")
    fun getContentForGenre(genre: String): List<Content>

    @Query("SELECT * FROM Content WHERE " +
            "UPPER(title) LIKE :query " +
            "or UPPER(genre) LIKE :query " +
            "or UPPER(artists) LIKE :query " +
            "or yor LIKE :query " +
            "or UPPER(language) LIKE :query " +
            "ORDER BY broadcast_date DESC")
    fun getContentForQuery(query: String): List<Content>


    @Query("UPDATE Content SET hubId = (:hubId) WHERE content_id IN (:contentIds)")
    fun setHubIdForContents(hubId: String, contentIds: Array<String>)

    //DEPRECATED
    //Get All Movies
    @Transaction
    @Query("SELECT * FROM Content WHERE hubId = (:hubId) ORDER BY created_date DESC")
    fun getAllMoviesForHubId(hubId: String): LiveData<List<Content>>

    //DEPRECATED
    @Transaction
    @Query("SELECT * FROM Content WHERE hubId = (:hubId) AND free = 1 ORDER BY created_date DESC")
    fun getFreeMoviesForHubId(hubId: String): LiveData<List<Content>>

    //DEPRECATED
    @Transaction
    @Query("SELECT * FROM Content WHERE hubId = (:hubId) AND free = 0 ORDER BY created_date DESC")
    fun getPaidMoviesForHubId(hubId: String): LiveData<List<Content>>

    //DEPRECATED
    @Transaction
    @Query("SELECT * FROM Content WHERE content_id = (:contentId)")
    fun getContentLiveData(contentId: String): Flow<Content?>

    @Transaction
    @Query("SELECT * FROM Content WHERE content_id = (:contentId)")
    fun getContent(contentId: String): Content?
}