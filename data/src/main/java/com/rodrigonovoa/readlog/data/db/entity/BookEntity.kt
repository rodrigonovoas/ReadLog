package com.rodrigonovoa.readlog.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "books")
data class BookEntity(
    @PrimaryKey(autoGenerate = true)
    val bookId: Int = 0,
    val title: String,
    val author: String,
    val genre: String,
    val releaseDate: String,
    val numPages: Int,
    val currentPage: Int,
    val creationDate: Long = System.currentTimeMillis()
)
