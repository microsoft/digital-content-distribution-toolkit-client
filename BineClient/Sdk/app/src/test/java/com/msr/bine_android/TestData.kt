// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.msr.bine_android

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.msr.bine_android.data.entities.CartEntity
import com.msr.bine_android.data.entities.ContentEntity
import com.msr.bine_android.data.entities.ContentFolderEntity
import com.msr.bine_android.data.entities.FolderEntity
import com.msr.bine_sdk.models.*
import java.util.*

fun getDummyBineMetaData() :BineMetadata {
    return BineMetadata("Dunkrik",
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
}

fun getDummyMovieBineMetaData() :BineMetadata {
    return BineMetadata("Dunkrik",
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
}

fun getDummTrailer(): Trailer {
    return Trailer(25000F, "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4", 340)
}

fun getDummyOTT(): OTT {
    return  OTT("ALT Balaji", "https://www.logodesign.net/images/nature-logo.png", "")
}

fun getDummySVOD() : SVODPayDetails {
    return SVODPayDetails(Calendar.getInstance().time,
            Calendar.getInstance().time,
            SubscriptionType.WEEKLY.mValue, 99.0, Status.NotPurchased.mValue)
}

fun getDummyTVOD(): TVODPayDetails {
    return TVODPayDetails(99.0, Status.NotPurchased.mValue)
}
fun getDummyRecommendation(): String {
    return "[{\n" +
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
}

fun getFolder(content: ContentEntity): FolderEntity {
    return FolderEntity(UUID.randomUUID().toString(), content.id, content.metadata.title,
            0,
            content.metadata.description,
            "/bulkfiles/Aye_Basant_Bahar_SD_h264_high_480p_1000-video.mp4",
            "Aye_Basant_Bahar_SD_h264_high_480p_1000-audio.mp4",
            "https://images004-a.erosnow.com/movie/0/1055530/img625352/6742776/1055530_6742776_19.jpg",
            934841918.9453125F,
            6360,
            "Aye_Basant_Bahar_SD/",
            "Aye_Basant_Bahar_SD_h264_high_480p_1000.mpd",
            "http://{HUB_IP}:5000/static/usr/bine/zzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzz/a4a70950-f963-48f9-bbbc-0c676b800cf6/bulkfiles"
    )
}

fun getDummySVODContent(): ContentEntity {
    val bineMetadata = getDummyBineMetaData()
    return ContentEntity("BADLAPUR",
            1, PaymentType.SVOD.mValue,
            bineMetadata,
            getDummTrailer(),
            getDummyOTT(),
            null,
            getDummySVOD(),
            getDummyRecommendation())
}

fun getDummyMovieContent(): ContentEntity {
    val bineMetadata = getDummyMovieBineMetaData()
    return ContentEntity("BADLAPUR",
            1, PaymentType.SVOD.mValue,
            bineMetadata,
            getDummTrailer(),
            getDummyOTT(),
            null,
            getDummySVOD(),
            getDummyRecommendation())
}
fun getDummyTVODContent(): ContentEntity {
    val bineMetadata = getDummyBineMetaData()
    return ContentEntity("BADLAPUR",
            1, PaymentType.TVOD.mValue,
            bineMetadata,
            getDummTrailer(),
            getDummyOTT(),
            getDummyTVOD(),
            null,
            getDummyRecommendation())
}


fun getDummyFreeContent(): ContentEntity {
    val bineMetadata = getDummyBineMetaData()
    return ContentEntity("BADLAPUR",
            1, PaymentType.FREE.mValue,
            bineMetadata,
            getDummTrailer(),
            getDummyOTT(),
            null,
            null,
            getDummyRecommendation())
}

fun getSVODContentFolderEntity(): ContentFolderEntity {
    val content = getDummySVODContent()
    val folder = getFolder(content)
    val list = mutableListOf<FolderEntity>()
    list.add(folder)
    return ContentFolderEntity(content, list)
}

fun getFreeContentFolderEntity(): ContentFolderEntity {
    val content = getDummyFreeContent()
    val folder = getFolder(content)
    val list = mutableListOf<FolderEntity>()
    list.add(folder)

    return ContentFolderEntity(content, list)
}

fun getTVODContentFolderEntity(): ContentFolderEntity {
    val content = getDummyTVODContent()
    val folder = getFolder(content)
    val list = mutableListOf<FolderEntity>()
    list.add(folder)

    return ContentFolderEntity(content, list)
}

fun getDummyCartItems(): LiveData<List<CartEntity>>{
    val cartEntity= CartEntity("BADLAPUR")
    val data: MutableLiveData<List<CartEntity>> = MutableLiveData()
    val list = mutableListOf<CartEntity>()
    list.add(cartEntity)
    data.value=list
    return data
}

fun getDummyContentList():MutableList<ContentFolderEntity>{
    val list = mutableListOf<ContentFolderEntity>()
    list.add(getSVODContentFolderEntity())
    list.add(getTVODContentFolderEntity())
    return list
}