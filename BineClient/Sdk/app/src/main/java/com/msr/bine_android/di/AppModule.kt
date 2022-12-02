package com.msr.bine_android.di

import android.content.Context
import com.msr.bine_android.data.AppDatabase
import com.msr.bine_android.data.DataRepository
import com.msr.bine_android.data.SharedPreferenceStore
import com.msr.bine_android.data.entities.CartEntityDao
import com.msr.bine_android.data.entities.ContentEntityDao
import com.msr.bine_android.data.entities.FolderDao
import com.msr.bine_android.data.entities.FolderEntityDao
import com.msr.bine_android.download.DownloadManager
import com.msr.bine_android.utils.BineNotificationHelper
import com.msr.bine_sdk.BineAPI
import com.msr.bine_sdk.BineConnect
import com.msr.bine_sdk.download.Downloader
import com.msr.bine_sdk.notifications.NotificationHelper
import com.msr.bine_sdk.secure.BineSharedPreference
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    /*@Provides
    @Singleton
    @ApplicationScope
    open fun provideContext(): Context {
        return application
    }

    @Provides
    open fun provideApplication(): Application {
        return application
    }*/

    @Provides
    fun provideBineSharePref(@ApplicationContext context: Context): BineSharedPreference {
        return BineSharedPreference(context)
    }

    @Singleton
    @Provides
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Provides
    fun provideFolderDao(database: AppDatabase): FolderDao {
        return database.folderDao()
    }

    @Provides
    fun provideFolderEntityDao(database: AppDatabase): FolderEntityDao {
        return database.folderEntityDao()
    }

    @Provides
    fun provideContentEntityDao(database: AppDatabase): ContentEntityDao {
        return database.contentDao()
    }
    @Provides
    fun provideCartEntityDao(database: AppDatabase): CartEntityDao {
        return database.cartDao()
    }

    @Singleton
    @Provides
    fun provideSharedPref(@ApplicationContext application: Context): SharedPreferenceStore {
        return SharedPreferenceStore(application)
    }

    @Provides
    fun provideDataRepository(@ApplicationContext application: Context): DataRepository {
        return DataRepository.getInstance(
                AppDatabase.getDatabase(application).folderDao(),
                AppDatabase.getDatabase(application).contentDao(),
                AppDatabase.getDatabase(application).cartDao(),
                AppDatabase.getDatabase(application).folderEntityDao(),
                SharedPreferenceStore(application))
    }

    @Provides
    fun provideBineAPI(): BineAPI {
        return BineAPI.getInstance()
    }

    @Provides
    fun provideBineConnect(@ApplicationContext application: Context): BineConnect {
        return BineConnect.getInstance(application)
    }

    @Provides
    fun provideDownloader(@ApplicationContext application: Context, notificationHelper: NotificationHelper): Downloader {
        return Downloader(application, notificationHelper)
    }

    @Provides
    fun provideNotificationHelper(@ApplicationContext application: Context): NotificationHelper {
        return BineNotificationHelper(application)
    }

    @Provides
    fun provideDownloadManager(@ApplicationContext application: Context, repository: DataRepository, downloader: Downloader, bineAPI: BineAPI): DownloadManager {
        return DownloadManager(application, repository, downloader, bineAPI)
    }
}