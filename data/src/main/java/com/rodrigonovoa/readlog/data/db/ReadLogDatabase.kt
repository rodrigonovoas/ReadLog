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
    version = 1,
    exportSchema = false
)
abstract class ReadLogDatabase : RoomDatabase() {
    abstract fun bookDao(): BookDao
    abstract fun sessionDao(): SessionDao
    abstract fun annotationDao(): AnnotationDao
}
