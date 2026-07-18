package com.rodrigonovoa.readlog.data.mapper

import com.rodrigonovoa.readlog.data.db.entity.UserProfileInfoEntity
import com.rodrigonovoa.readlog.domain.model.UserProfileInfo
import javax.inject.Inject

class UserProfileInfoDataMapperImpl @Inject constructor() : UserProfileInfoDataMapper {
    override fun toDomain(entity: UserProfileInfoEntity): UserProfileInfo {
        return UserProfileInfo(
            userId = entity.userId,
            followersCount = entity.followersCount,
            likesCount = entity.likesCount,
            sessionsThisWeek = entity.sessionsThisWeek,
            weekTimeSeconds = entity.weekTimeSeconds,
            bookCollection = entity.bookCollection,
            lastModified = entity.lastModified,
            displayName = entity.displayName,
        )
    }

    override fun toEntity(domain: UserProfileInfo): UserProfileInfoEntity {
        return UserProfileInfoEntity(
            userId = domain.userId,
            followersCount = domain.followersCount,
            likesCount = domain.likesCount,
            sessionsThisWeek = domain.sessionsThisWeek,
            weekTimeSeconds = domain.weekTimeSeconds,
            bookCollection = domain.bookCollection,
            lastModified = domain.lastModified,
            displayName = domain.displayName,
        )
    }
}
