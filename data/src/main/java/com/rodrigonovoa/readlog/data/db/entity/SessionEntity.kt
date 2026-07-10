package com.rodrigonovoa.readlog.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sessions",
    foreignKeys = [
        ForeignKey(
            entity = BookEntity::class,
            parentColumns = ["bookId"],
            childColumns = ["bookId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["bookId"])]
)
data class SessionEntity(
    @PrimaryKey(autoGenerate = true)
    val sessionId: Int = 0,
    val bookId: Int,
    val time: Long,
    val creationDate: Long = System.currentTimeMillis()
)
