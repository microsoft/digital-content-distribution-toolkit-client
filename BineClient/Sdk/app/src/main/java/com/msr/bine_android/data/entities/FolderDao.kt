package com.msr.bine_android.data.entities

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.room.*

@Dao
interface FolderDao {
    @Query("SELECT * FROM folder ORDER BY download_count DESC")
    fun getAll(): LiveData<List<Folder>>

    @Query("SELECT * FROM folder ORDER BY download_count DESC")
    fun getAllNew(): List<Folder>

    @Query("SELECT * FROM folder ORDER BY download_count DESC")
    fun getFoldersALL(): DataSource.Factory<Int, Folder>

    @Query("SELECT * FROM folder where hub_id = :hubId ORDER BY download_count DESC")
    fun getFoldersALL(hubId: String): DataSource.Factory<Int, Folder>

//    @Query("SELECT * FROM folder WHERE UPPER(language) = :language ORDER BY download_count DESC")
//    fun getFoldersLanguageBased(language: String): DataSource.Factory<Int?, Folder?>
//
//    @Query("SELECT * FROM folder WHERE hub_id=:hubId AND (UPPER(language) = :language) ORDER BY download_count DESC")
//    fun getFoldersLanguageBased(language: String, hubId: String): DataSource.Factory<Int?, Folder?>
//
//    @Query("SELECT * FROM folder WHERE UPPER(language)=:language AND (castInfo LIKE :query or yearOfRelease LIKE:query or title LIKE:query) ORDER BY download_count DESC")
//    fun getFoldersQueryLanguageBased(query: String, language: String): DataSource.Factory<Int?, Folder?>
//
//    @Query("SELECT * FROM folder WHERE hub_id=:hubId AND UPPER(language)=:language AND (castInfo LIKE :query or yearOfRelease LIKE:query or title LIKE:query) ORDER BY download_count DESC")
//    fun getFoldersQueryLanguageBased(query: String, language: String,hubId: String): DataSource.Factory<Int?, Folder?>
//
//    @Query("SELECT * FROM folder WHERE castInfo LIKE :query or yearOfRelease LIKE:query or title LIKE:query or language LIKE:query ORDER BY download_count DESC")
//    fun getAllFoldersQueryBased(query: String): DataSource.Factory<Int?, Folder?>
//
//    @Query("SELECT * FROM folder WHERE hub_id=:hubId AND (castInfo LIKE :query or yearOfRelease LIKE:query or title LIKE:query or language LIKE:query) ORDER BY download_count DESC")
//    fun getAllFoldersQueryBased(query: String,hubId: String): DataSource.Factory<Int?, Folder?>

    @Query("SELECT * FROM folder WHERE parent = :parent")
    fun getFoldersForParent(parent: String): List<Folder>

    @Query("SELECT * FROM folder where ID = :id")
    fun getFolder(id: String): Folder?

    @Query("SELECT * FROM folder WHERE download_status = 3")
    fun getDownloaded(): List<Folder>

    @Query("SELECT * FROM folder WHERE download_status = 1 OR download_status = 2 OR download_status = 3  ORDER BY download_status")
    fun getDownloads(): LiveData<List<Folder>>

    @Query("SELECT * FROM folder WHERE hub_id = :hubId ORDER BY download_count DESC")
    fun getFoldersOnHub(hubId: String): LiveData<List<Folder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg folder: Folder)

    @Query("UPDATE folder SET download_status = :status, download_progress = :progress WHERE ID = :folderId")
    fun updateDownload(folderId: String, status: Int, progress: Int)

    @Query("UPDATE folder SET download_status = :status, download_progress = :progress, download_type = :type WHERE ID = :folderId")
    fun updateDownload(folderId: String, status: Int, type: Int, progress: Int)

    @Query("UPDATE folder SET hub_id = \"\"")
    fun updateHubIdForAllContents()

    @Query("UPDATE folder SET hub_id = :id")
    fun updateHubIdForAllContents(id: String)

//    @Query("UPDATE folder SET folderUrl = :folderUrl WHERE ID = :id")
//    fun updateFolderUrlForFolder(id: String, folderUrl: String)

    @Delete
    fun delete(folder: Folder)

    @Query("DELETE FROM folder")
    fun deleteAll()

    @Query("DELETE FROM folder WHERE download_status != 3")
    fun deleteNotDownloaded()
}