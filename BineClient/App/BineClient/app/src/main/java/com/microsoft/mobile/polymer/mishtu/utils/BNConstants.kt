// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.microsoft.mobile.polymer.mishtu.utils

import com.microsoft.mobile.polymer.mishtu.BuildConfig
import com.microsoft.mobile.polymer.mishtu.storage.SharedPreferenceStore
import com.msr.bine_sdk.BineAPI

object BNConstants {
    const val CUT_OFF_DATE = "2022-12-31T00:00:00Z"
    const val DEVICE_SSID = "meramishtu"
    const val DEVICE_HOSTNAME = "vodegateway"
    const val PAGE_SIZE = 200

    const val KEY_EXTRA_DEVICE_SSID = "SSID"
    const val KEY_EXTRA_DEVICE_IP = "DEVICE_IP"
    const val SQUARE_LOGO_NAME = "pictorialmark_square.png"
    const val WATERMARK_LOGO_NAME = "water_mark.png"
    const val FIREWORK_SHORT_VIDEO_CHANNEL_ID = "wYMP071"
    const val FIREWORK_MISHTU_TRAILER_CHANNEL_ID = "m0arqbq"
    const val FIREWORK_MISHTU_TRAILER_PLAYLIST_ID = "5DKEjv"


    enum class Service {
        ENTERTAINMENT,
        JOBS,
        OFFERS,
        GOVERNMENT,
        NONE
    }

    val Services = arrayOf("Entertainment:bn_service_entertainment:bn_service_entertainment_desc",
        "ExploreOffers:bn_service_offers:bn_service_offers_desc",
        "Jobs:bn_service_jobs:bn_service_jobs_desc")/*,
            "GovernmentServices:bn_service_government")*/

    val FwCategories = arrayOf("Travel:bn_fw_travel:LN4xnbk",
        "Food:bn_fw_food:nd6Ql8V",
        "Comedy:bn_fw_comedy:4Y09BNZ",
        "News:bn_fw_news:N1A8Nd0",
        "Fitness:bn_fw_fitness:R71L9XL",
        "Fashion:bn_fw_fashion:jE7D89E",
        "Movie:bn_fw_movie:wYMP071",
        "Celebrity:bn_fw_celebrity:ma1DNAR")

    internal enum class CHILDTYPE(val value: String) {
        FIREWORKFULLSCREENBANNER("FireworkFullScreenBanner"),
        TOPMOVIE("TopMovies"),
        FRESHARRIVAL("FreshArrival"),
        MISHTUSHORTCLIPS("MishtuShortClips"),
        FIREWORKSHORTCLIPS("FireworkShortClips"),
        MSNVIEW("MSNView"),
        EXCLUSIVE("ErosExclusive"),
        EXCLUSIVEMOVIES("ErosExclusiveMovies"),
        EXCLUSIVESERIES("ErosExclusiveSeries"),
        FIREWORKCATEGORIES("FireworkCategories"),
        BUYPACK("BuyPack"),
        CONTINUEWATCH("ContinueWatch"),
        LANGUAGELIST("LanguagesList"),
        GENRESLIST("GenresList"),
        RECOMMENDEDMOVIES("RecommendedMovies"),
        TRENDINGMOVIES("TrendingMovies"),
        RECOMMENDEDSERIES("RecommendedSeries"),
        TRENDINGSERIES("TrendingSeries"),
        TRENDINGSERIESTRAILERS("TrendingSeriesTrailers"),
        TRENDINGMOVIESTRAILERS("TrendingMoviesTrailers"),
        PAIDMOVIES("PaidMovies"),
        FREEMOVIES("FreeMovies"),
        PACK_PROMOTION_BANNER("PackPromoBanner"),
        BIGMOVIECARD("BigMovieCard"),
        OFFLINEPROMOCARD("OfflinePromoCard")
    }

    val Languages = arrayOf(
        "English:bn_ln_english",
        "Hindi:bn_ln_hindi",
        "Bengali:bn_ln_bengali",
        "Tamil:bn_ln_tamil",
        "Telugu:bn_ln_telugu",
        "Kannada:bn_ln_kannada",
        "Malayalam:bn_ln_malayalam",
        "Punjabi:bn_ln_punjabi",
        "Gujarati:bn_ln_gujarati",
        "Marathi:bn_ln_marathi",
        "Assamese:bn_ln_assamese",
        "Konkani:bn_ln_hindi",
        "Odia:bn_ln_odia")

    val LanguagesCode = arrayOf(
        "en",
        "hi",
        "bn",
        "ta",
        "te",
        "Kn",
        "ml",
        "pa",
        "gu",
        "mr",
        "as",
        "kok",
        "or"
    )

    val Genres = arrayOf(
        "Drama:bn_gn_drama",
        "Family:bn_gn_family",
        "Reality:bn_gn_reality",
        "Crime:bn_gn_crime",
        "Romance:bn_gn_romance",
        "Action:bn_gn_action",
        "Thriller:bn_gn_thriller",
        "Fantasy:bn_gn_fantasy",
        "Mythology:bn_gn_mythology"
    )

    val ALL = arrayOf("FireworkCategories",
        "FireworkShortClips",
        "TopMovies",
        "FreshArrival",
        "ErosExclusive")

    val CLIPS = arrayOf("FireworkCategories",
        "MishtuShortClips",
        "FireworkShortClips")

    val MOVIES = arrayOf(

        "BigMovieCard",
        "ContinueWatch",
        "BigMovieCard",
        "PackPromoBanner",
        "BigMovieCard",
        "OfflinePromoCard",
        "BigMovieCard",
        /*"PaidMovies",*/
        "BigMovieCard",
        "BigMovieCard",
        "FreeMovies",
        "BigMovieCard",
        "BigMovieCard",
        "LanguagesList",
        "BigMovieCard",
        "BigMovieCard",
        "GenresList"
        )

    val SERIES = arrayOf(
        "BigMovieCard",
        "ContinueWatch",
        "BigMovieCard",
        "PackPromoBanner",
        "BigMovieCard",
        "OfflinePromoCard",
        "LanguagesList",
        "BigMovieCard",
        "FreeMovies",
        "BigMovieCard",
        "GenresList"
    )

    val Avatars = arrayOf(
        "avatar_1",
        "avatar_2",
        "avatar_3",
        "avatar_4",
        "avatar_5",
        "avatar_6",
        "avatar_7",
        "avatar_8",
        "avatar_9",
        "avatar_10",
        "avatar_11",
        "avatar_12"
    )

    const val EXCLUSIVE = "Exclusive"
    const val DATE_FORMAT_dd_MM_YY = "dd/MM/yyyy"

    const val CONTENT_VIEW_ALL_POSITION = 4
    const val formatter = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"

    enum class NotificationType(val value: String) {
        NewContentArrived("NewArrival"),
        NewOffer("NewOffer"),
        OrderComplete("OrderComplete"),
        UserDataExportComplete("UserDataExportComplete"),
        Downloads("Download");

        companion object {
            fun fromInt(value: String): NotificationType {
                for (type in values()) {
                    if (value == type.value) {
                        return type
                    }
                }
                throw UnsupportedOperationException("No Notification with value : $value")
            }
        }
    }

    //for rooted device check
    val knownRootAppsPackages = arrayOf(
        "com.noshufou.android.su",
        "com.noshufou.android.su.elite",
        "eu.chainfire.supersu",
        "com.koushikdutta.superuser",
        "com.thirdparty.superuser",
        "com.yellowes.su",
        "com.topjohnwu.magisk",
        "com.kingroot.kinguser",
        "com.kingo.root",
        "com.smedialink.oneclickroot",
        "com.zhiqupk.root.global",
        "com.alephzain.framaroot"
    )

    //for rooted device check
    val knownDangerousAppsPackages = arrayOf(
        "com.koushikdutta.rommanager",
        "com.koushikdutta.rommanager.license",
        "com.dimonvideo.luckypatcher",
        "com.chelpus.lackypatch",
        "com.ramdroid.appquarantine",
        "com.ramdroid.appquarantinepro",
        "com.android.vending.billing.InAppBillingService.COIN",
        "com.android.vending.billing.InAppBillingService.LUCK",
        "com.chelpus.luckypatcher",
        "com.blackmartalpha",
        "org.blackmart.market",
        "com.allinone.free",
        "com.repodroid.app",
        "org.creeplays.hack",
        "com.baseappfull.fwd",
        "com.zmapp",
        "com.dv.marketmod.installer",
        "org.mobilism.android",
        "com.android.wp.net.log",
        "com.android.camera.update",
        "cc.madkite.freedom",
        "com.solohsu.android.edxp.manager",
        "org.meowcat.edxposed.manager",
        "com.xmodgame",
        "com.cih.game_cih",
        "com.charles.lpoqasert",
        "catch_.me_.if_.you_.can_"
    )
    val PRIVACY_POLICY_URL = "https://privacy.microsoft.com/en-us/PrivacyStatement"
    val TERMS_OF_USE_URL = if(BuildConfig.BUILD_TYPE == "release") {
        "https://meramishtu.com/portal/assets/doc/Mishtu-Terms-of-Use.html"
    } else {
        BuildConfig.CDN_BASE_URL + "blendnet-assets/Mishtu-Terms-of-Use.html"
    }

    fun getDeviceIP(): String {
        val settingsIP = SharedPreferenceStore.getInstance().get(KEY_EXTRA_DEVICE_IP)
        if (!settingsIP.isNullOrEmpty()) return settingsIP
        return DEVICE_HOSTNAME
    }

    enum class APIError(val value: String) {
        ACCOUNT_DELETE_IN_PROGRESS("USR_ERR_020")
    }

    fun bineSDKEnvironment(): BineAPI.Environment {
        return when(BuildConfig.BUILD_TYPE) {
            // DEV_ENV: Pointing debug to staging as we frequently use stage in debug mode.
            // If dev us required uncomment below section
            "debug" -> BineAPI.Environment.STAGE
            "stage" -> BineAPI.Environment.STAGE
            "release" -> BineAPI.Environment.PROD
            "unsigned" -> BineAPI.Environment.PROD
            else -> BineAPI.Environment.DEV
        }
    }
}