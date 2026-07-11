package com.rodrigonovoa.readlog.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.rodrigonovoa.readlog.data.db.entity.BookEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {

    @Insert
    suspend fun insert(book: BookEntity): Long

    @Update
    suspend fun update(book: BookEntity)

    @Delete
    suspend fun delete(book: BookEntity)

    @Query("SELECT * FROM books ORDER BY creationDate DESC")
    fun getAll(): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE bookId = :id")
    suspend fun getById(id: Int): BookEntity?

    @Query("SELECT COUNT(*) FROM books")
    suspend fun getAllCount(): Int
}
