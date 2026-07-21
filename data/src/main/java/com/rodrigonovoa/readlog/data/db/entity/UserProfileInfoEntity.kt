package com.rodrigonovoa.readlog.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile_info")
data class UserProfileInfoEntity(
    @PrimaryKey val userId: String,
    val followersCount: Int = 0,
    val likesCount: Int = 0,
    val sessionsThisWeek: Int = 0,
    val weekTimeSeconds: Long = 0L,
    val bookCollection: List<String> = emptyList(),
    val lastModified: Long = 0L,
    val displayName: String? = null,
    val username: String? = null,
)
