package io.zoemeow.dutapp.android.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.zoemeow.dutapp.android.repository.AccountFileRepository
import io.zoemeow.dutapp.android.repository.CacheFileRepository
import io.zoemeow.dutapp.android.repository.SettingsFileRepository
import java.io.File
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FileModule {
    @Provides
    @Singleton
    fun provideAccountFileRepository(@ApplicationContext context: Context): AccountFileRepository {
        val filePath = "${context.filesDir.path}/accounts.json"
        val file = File(filePath)
        return AccountFileRepository(file)
    }

    @Provides
    @Singleton
    fun provideCacheFileRepository(@ApplicationContext context: Context): CacheFileRepository {
        val filePath = "${context.cacheDir.path}/newsCacheGlobal.json"
        val file = File(filePath)
        val filePath2 = "${context.cacheDir.path}/newsCacheSubject.json"
        val file2 = File(filePath2)
        return CacheFileRepository(file, file2)
    }

    @Provides
    @Singleton
    fun provideSettingsFileRepository(@ApplicationContext context: Context): SettingsFileRepository {
        val filePath = "${context.filesDir.path}/settings.json"
        val file = File(filePath)
        return SettingsFileRepository(file)
    }
}