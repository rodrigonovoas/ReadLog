package com.rodrigonovoa.readlog.data.mapper

import com.rodrigonovoa.readlog.domain.model.UserProfileInfo

interface UserProfileInfoFirestoreMapper {
    fun toFirestoreMap(info: UserProfileInfo): Map<String, Any>
    fun fromFirestoreMap(map: Map<String, Any?>, userId: String): UserProfileInfo
}
