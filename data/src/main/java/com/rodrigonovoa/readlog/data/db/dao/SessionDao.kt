package com.rodrigonovoa.readlog.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.rodrigonovoa.readlog.data.db.entity.SessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {

    @Insert
    suspend fun insert(session: SessionEntity): Long

    @Update
    suspend fun update(session: SessionEntity)

    @Delete
    suspend fun delete(session: SessionEntity)

    @Query("SELECT * FROM sessions WHERE bookId = :bookId ORDER BY creationDate DESC")
    fun getAllForBook(bookId: Int): Flow<List<SessionEntity>>

    @Query("SELECT * FROM sessions WHERE sessionId = :id")
    suspend fun getById(id: Int): SessionEntity?
}
