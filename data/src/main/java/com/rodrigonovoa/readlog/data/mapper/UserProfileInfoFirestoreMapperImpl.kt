package com.rodrigonovoa.readlog.data.mapper

import com.rodrigonovoa.readlog.domain.model.UserProfileInfo
import javax.inject.Inject

class UserProfileInfoFirestoreMapperImpl @Inject constructor() : UserProfileInfoFirestoreMapper {

    override fun toFirestoreMap(info: UserProfileInfo): Map<String, Any> {
        return mapOf(
            "likesCount" to info.likesCount,
            "sessionsThisMonth" to info.sessionsThisMonth,
            "monthTimeSeconds" to info.monthTimeSeconds,
            "bookCollection" to info.bookCollection,
            "lastModified" to info.lastModified,
            "displayName" to info.displayName.orEmpty(),
            "username" to info.username.orEmpty(),
            "usernameLower" to info.username.orEmpty().lowercase(),
            "followeds" to info.followeds,
        )
    }

    @Suppress("UNCHECKED_CAST")
    override fun fromFirestoreMap(map: Map<String, Any?>, userId: String): UserProfileInfo {
        return UserProfileInfo(
            userId = userId,
            likesCount = (map["likesCount"] as? Number)?.toInt() ?: 0,
            sessionsThisMonth = (map["sessionsThisMonth"] as? Number)?.toInt() ?: 0,
            monthTimeSeconds = (map["monthTimeSeconds"] as? Number)?.toLong() ?: 0L,
            bookCollection = (map["bookCollection"] as? List<String>) ?: emptyList(),
            lastModified = (map["lastModified"] as? Number)?.toLong() ?: 0L,
            displayName = (map["displayName"] as? String)?.ifBlank { null },
            username = (map["username"] as? String)?.ifBlank { null },
            followeds = (map["followeds"] as? List<String>) ?: emptyList(),
        )
    }
}
