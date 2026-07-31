package com.rodrigonovoa.readlog.data.mapper

import com.rodrigonovoa.readlog.data.db.entity.UserProfileInfoEntity
import com.rodrigonovoa.readlog.domain.model.UserProfileInfo
import javax.inject.Inject

class UserProfileInfoDataMapperImpl @Inject constructor() : UserProfileInfoDataMapper {
    override fun toDomain(entity: UserProfileInfoEntity): UserProfileInfo {
        return UserProfileInfo(
            userId = entity.userId,
            likesCount = entity.likesCount,
            sessionsThisMonth = entity.sessionsThisMonth,
            monthTimeSeconds = entity.monthTimeSeconds,
            bookCollection = entity.bookCollection,
            lastModified = entity.lastModified,
            displayName = entity.displayName,
            username = entity.username,
            followeds = entity.followeds,
        )
    }

    override fun toEntity(domain: UserProfileInfo): UserProfileInfoEntity {
        return UserProfileInfoEntity(
            userId = domain.userId,
            likesCount = domain.likesCount,
            sessionsThisMonth = domain.sessionsThisMonth,
            monthTimeSeconds = domain.monthTimeSeconds,
            bookCollection = domain.bookCollection,
            lastModified = domain.lastModified,
            displayName = domain.displayName,
            username = domain.username,
            followeds = domain.followeds,
        )
    }
}
