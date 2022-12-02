// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.microsoft.mobile.polymer.mishtu.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.microsoft.mobile.polymer.mishtu.offline.DownloadManager
import com.microsoft.mobile.polymer.mishtu.storage.AppDatabase
import com.microsoft.mobile.polymer.mishtu.storage.repositories.*
import com.microsoft.mobile.polymer.mishtu.storage.snappyDB.SnappyDB
import com.microsoft.mobile.polymer.mishtu.utils.BineNotificationHelper
import com.microsoft.mobile.polymer.mishtu.utils.DeviceConnect
import com.microsoft.mobile.polymer.mishtu.utils.NotificationHandler
import com.microsoft.mobile.polymer.mishtu.utils.SubscriptionManager
import com.msr.bine_sdk.download.Downloader
import com.msr.bine_sdk.notifications.NotificationHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DataModule {

    @Singleton
    @Provides
    fun provideSnappyDB(@ApplicationContext context: Context): SnappyDB {
        return SnappyDB.getInstance(context)
    }

    @Singleton
    @Provides
    fun provideNotificationRepository(): NotificationBadgeHelper {
        return NotificationBadgeHelper()
    }

    @Singleton
    @Provides
    fun provideDeviceConnect(@ApplicationContext context: Context): DeviceConnect {
        return DeviceConnect(context)
    }

    @Singleton
    @Provides
    fun provideSubscriptionRepository(
        database: AppDatabase,
        ioDispatcher: CoroutineDispatcher
    ): SubscriptionRepository {
        return SubscriptionRepository(database.subscriptionPackDao(), ioDispatcher,database.contentProviderDao(),database.activeSubscriptionPackDao())
    }

    @Singleton
    @Provides
    fun provideNotificationHandler(contentRepository: ContentRepository,
                                   orderRepository: OrderRepository,
                                   notificationBadgeHelper: NotificationBadgeHelper): NotificationHandler {
        return NotificationHandler(contentRepository, orderRepository, notificationBadgeHelper)
    }

    @Singleton
    @Provides
    fun provideOrderRepository(database: AppDatabase, downloadManager: DownloadManager, subscriptionManager: SubscriptionManager): OrderRepository {
        return OrderRepository(database.orderDao(),
            database.activeSubscriptionPackDao(),
            downloadManager,
            database.contentDownloadDao(),
            database.contentProviderDao(),
            database.subscriptionPackDao(),
            subscriptionManager)
    }

    @Singleton
    @Provides
    fun provideSubscriptionManager(subscriptionRepository: SubscriptionRepository): SubscriptionManager{
        return SubscriptionManager(subscriptionRepository)
    }

    @Singleton
    @Provides
    fun provideContentRepository(
        database: AppDatabase,
        ioDispatcher: CoroutineDispatcher,
        snappyDB: SnappyDB
    ): ContentRepository {
        return ContentRepository(
            database.contentDao(),
            database.contentProviderDao(),
            database.downloadsDao(),
            database.contentDownloadDao(),
            ioDispatcher, snappyDB)
    }

    @Singleton
    @Provides
    fun provideIncentiveRepository(
        snappyDB: SnappyDB
    ): IncentivesRepository {
        return IncentivesRepository(snappyDB)
    }

    @Singleton
    @Provides
    fun provideDownloader(
        @ApplicationContext context: Context,
        notificationHelper: NotificationHelper
    ): Downloader {
        return Downloader(context, notificationHelper)
    }

    @Provides
    fun provideNotificationHelper(@ApplicationContext application: Context): NotificationHelper {
        return BineNotificationHelper(application)
    }

    @Provides
    fun provideDownloadManager(repository: ContentRepository, downloader: Downloader, deviceConnect: DeviceConnect,
                               notificationBadgeHelper: NotificationBadgeHelper): DownloadManager {
        return DownloadManager( repository, downloader, deviceConnect, notificationBadgeHelper)
    }

    @Singleton
    @Provides
    fun provideDataBase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "user_db.db"
        ).addMigrations(MIGRATION_1_2).build()
    }

    private fun schemaUpdated(database: SupportSQLiteDatabase): Boolean {
        return try {
            database.query("SELECT subscription_type FROM SubscriptionPack LIMIT 1")
            true
        } catch (e: Exception) {
            false
        }

    }

    @Singleton
    @Provides
    fun provideIoDispatcher() = Dispatchers.IO

    private val MIGRATION_1_2: Migration = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            val dbVersion = database.version
            if(dbVersion == 1 && !schemaUpdated(database)) {
                database.execSQL("Delete from SubscriptionPack")
                database.execSQL("ALTER TABLE SubscriptionPack ADD COLUMN subscription_type TEXT DEFAULT '' NOT NULL")
                database.execSQL("ALTER TABLE SubscriptionPack ADD COLUMN contentId_list TEXT")
                database.execSQL("ALTER TABLE ActiveSubscription ADD COLUMN subscription_type TEXT DEFAULT '' NOT NULL")
                database.execSQL("ALTER TABLE ActiveSubscription ADD COLUMN contentId_list TEXT")
                database.execSQL("ALTER TABLE 'Order' ADD COLUMN subscription_id TEXT DEFAULT '' NOT NULL")
            }

        }
    }
    /*
    private val MIGRATION_2_3: Migration = object : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE 'Order' ADD COLUMN duration INTEGER DEFAULT 0 NOT NULL")
        }
    }

    private val MIGRATION_3_4: Migration = object : Migration(3, 4) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE 'Content' ADD COLUMN ageAppropriateness TEXT")
            database.execSQL("ALTER TABLE 'Content' ADD COLUMN contentAdvisory TEXT")
        }
    }*/
}