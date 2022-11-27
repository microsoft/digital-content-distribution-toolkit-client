package com.msr.bine_android.utils

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

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
    fun toDate(dateLong: Long?): java.util.Date? {
        return dateLong?.let { java.util.Date(it) }
    }

    @TypeConverter
    fun fromDate(date: java.util.Date?): Long? {
        return date?.time
    }
}