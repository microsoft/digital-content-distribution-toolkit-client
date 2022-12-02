// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.msr.bine_android.data

import android.text.TextUtils
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import com.google.gson.Gson
import com.msr.bine_android.data.entities.*
import com.msr.bine_android.data.entities.Folder
import com.msr.bine_android.utils.Constants
import com.msr.bine_sdk.download.Downloader
import com.msr.bine_sdk.hub.model.ConnectedHub
import com.msr.bine_sdk.hub.model.DownloadStatus
import java.util.*
import javax.inject.Inject

open class DataRepository @Inject constructor(private val folderDao: FolderDao,
                                              private val contentEntityDao: ContentEntityDao,
                                              private val cartEntityDao: CartEntityDao,
                                              private val folderEntityDao: FolderEntityDao,
                                              private val sharedPreferenceStore: SharedPreferenceStore) {

    companion object {
        private var instance: DataRepository? = null

        @Synchronized
        fun getInstance(folderDao: FolderDao,
                        contentEntityDao: ContentEntityDao,
                        cartEntityDao: CartEntityDao,
                        folderEntityDao: FolderEntityDao,
                        sharedPreferenceStore: SharedPreferenceStore): DataRepository = instance
                ?: DataRepository(folderDao, contentEntityDao, cartEntityDao, folderEntityDao, sharedPreferenceStore).also {
                    instance = it
                }
    }

    fun saveHub(hub: ConnectedHub?) {
        var json = "";
        if (hub != null) {
            json = Gson().toJson(hub)
        }
        sharedPreferenceStore.save(SharedPreferenceStore.KEY_SHAREDPREF_HUB, json);
    }

    fun getHub(): ConnectedHub? {
        val hubString = sharedPreferenceStore.get(SharedPreferenceStore.KEY_SHAREDPREF_HUB)
        if (!hubString.isNullOrEmpty()) {
            return Gson().fromJson(hubString, ConnectedHub::class.java)
        }
        return null
    }

    fun saveHubId(hubId: String) {
        sharedPreferenceStore.save(SharedPreferenceStore.KEY_SHAREDPREF_HUB_ID, hubId);
    }


    fun getHubId(): String {
        val hubIdString = sharedPreferenceStore.get(SharedPreferenceStore.KEY_SHAREDPREF_HUB_ID)
        if (!hubIdString.isNullOrEmpty()) {
            return hubIdString
        }
        return ""
    }

    fun saveUserId(token: String) {
        sharedPreferenceStore.save(SharedPreferenceStore.KEY_SHAREDPREF_USER_ID, token)
    }

    fun getUserId(): String? {
        return sharedPreferenceStore.get(SharedPreferenceStore.KEY_SHAREDPREF_USER_ID)
    }

    fun saveCoupon(token: String) {
        sharedPreferenceStore.save(SharedPreferenceStore.KEY_SHAREDPREF_COUPON, token)
    }

    fun getCoupon(): String? {
        return sharedPreferenceStore.get(SharedPreferenceStore.KEY_SHAREDPREF_COUPON)
    }

    fun saveContentDownloadStatus(status: String) {
        sharedPreferenceStore.save(SharedPreferenceStore.KEY_SHAREDPREF_IS_CONTENT_FETCHED, status)
    }

    fun getContentDownloadStatus(): String? {
        return sharedPreferenceStore.get(SharedPreferenceStore.KEY_SHAREDPREF_IS_CONTENT_FETCHED)
    }

    fun firstInstall(boolean: Boolean) {
        sharedPreferenceStore.save(SharedPreferenceStore.KEY_SHAREDPREF_FIRST_INSTALL, if (boolean) "1" else "0")
    }

    fun isFirstInstall(): Boolean {
        val bool = sharedPreferenceStore.get(SharedPreferenceStore.KEY_SHAREDPREF_FIRST_INSTALL)
        if (bool == null || bool == "") {
            return true
        }
        return bool.equals("1")
    }

    fun tncAccepted(boolean: Boolean) {
        sharedPreferenceStore.save(SharedPreferenceStore.KEY_SHAREDPREF_TNC_ACCEPTED, if (boolean) "1" else "0")
    }

    fun isTncAccepted(): Boolean {
        val bool = sharedPreferenceStore.get(SharedPreferenceStore.KEY_SHAREDPREF_TNC_ACCEPTED)
        if (bool == null || bool == "") {
            return false
        }
        return bool.equals("1")
    }

    fun firstDownload(boolean: Boolean) {
        sharedPreferenceStore.save(SharedPreferenceStore.KEY_SHAREDPREF_FIRST_DOWNLOAD, if (boolean) "1" else "0")
    }

    fun isFirstDownload(): Boolean {
        val bool = sharedPreferenceStore.get(SharedPreferenceStore.KEY_SHAREDPREF_FIRST_DOWNLOAD)
        if (bool == null || bool == "") {
            return true
        }
        return bool.equals("1")
    }

    fun firstLogin(boolean: Boolean) {
        sharedPreferenceStore.save(SharedPreferenceStore.KEY_SHAREDPREF_FIRST_LOGIN, if (boolean) "1" else "0")
    }

    fun isFirstLogin(): Boolean {
        val bool = sharedPreferenceStore.get(SharedPreferenceStore.KEY_SHAREDPREF_FIRST_LOGIN)
        if (bool == null || bool == "") {
            return true
        }
        return bool.equals("1")
    }

    /*fun setHubIP(ip: String) {
        sharedPreferenceStore.save(BineSharedPreference.KEY_HUB_IP, ip)
    }

    fun getHubIP(): String {
        return sharedPreferenceStore.getDefaultIfNull(BineSharedPreference.KEY_HUB_IP)
    }*/

    fun setHubSSID(ip: String) {
        sharedPreferenceStore.save(SharedPreferenceStore.KEY_SHAREDPREF_HUB_SSID, ip)
    }

    fun getHubSSID(): String? {
        return sharedPreferenceStore.get(SharedPreferenceStore.KEY_SHAREDPREF_HUB_SSID)
    }

    fun setMediaHouse(ip: String) {
        sharedPreferenceStore.save(SharedPreferenceStore.KEY_SHAREDPREF_MEDIA_HOUSE, ip)
    }

    fun getMediaHouse(): String {
        val ip = sharedPreferenceStore.get(SharedPreferenceStore.KEY_SHAREDPREF_MEDIA_HOUSE)
        if (ip == null || ip.isEmpty()) return Constants.DEFAULT_MEDIAHOUSE
        return ip
    }

    fun setDownloadType(type: String) {
        sharedPreferenceStore.save(SharedPreferenceStore.KEY_SHAREDPREF_DOWNLOAD_TYPE, type)
    }

    fun getDownloadType(): Downloader.TYPE {
        val type = sharedPreferenceStore.get(SharedPreferenceStore.KEY_SHAREDPREF_DOWNLOAD_TYPE)
        if (type == null || type.isEmpty()) return Downloader.TYPE.EXO
        return if (type == Downloader.TYPE.CUSTOM.name) Downloader.TYPE.CUSTOM else Downloader.TYPE.EXO
    }

  /*  fun getDBBineMetaData(bineMetadataJson: BineMetadata): com.msr.bine_android.data.entities.BineMetadata {
        val json = Gson().toJson(bineMetadataJson)
        return Gson().fromJson(json, com.msr.bine_android.data.entities.BineMetadata::class.java)
    }*/

   /* fun saveHubFolders(folders: List<com.msr.bine_sdk.models.old.Folder>, hubId: String): List<Folder> {
        val dbFolders = mutableListOf<Folder>()
        for (folder: com.msr.bine_sdk.models.old.Folder in folders) {
            val dbFolder: Folder
            val existingFolder = folderDao.getFolder(folder.ID);
            if (existingFolder == null) {
                dbFolder = Folder(folder.ID,
                        "root",
                        false,
                        Gson().toJson(folder.metadata),
                        DownloadStatus.NOT_DOWNLOADED.value,
                        Source.HUB.value,
                        Downloader.TYPE.EXO.ordinal,
                        hubId,
                        0,
                        0, getDBBineMetaData(folder.metadata))
            } else {
                dbFolder = Folder(folder.ID,
                        "root",
                        false,
                        Gson().toJson(folder.metadata),
                        existingFolder.downloadStatus,
                        existingFolder.source,
                        existingFolder.type,
                        hubId,
                        existingFolder.progress,
                        existingFolder.downloadCount, getDBBineMetaData(folder.metadata))
            }
            AppDatabase.databaseWriteExecutor.execute { folderDao.insert(dbFolder) }
            dbFolders.add(dbFolder)
        }
        return dbFolders;
    }*/

    /*fun saveFolders(folders: List<com.msr.bine_sdk.models.old.Folder>): List<Folder> {
        val dbFolders = mutableListOf<Folder>()
        for (folder: com.msr.bine_sdk.models.old.Folder in folders) {
            val dbFolder: Folder
            if (folder.source == Source.HUB) {
                dbFolder = Folder(folder.ID,
                        folder.parent,
                        folder.hasChildren,
                        folder.bineMetadataJson,
                        0,//TODO
                        Source.HUB.value,
                        Downloader.TYPE.EXO.ordinal,
                        null,
                        0,
                        folder.downloadCount,
                        getDBBineMetaData(folder.metadata))


                val metadataJsonFile = Gson().fromJson(folder.bineMetadataJson, BineMetadata::class.java)

                *//*for (metadata: MetadataFile in folder.metadataFiles) {
                    //Thumbnail
                    if (metadata.name.equals(metadataJsonFile.thumbnail_2x)) {
                        metadataJsonFile.thumbnail_2x = metadata.filePath
                    }
                    //Tumbnail2x
                    if (metadata.name.equals(metadataJsonFile.thumbnail)) {
                        metadataJsonFile.thumbnail = metadata.filePath
                    }
                }*//*
                dbFolder.metadataJson = Gson().toJson(metadataJsonFile)
            } else {
                val existingFolder = folderDao.getFolder(folder.ID);
                if (existingFolder == null) {
                    dbFolder = Folder(folder.ID,
                            "root",
                            false,
                            Gson().toJson(folder.metadata),
                            0,//TODO:
                            Source.CLOUD.value,
                            Downloader.TYPE.EXO.ordinal,
                            null,
                            0,
                            folder.downloadCount,
                            getDBBineMetaData(folder.metadata))
                } else {
                    if (existingFolder.downloadStatus == DownloadStatus.DOWNLOADED.value) {
                        continue
                    }
                    dbFolder = Folder(folder.ID,
                            "root",
                            false,
                            existingFolder.metadataJson,
                            existingFolder.downloadStatus,
                            existingFolder.source,
                            existingFolder.type,
                            existingFolder.hubId,
                            existingFolder.progress,
                            folder.downloadCount,
                            getDBBineMetaData(folder.metadata))
                }
            }
            AppDatabase.databaseWriteExecutor.execute { folderDao.insert(dbFolder) }
            dbFolders.add(dbFolder)
        }
        return dbFolders
    }*/

    fun getAllFolders(): LiveData<List<Folder>> {
        return folderDao.getAll()
    }

    fun getAllFoldersNew(): List<Folder> {
        return folderDao.getAllNew()
    }

    fun getFoldersOnHub(hubId: String): LiveData<List<Folder>> {
        return folderDao.getFoldersOnHub(hubId)
    }


    /**
     * Fetches foldes 1) Downloading 2) Queued 3) Downloaded
     */
    fun getDownloadsList(): LiveData<List<Folder>> {
        return folderDao.getDownloads()
    }

    /**
     * Fetches folders already Downloaded
     */
    fun getDownloaded(): List<Folder> {
        return folderDao.getDownloaded()
    }

    fun getFolder(folderId: String): Folder? {
        return folderDao.getFolder(folderId)
    }

    fun getFoldersForParent(parent: String): List<Folder> {
        return folderDao.getFoldersForParent(parent)
    }

    fun updateDownload(folderId: String, status: DownloadStatus, progress: Int, type: Downloader.TYPE) {
        AppDatabase.databaseWriteExecutor.execute { folderDao.updateDownload(folderId, status.value, type.ordinal, progress) }
    }

    fun updateDownload(folderId: String, status: DownloadStatus, progress: Int) {
        AppDatabase.databaseWriteExecutor.execute { folderDao.updateDownload(folderId, status.value, progress) }
    }

    /*fun updateFolderUrl(folderId: String, folderUrl: String) {
        AppDatabase.databaseWriteExecutor.execute { folderDao.updateFolderUrlForFolder(folderId, folderUrl) }
    }*/

    fun resetHubForAllContents() {
        AppDatabase.databaseWriteExecutor.execute { folderDao.updateHubIdForAllContents() }
    }

    fun updateHubIdForAllContents(id: String) {
        AppDatabase.databaseWriteExecutor.execute { folderDao.updateHubIdForAllContents(id) }
    }

    fun deleteAllFolders() {
        AppDatabase.databaseWriteExecutor.execute { folderDao.deleteAll() }
    }

    fun deleteNotDownloaded() {
        folderDao.deleteNotDownloaded()
    }

    fun saveLanguageLocale(language: String) {
        sharedPreferenceStore.save(SharedPreferenceStore.KEY_SHAREDPREF_LANGUAGE_LOCALE, language)
    }

    fun getLanguageLocale(): String {
        val language = sharedPreferenceStore.get(SharedPreferenceStore.KEY_SHAREDPREF_LANGUAGE_LOCALE)
        if (language.isNullOrEmpty()) {
            saveLanguageLocale("en")
            return "en"
        }
        return language
    }

    fun saveReferralCode(code: String) {
        sharedPreferenceStore.save(SharedPreferenceStore.KEY_REFERRAL_CODE, code)
    }

    fun getReferralCode(): String {
        val code = sharedPreferenceStore.get(SharedPreferenceStore.KEY_REFERRAL_CODE)
        if (code.isNullOrEmpty()) {
            return ""
        }
        return code
    }

    fun saveTelemetryId(key: String, value: String) {
        sharedPreferenceStore.save(key, value)
    }

    fun getTelemetryId(key: String): String? {
        return sharedPreferenceStore.get(key)
    }

    fun getScreenRecordingState(key: String): String? {
        return sharedPreferenceStore.get(key)
    }

    fun changeScreenRecordingSettings(key: String, b: String) {
        sharedPreferenceStore.save(key, b)
    }

    fun saveLastDeletedDate(date: Date) {
        sharedPreferenceStore.save(SharedPreferenceStore.KEY_LAST_DELETED_DATE, date.time.toString());
    }

    fun getLastDeletedDate(): Date {
        val timeString = sharedPreferenceStore.get(SharedPreferenceStore.KEY_LAST_DELETED_DATE)
        if (!timeString.isNullOrEmpty()) {
            return Date(timeString.toLong())
        } else {
            val date = Date()
            saveLastDeletedDate(date)
            return date
        }
    }

    fun getFoldersALL(hub: ConnectedHub?): DataSource.Factory<Int, Folder> {
        if (hub != null && !TextUtils.isEmpty(hub.id)) {
            return folderDao.getFoldersALL(hub.id)
        }
        return folderDao.getFoldersALL()
    }

   /* fun getFoldersLanguageBased(hub: ConnectedHub?, language: String): DataSource.Factory<Int?, Folder?> {
        if (hub != null && !TextUtils.isEmpty(hub.id)) {
            return folderDao.getFoldersLanguageBased(language, hub.id)
        }
        return folderDao.getFoldersLanguageBased(language)
    }

    fun getFoldersQueryLanguageBased(hub: ConnectedHub?, query: String, language: String): DataSource.Factory<Int?, Folder?> {
        if (hub != null && !TextUtils.isEmpty(hub.id)) {
            return folderDao.getFoldersQueryLanguageBased(query, language, hub.id)
        }
        return folderDao.getFoldersQueryLanguageBased(query, language)
    }

    fun getAllFoldersQueryBased(hub: ConnectedHub?, query: String): DataSource.Factory<Int?, Folder?> {
        if (hub != null && !TextUtils.isEmpty(hub.id)) {
            return folderDao.getAllFoldersQueryBased(query)
        }
        return folderDao.getAllFoldersQueryBased(query)
    }*/

   /* fun addDummyContentFolder() {
        GlobalScope.async {
            val contents = contentEntityDao.getContentWithFolders()
            if (contents.isNotEmpty()) {
                return@async
            }
            val bineMetadata = BineMetadata("Dunkrik",
                    "A romantic comedy full of love, laughter and heartbreak, Cocktail is a story of three friends - Meera, Veronica and Gautam - whose lives turn upside down when their friendship evolves into love, which adds more complications than they can handle. Set against the vibrant and luscious backdrop of London and Capetown, this is a comical, new-age and warm celebration of relationships exploring various choices that life may offer and the extraordinary choices that ordinary people make.",
                    "Comedy",
                    ContentType.SERIES.value,
                    "Hindi",
                    arrayListOf("Rajinikanth-Deepika", "Rajnikant-Deepika", "Rajnikanth-Deepika"),
                    "2014",
                    arrayListOf("https://images004-a.erosnow.com/movie/0/1055530/img625352/6742776/1055530_6742776_19.jpg", "https://images001-a.erosnow.com/movie/0/1055530/img14582/6742777/1055530_6742777_77.jpg"),
                    arrayListOf(),
                    arrayListOf(),
                    "https://arunwithaview.files.wordpress.com/2017/07/dunkirk-movie-poster.jpg?w=768",
                    "",
                    934841918.9453125F,
                    "",
                    "",
                    "")

            val trailer = Trailer(25000F, "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4", 340)
            val ott = OTT("ALT Balaji", "https://www.logodesign.net/images/nature-logo.png", "")
            val tvodPayDetails = TVODPayDetails(99.0, Status.NotPurchased.mValue)
            //UUID.randomUUID().toString()
            val recommendation = "[{\n" +
                    "\"id\":\"Welcome_Back\",\n" +
                    "\"metadata\":{\n" +
                    "\"title\":\"Welcome Back\",\n" +
                    "\"description\":\"Uday Shetty (Nana Patekar) and Majnu Bhai (Anil Kapoor), the protagonists from the film 'Welcome', have left the underworld and are now big businessmen. Two women, Chandni (Ankita Srivastava) and Maharani (Dimple Kapadia), posing as princess and Queen of Nazafgardh, enter their lives; Chandni (Ankita Srivastava) is the new lady love in Uday Shetty (Nana Patekar) and Majnu’s (Anil Kapoor) life and both dream of tying the knot with her at the earliest. However, Appa, Uday’s father, plays spoilsport by bringing another daughter named Ranjana (Shruti Haasan) from his third marriage into the picture. He tells Uday Shetty (Nana Patekar) to get her married to someone from a good family. Maharani (Dimple Kapadia) puts in a condition that only after their sister is married to Chandni (Ankita Srivastava) will she marry one of them. The search to find a suitable boy for their sister Ranjana (Shruti Haasan) hence begins!\",\n" +
                    "\"duration\":8939,\n" +
                    "\"keywords\":[\n" +
                    "\"Item Song\",\n" +
                    "\"debutant\",\n" +
                    "\"Comedy\",\n" +
                    "\"Multi-Starrer\",\n" +
                    "\"Dual Actors\",\n" +
                    "\"2015 Release\",\n" +
                    "\"Welcome Back\",\n" +
                    "\"Welcome Back Movie\",\n" +
                    "\"Welcome Back Full Movie\"\n" +
                    "],\n" +
                    "\"castInfo\":[\n" +
                    "\"Anil Kapoor\",\n" +
                    "\"John Abraham\",\n" +
                    "\"Nana Patekar\",\n" +
                    "\"Paresh Rawal\",\n" +
                    "\"Naseeruddin Shah\",\n" +
                    "\"Shruti Haasan\",\n" +
                    "\"Dimple Kapadia\",\n" +
                    "\"Ankita Srivastava\"\n" +
                    "],\n" +
                    "\"language\":\"Hindi\",\n" +
                    "\"yearOfRelease\":2015,\n" +
                    "\"thumbnail\":\"https://images001-a.erosnow.com/movie/0/1023260/img14582/6608237/1023260_6608237_93.jpg\"\n" +
                    "},\n" +
                    "\"download_count\":0\n" +
                    "}]"
            val content = ContentEntity("1000096_bezubaan",
                    1, PaymentType.TVOD.mValue, bineMetadata, trailer, ott, tvodPayDetails, null, recommendation)

            contentEntityDao.insert(content)


            val folder = FolderEntity(UUID.randomUUID().toString(), content.id, bineMetadata.title,
                    0,
                    bineMetadata.description,
                    "/bulkfiles/Aye_Basant_Bahar_SD_h264_high_480p_1000-video.mp4",
                    "Aye_Basant_Bahar_SD_h264_high_480p_1000-audio.mp4",
                    "https://images004-a.erosnow.com/movie/0/1055530/img625352/6742776/1055530_6742776_19.jpg",
                    934841918.9453125F,
                    6360,
                    "Aye_Basant_Bahar_SD/",
                    "Aye_Basant_Bahar_SD_h264_high_480p_1000.mpd",
                    "http://{HUB_IP}:5000/static/usr/bine/zzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzz/a4a70950-f963-48f9-bbbc-0c676b800cf6/bulkfiles"
            )
            folderEntityDao.insert(folder)
            addFreeContent();
            addMovieContent()
            addSubscrition()
        }
    }

    private fun addFreeContent() {
        GlobalScope.async {
            val bineMetadata = BineMetadata("Dunkrik",
                    "A romantic comedy full of love, laughter and heartbreak, Cocktail is a story of three friends - Meera, Veronica and Gautam - whose lives turn upside down when their friendship evolves into love, which adds more complications than they can handle. Set against the vibrant and luscious backdrop of London and Capetown, this is a comical, new-age and warm celebration of relationships exploring various choices that life may offer and the extraordinary choices that ordinary people make.",
                    "Comedy",
                    ContentType.MOVIE.value,
                    "Hindi",
                    arrayListOf("Rajinikanth-Deepika", "Rajnikant-Deepika", "Rajnikanth-Deepika"),
                    "2014",
                    arrayListOf("https://images004-a.erosnow.com/movie/0/1055530/img625352/6742776/1055530_6742776_19.jpg", "https://images001-a.erosnow.com/movie/0/1055530/img14582/6742777/1055530_6742777_77.jpg"),
                    arrayListOf(),
                    arrayListOf(),
                    "https://arunwithaview.files.wordpress.com/2017/07/dunkirk-movie-poster.jpg?w=768",
                    "",
                    934841918.9453125F,
                    "",
                    "",
                    "")

            val trailer = Trailer(25000F, "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4", 340)
            val ott = OTT("ALT Balaji", "https://www.logodesign.net/images/nature-logo.png", "")
            val tvodPayDetails = TVODPayDetails(99.0, Status.NotPurchased.mValue)
            //UUID.randomUUID().toString()
            val content = ContentEntity("4_2_ka_1",
                    1, PaymentType.FREE.mValue, bineMetadata, trailer, ott, tvodPayDetails, null, "")

            contentEntityDao.insert(content)


            val folder = FolderEntity(UUID.randomUUID().toString(), content.id, bineMetadata.title,
                    0,
                    bineMetadata.description,
                    "/bulkfiles/Aye_Basant_Bahar_SD_h264_high_480p_1000-video.mp4",
                    "Aye_Basant_Bahar_SD_h264_high_480p_1000-audio.mp4",
                    "https://images004-a.erosnow.com/movie/0/1055530/img625352/6742776/1055530_6742776_19.jpg",
                    934841918.9453125F,
                    6360,
                    "Aye_Basant_Bahar_SD/",
                    "Aye_Basant_Bahar_SD_h264_high_480p_1000.mpd",
                    "http://{HUB_IP}:5000/static/usr/bine/zzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzz/a4a70950-f963-48f9-bbbc-0c676b800cf6/bulkfiles"
            )
            folderEntityDao.insert(folder)
        }
    }

    private fun addMovieContent() {
        GlobalScope.async {
            val bineMetadata = BineMetadata("Dunkrik",
                    "A romantic comedy full of love, laughter and heartbreak, Cocktail is a story of three friends - Meera, Veronica and Gautam - whose lives turn upside down when their friendship evolves into love, which adds more complications than they can handle. Set against the vibrant and luscious backdrop of London and Capetown, this is a comical, new-age and warm celebration of relationships exploring various choices that life may offer and the extraordinary choices that ordinary people make.",
                    "Comedy",
                    ContentType.MOVIE.value,
                    "Hindi",
                    arrayListOf("Rajinikanth-Deepika", "Rajnikant-Deepika", "Rajnikanth-Deepika"),
                    "2014",
                    arrayListOf("https://images004-a.erosnow.com/movie/0/1055530/img625352/6742776/1055530_6742776_19.jpg", "https://images001-a.erosnow.com/movie/0/1055530/img14582/6742777/1055530_6742777_77.jpg"),
                    arrayListOf(),
                    arrayListOf(),
                    "https://arunwithaview.files.wordpress.com/2017/07/dunkirk-movie-poster.jpg?w=768",
                    "",
                    934841918.9453125F,
                    "",
                    "",
                    "")

            val trailer = Trailer(25000F, "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4", 340)
            val ott = OTT("ALT Balaji", "https://www.logodesign.net/images/nature-logo.png", "")
            val tvodPayDetails = TVODPayDetails(99.0, Status.NotPurchased.mValue)

            val recommendation = "[{\n" +
                    "\"id\":\"Welcome_Back\",\n" +
                    "\"metadata\":{\n" +
                    "\"title\":\"Welcome Back\",\n" +
                    "\"description\":\"Uday Shetty (Nana Patekar) and Majnu Bhai (Anil Kapoor), the protagonists from the film 'Welcome', have left the underworld and are now big businessmen. Two women, Chandni (Ankita Srivastava) and Maharani (Dimple Kapadia), posing as princess and Queen of Nazafgardh, enter their lives; Chandni (Ankita Srivastava) is the new lady love in Uday Shetty (Nana Patekar) and Majnu’s (Anil Kapoor) life and both dream of tying the knot with her at the earliest. However, Appa, Uday’s father, plays spoilsport by bringing another daughter named Ranjana (Shruti Haasan) from his third marriage into the picture. He tells Uday Shetty (Nana Patekar) to get her married to someone from a good family. Maharani (Dimple Kapadia) puts in a condition that only after their sister is married to Chandni (Ankita Srivastava) will she marry one of them. The search to find a suitable boy for their sister Ranjana (Shruti Haasan) hence begins!\",\n" +
                    "\"duration\":8939,\n" +
                    "\"keywords\":[\n" +
                    "\"Item Song\",\n" +
                    "\"debutant\",\n" +
                    "\"Comedy\",\n" +
                    "\"Multi-Starrer\",\n" +
                    "\"Dual Actors\",\n" +
                    "\"2015 Release\",\n" +
                    "\"Welcome Back\",\n" +
                    "\"Welcome Back Movie\",\n" +
                    "\"Welcome Back Full Movie\"\n" +
                    "],\n" +
                    "\"castInfo\":[\n" +
                    "\"Anil Kapoor\",\n" +
                    "\"John Abraham\",\n" +
                    "\"Nana Patekar\",\n" +
                    "\"Paresh Rawal\",\n" +
                    "\"Naseeruddin Shah\",\n" +
                    "\"Shruti Haasan\",\n" +
                    "\"Dimple Kapadia\",\n" +
                    "\"Ankita Srivastava\"\n" +
                    "],\n" +
                    "\"language\":\"Hindi\",\n" +
                    "\"yearOfRelease\":2015,\n" +
                    "\"thumbnail\":\"https://images001-a.erosnow.com/movie/0/1023260/img14582/6608237/1023260_6608237_93.jpg\"\n" +
                    "},\n" +
                    "\"download_count\":0\n" +
                    "}]"

            //UUID.randomUUID().toString()
            val content = ContentEntity("Aye_Basant_Bahar_SD",
                    1, PaymentType.TVOD.mValue, bineMetadata, trailer, ott, tvodPayDetails, null, recommendation)

            contentEntityDao.insert(content)

            val folder = FolderEntity(UUID.randomUUID().toString(), content.id, bineMetadata.title,
                    0,
                    bineMetadata.description,
                    "/bulkfiles/Aye_Basant_Bahar_SD_h264_high_480p_1000-video.mp4",
                    "Aye_Basant_Bahar_SD_h264_high_480p_1000-audio.mp4",
                    "https://images004-a.erosnow.com/movie/0/1055530/img625352/6742776/1055530_6742776_19.jpg",
                    934841918.9453125F,
                    6360,
                    "Aye_Basant_Bahar_SD/",
                    "Aye_Basant_Bahar_SD_h264_high_480p_1000.mpd",
                    "http://{HUB_IP}:5000/static/usr/bine/zzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzz/a4a70950-f963-48f9-bbbc-0c676b800cf6/bulkfiles"
            )
            folderEntityDao.insert(folder)


        }
    }

    private fun addSubscrition() {
        GlobalScope.async {
            val bineMetadata = BineMetadata("Dunkrik",
                    "A romantic comedy full of love, laughter and heartbreak, Cocktail is a story of three friends - Meera, Veronica and Gautam - whose lives turn upside down when their friendship evolves into love, which adds more complications than they can handle. Set against the vibrant and luscious backdrop of London and Capetown, this is a comical, new-age and warm celebration of relationships exploring various choices that life may offer and the extraordinary choices that ordinary people make.",
                    "Comedy",
                    ContentType.SERIES.value,
                    "Hindi",
                    arrayListOf("Rajinikanth-Deepika", "Rajnikant-Deepika", "Rajnikanth-Deepika"),
                    "2014",
                    arrayListOf("https://images004-a.erosnow.com/movie/0/1055530/img625352/6742776/1055530_6742776_19.jpg", "https://images001-a.erosnow.com/movie/0/1055530/img14582/6742777/1055530_6742777_77.jpg"),
                    arrayListOf(),
                    arrayListOf(),
                    "https://arunwithaview.files.wordpress.com/2017/07/dunkirk-movie-poster.jpg?w=768",
                    "",
                    934841918.9453125F,
                    "",
                    "",
                    "")

            val trailer = Trailer(25000F, "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4", 340)
            val ott = OTT("ALT Balaji", "https://www.logodesign.net/images/nature-logo.png", "")
            val svodPayDetails = SVODPayDetails(Calendar.getInstance().time,
                    Calendar.getInstance().time,
                    SubscriptionType.WEEKLY.mValue, 99.0, Status.NotPurchased.mValue)
            //UUID.randomUUID().toString()

            val recommendation = "[{\n" +
                    "\"id\":\"Welcome_Back\",\n" +
                    "\"metadata\":{\n" +
                    "\"title\":\"Welcome Back\",\n" +
                    "\"description\":\"Uday Shetty (Nana Patekar) and Majnu Bhai (Anil Kapoor), the protagonists from the film 'Welcome', have left the underworld and are now big businessmen. Two women, Chandni (Ankita Srivastava) and Maharani (Dimple Kapadia), posing as princess and Queen of Nazafgardh, enter their lives; Chandni (Ankita Srivastava) is the new lady love in Uday Shetty (Nana Patekar) and Majnu’s (Anil Kapoor) life and both dream of tying the knot with her at the earliest. However, Appa, Uday’s father, plays spoilsport by bringing another daughter named Ranjana (Shruti Haasan) from his third marriage into the picture. He tells Uday Shetty (Nana Patekar) to get her married to someone from a good family. Maharani (Dimple Kapadia) puts in a condition that only after their sister is married to Chandni (Ankita Srivastava) will she marry one of them. The search to find a suitable boy for their sister Ranjana (Shruti Haasan) hence begins!\",\n" +
                    "\"duration\":8939,\n" +
                    "\"keywords\":[\n" +
                    "\"Item Song\",\n" +
                    "\"debutant\",\n" +
                    "\"Comedy\",\n" +
                    "\"Multi-Starrer\",\n" +
                    "\"Dual Actors\",\n" +
                    "\"2015 Release\",\n" +
                    "\"Welcome Back\",\n" +
                    "\"Welcome Back Movie\",\n" +
                    "\"Welcome Back Full Movie\"\n" +
                    "],\n" +
                    "\"castInfo\":[\n" +
                    "\"Anil Kapoor\",\n" +
                    "\"John Abraham\",\n" +
                    "\"Nana Patekar\",\n" +
                    "\"Paresh Rawal\",\n" +
                    "\"Naseeruddin Shah\",\n" +
                    "\"Shruti Haasan\",\n" +
                    "\"Dimple Kapadia\",\n" +
                    "\"Ankita Srivastava\"\n" +
                    "],\n" +
                    "\"language\":\"Hindi\",\n" +
                    "\"yearOfRelease\":2015,\n" +
                    "\"thumbnail\":\"https://images001-a.erosnow.com/movie/0/1023260/img14582/6608237/1023260_6608237_93.jpg\"\n" +
                    "},\n" +
                    "\"download_count\":0\n" +
                    "}]"

            val content = ContentEntity("BADLAPUR",
                    1, PaymentType.SVOD.mValue, bineMetadata, trailer, ott, null, svodPayDetails, recommendation)

            contentEntityDao.insert(content)


            val folder = FolderEntity(UUID.randomUUID().toString(), content.id, bineMetadata.title,
                    0,
                    bineMetadata.description,
                    "/bulkfiles/Aye_Basant_Bahar_SD_h264_high_480p_1000-video.mp4",
                    "Aye_Basant_Bahar_SD_h264_high_480p_1000-audio.mp4",
                    "https://images004-a.erosnow.com/movie/0/1055530/img625352/6742776/1055530_6742776_19.jpg",
                    934841918.9453125F,
                    6360,
                    "Aye_Basant_Bahar_SD/",
                    "Aye_Basant_Bahar_SD_h264_high_480p_1000.mpd",
                    "http://{HUB_IP}:5000/static/usr/bine/zzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzz/a4a70950-f963-48f9-bbbc-0c676b800cf6/bulkfiles"
            )
            folderEntityDao.insert(folder)
        }
    }
*/

    suspend fun getContent(id: String): ContentFolderEntity? {
        Log.e("inside repo", id)
        return contentEntityDao.getContentById(id)
    }

    suspend fun getContentListByIds(id: List<String>): List<ContentFolderEntity> {
        return contentEntityDao.getContentListByIds(id)
    }

    suspend fun addContentToCart(cartEntity: CartEntity) {
        cartEntityDao.insert(cartEntity)
    }

    fun getAllCartItems(): LiveData<List<CartEntity>> {
        return cartEntityDao.getCartItems()
    }

    fun getCartItem(contentId: String): LiveData<CartEntity> {
        return cartEntityDao.getCartItem(contentId)
    }

    suspend fun removeItemFromCart(cartEntity: CartEntity) {
        cartEntityDao.deleteItem(cartEntity)
    }
}