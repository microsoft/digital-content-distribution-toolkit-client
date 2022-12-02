// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.microsoft.mobile.polymer.mishtu.storage

import androidx.room.TypeConverter

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.msr.bine_sdk.cloud.models.Content
import java.lang.reflect.Type
import java.util.*
import kotlin.collections.ArrayList

class Converters {
    @TypeConverter
    fun restoreList(listOfString: String?): ArrayList<String?>? {
        return Gson().fromJson(listOfString, object : TypeToken<ArrayList<String?>?>() {}.type)
    }

    @TypeConverter
    fun saveList(listOfString: ArrayList<String?>?): String? {
        return Gson().toJson(listOfString)
    }

    @TypeConverter
    fun toDate(dateLong: Long?): Date? {
        return dateLong?.let { Date(it) }
    }

    @TypeConverter
    fun fromDate(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun storedStringToAttachment(data: String): List<Content.Attachment> {
        val gson = Gson()
        val listType: Type? = object : TypeToken<List<Content.Attachment>>() {}.type
        return gson.fromJson(data, listType)
    }

    @TypeConverter
    fun attachmentsToStoredString(myObjects: List<Content.Attachment>): String {
        val gson = Gson()
        return gson.toJson(myObjects)
    }

    @TypeConverter
    fun storedStringToArtists(data: String): List<Content.Artist> {
        val gson = Gson()
        val listType: Type? = object : TypeToken<List<Content.Artist>>() {}.type
        return gson.fromJson(data, listType)
    }

    @TypeConverter
    fun artistsToStoredString(myObjects: List<Content.Artist>): String {
        val gson = Gson()
        return gson.toJson(myObjects)
    }
}