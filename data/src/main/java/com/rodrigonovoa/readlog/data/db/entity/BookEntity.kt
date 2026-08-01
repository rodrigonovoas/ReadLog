package com.rodrigonovoa.readlog.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.rodrigonovoa.readlog.domain.model.BookState

@Entity(tableName = "books")
data class BookEntity(
    @PrimaryKey(autoGenerate = true)
    val bookId: Int = 0,
    val remoteId: String = "",
    val title: String,
    val author: String,
    val genre: String,
    val releaseDate: String,
    val numPages: Int,
    val currentPage: Int,
    val state: String = BookState.IN_PROGRESS.name,
    val creationDate: Long = System.currentTimeMillis(),
    val lastModified: Long = 0L,
    val statusDate: Long? = null,
)
