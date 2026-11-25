package com.aimp.compose.di

import android.content.Context
import com.aimp.compose.data.AimpDatabase
import com.aimp.compose.data.LyricsDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AimpDatabase = AimpDatabase.get(context)

    @Provides
    fun provideLyricsDao(db: AimpDatabase): LyricsDao = db.lyricsDao()
}
