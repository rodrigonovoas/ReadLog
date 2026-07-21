package com.rodrigonovoa.readlog.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.rodrigonovoa.readlog.data.db.dao.AnnotationDao
import com.rodrigonovoa.readlog.data.db.dao.BookDao
import com.rodrigonovoa.readlog.data.db.dao.SessionDao
import com.rodrigonovoa.readlog.data.db.dao.UserProfileInfoDao
import com.rodrigonovoa.readlog.data.db.entity.AnnotationEntity
import com.rodrigonovoa.readlog.data.db.entity.BookEntity
import com.rodrigonovoa.readlog.data.db.entity.SessionEntity
import com.rodrigonovoa.readlog.data.db.entity.UserProfileInfoEntity

@Database(
    entities = [BookEntity::class, SessionEntity::class, AnnotationEntity::class, UserProfileInfoEntity::class],
    version = 6,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class ReadLogDatabase : RoomDatabase() {
    abstract fun bookDao(): BookDao
    abstract fun sessionDao(): SessionDao
    abstract fun annotationDao(): AnnotationDao
    abstract fun userProfileInfoDao(): UserProfileInfoDao

    companion object {
        val MIGRATION_1_2 = androidx.room.migration.Migration(1, 2) { database ->
            database.execSQL("ALTER TABLE books ADD COLUMN remoteId TEXT NOT NULL DEFAULT ''")
            database.execSQL("ALTER TABLE books ADD COLUMN lastModified INTEGER NOT NULL DEFAULT 0")
            database.execSQL("CREATE UNIQUE INDEX index_books_remoteId ON books(remoteId)")

            database.execSQL("ALTER TABLE sessions ADD COLUMN remoteId TEXT NOT NULL DEFAULT ''")
            database.execSQL("ALTER TABLE sessions ADD COLUMN bookRemoteId TEXT NOT NULL DEFAULT ''")
            database.execSQL("ALTER TABLE sessions ADD COLUMN lastModified INTEGER NOT NULL DEFAULT 0")
            database.execSQL("CREATE UNIQUE INDEX index_sessions_remoteId ON sessions(remoteId)")

            database.execSQL("ALTER TABLE annotations ADD COLUMN remoteId TEXT NOT NULL DEFAULT ''")
            database.execSQL("ALTER TABLE annotations ADD COLUMN sessionRemoteId TEXT NOT NULL DEFAULT ''")
            database.execSQL("ALTER TABLE annotations ADD COLUMN lastModified INTEGER NOT NULL DEFAULT 0")
            database.execSQL("CREATE UNIQUE INDEX index_annotations_remoteId ON annotations(remoteId)")
        }

        val MIGRATION_2_3 = androidx.room.migration.Migration(2, 3) { database ->
            database.execSQL(
                """
                CREATE TABLE IF NOT EXISTS user_profile_stats (
                    userId TEXT NOT NULL PRIMARY KEY,
                    followersCount INTEGER NOT NULL DEFAULT 0,
                    likesCount INTEGER NOT NULL DEFAULT 0,
                    sessionsThisWeek INTEGER NOT NULL DEFAULT 0,
                    weekTimeSeconds INTEGER NOT NULL DEFAULT 0,
                    bookCollection TEXT NOT NULL DEFAULT '',
                    lastModified INTEGER NOT NULL DEFAULT 0
                )
                """.trimIndent()
            )
        }

        val MIGRATION_3_4 = androidx.room.migration.Migration(3, 4) { database ->
            database.execSQL("ALTER TABLE user_profile_stats ADD COLUMN displayName TEXT")
        }

        val MIGRATION_4_5 = androidx.room.migration.Migration(4, 5) { database ->
            database.execSQL("ALTER TABLE user_profile_stats RENAME TO user_profile_info")
        }

        val MIGRATION_5_6 = androidx.room.migration.Migration(5, 6) { database ->
            database.execSQL("ALTER TABLE user_profile_info ADD COLUMN username TEXT")
        }
    }
}
