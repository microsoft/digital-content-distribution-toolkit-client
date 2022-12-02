// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.microsoft.mobile.polymer.mishtu.storage

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.microsoft.mobile.polymer.mishtu.storage.entities.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Database(entities = [Content::class,
    ContentProvider::class,
    Downloads::class,
    SubscriptionPack::class,
    Order::class,
    ActiveSubscription::class],
    version = 2,
    exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase: RoomDatabase()  {

    abstract fun contentDao(): ContentDao
    abstract fun contentProviderDao(): ContentProviderDao
    abstract fun subscriptionPackDao(): SubscriptionPackDao
    abstract fun activeSubscriptionPackDao(): ActiveSubscriptionDao
    abstract fun orderDao(): OrderDao
    abstract fun downloadsDao(): DownloadsDao
    abstract fun contentDownloadDao(): ContentDownloadDao

    companion object {
        private const val NUMBER_OF_THREADS = 4
        val databaseWriteExecutor: ExecutorService = Executors.newFixedThreadPool(NUMBER_OF_THREADS)
    }
}