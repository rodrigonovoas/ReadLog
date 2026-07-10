package com.rodrigonovoa.readlog.data.di

import android.content.Context
import androidx.room.Room
import com.rodrigonovoa.readlog.data.db.ReadLogDatabase
import com.rodrigonovoa.readlog.data.db.dao.AnnotationDao
import com.rodrigonovoa.readlog.data.db.dao.BookDao
import com.rodrigonovoa.readlog.data.db.dao.SessionDao
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
    fun provideDatabase(
        @ApplicationContext context: Context
    ): ReadLogDatabase {
        return Room.databaseBuilder(
            context,
            ReadLogDatabase::class.java,
            "readlog_database"
        ).build()
    }

    @Provides
    fun provideBookDao(database: ReadLogDatabase): BookDao = database.bookDao()

    @Provides
    fun provideSessionDao(database: ReadLogDatabase): SessionDao = database.sessionDao()

    @Provides
    fun provideAnnotationDao(database: ReadLogDatabase): AnnotationDao = database.annotationDao()
}
