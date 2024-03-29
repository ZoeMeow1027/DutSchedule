package io.zoemeow.dutschedule.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.zoemeow.dutschedule.repository.DutRequestRepository
import io.zoemeow.dutschedule.repository.FileModuleRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideFileModuleRepository(@ApplicationContext context: Context): FileModuleRepository {
        return FileModuleRepository(context)
    }

    @Provides
    @Singleton
    fun provideDutRequestRepository(): DutRequestRepository {
        return DutRequestRepository()
    }

}