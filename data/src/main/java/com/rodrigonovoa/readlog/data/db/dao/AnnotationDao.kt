package com.rodrigonovoa.readlog.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.rodrigonovoa.readlog.data.db.entity.AnnotationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AnnotationDao {

    @Insert
    suspend fun insert(annotation: AnnotationEntity): Long

    @Update
    suspend fun update(annotation: AnnotationEntity)

    @Delete
    suspend fun delete(annotation: AnnotationEntity)

    @Query("SELECT * FROM annotations WHERE sessionId = :sessionId ORDER BY creationDate DESC")
    fun getAllForSession(sessionId: Int): Flow<List<AnnotationEntity>>

    @Query("SELECT * FROM annotations WHERE annotationId = :id")
    suspend fun getById(id: Int): AnnotationEntity?
}
