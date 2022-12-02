// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.msr.bine_android.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.msr.bine_android.data.entities.*
import com.msr.bine_android.utils.Converters
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Database(entities = [Folder::class,
    FolderEntity::class,
    ContentEntity::class,
    CartEntity::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun folderDao(): FolderDao
    abstract fun contentDao(): ContentEntityDao
    abstract fun cartDao(): CartEntityDao
    abstract fun folderEntityDao(): FolderEntityDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        private const val NUMBER_OF_THREADS = 4
        val databaseWriteExecutor: ExecutorService = Executors.newFixedThreadPool(NUMBER_OF_THREADS)

        fun getDatabase(context: Context): AppDatabase {
            if (INSTANCE == null) {
                synchronized(AppDatabase::class.java) {
                    if (INSTANCE == null) {
                        INSTANCE = Room.databaseBuilder(context.applicationContext,
                                AppDatabase::class.java, "folder_db")
                                .build()
                    }
                }
            }
            return INSTANCE!!
        }
    }
}