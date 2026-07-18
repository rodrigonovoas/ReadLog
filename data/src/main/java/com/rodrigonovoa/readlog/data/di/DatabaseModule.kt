package com.rodrigonovoa.readlog.data.di

import android.content.Context
import androidx.room.Room
import com.rodrigonovoa.readlog.data.db.ReadLogDatabase
import com.rodrigonovoa.readlog.data.db.dao.AnnotationDao
import com.rodrigonovoa.readlog.data.db.dao.BookDao
import com.rodrigonovoa.readlog.data.db.dao.SessionDao
import com.rodrigonovoa.readlog.data.db.dao.UserProfileInfoDao
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
        )
            .addMigrations(
                ReadLogDatabase.MIGRATION_1_2,
                ReadLogDatabase.MIGRATION_2_3,
                ReadLogDatabase.MIGRATION_3_4,
                ReadLogDatabase.MIGRATION_4_5,
            )
            .build()
    }

    @Provides
    fun provideBookDao(database: ReadLogDatabase): BookDao = database.bookDao()

    @Provides
    fun provideSessionDao(database: ReadLogDatabase): SessionDao = database.sessionDao()

    @Provides
    fun provideAnnotationDao(database: ReadLogDatabase): AnnotationDao = database.annotationDao()

    @Provides
    fun provideUserProfileInfoDao(database: ReadLogDatabase): UserProfileInfoDao =
        database.userProfileInfoDao()
}
