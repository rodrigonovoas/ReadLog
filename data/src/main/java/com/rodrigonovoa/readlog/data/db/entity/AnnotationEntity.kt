package com.rodrigonovoa.readlog.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "annotations",
    foreignKeys = [
        ForeignKey(
            entity = SessionEntity::class,
            parentColumns = ["sessionId"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["sessionId"])]
)
data class AnnotationEntity(
    @PrimaryKey(autoGenerate = true)
    val annotationId: Int = 0,
    val remoteId: String = "",
    val sessionId: Int,
    val sessionRemoteId: String = "",
    val annotation: String,
    val creationDate: Long = System.currentTimeMillis(),
    val lastModified: Long = 0L,
)
