package com.rodrigonovoa.readlog.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.rodrigonovoa.readlog.data.db.dao.AnnotationDao
import com.rodrigonovoa.readlog.data.db.dao.BookDao
import com.rodrigonovoa.readlog.data.db.dao.SessionDao
import com.rodrigonovoa.readlog.data.db.entity.AnnotationEntity
import com.rodrigonovoa.readlog.data.db.entity.BookEntity
import com.rodrigonovoa.readlog.data.db.entity.SessionEntity

@Database(
    entities = [BookEntity::class, SessionEntity::class, AnnotationEntity::class],
    version = 2,
    exportSchema = false
)
abstract class ReadLogDatabase : RoomDatabase() {
    abstract fun bookDao(): BookDao
    abstract fun sessionDao(): SessionDao
    abstract fun annotationDao(): AnnotationDao

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
    }
}
