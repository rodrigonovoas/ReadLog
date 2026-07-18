package com.rodrigonovoa.readlog.data.mapper

import com.rodrigonovoa.readlog.data.db.entity.UserProfileInfoEntity
import com.rodrigonovoa.readlog.domain.model.UserProfileInfo

interface UserProfileInfoDataMapper {
    fun toDomain(entity: UserProfileInfoEntity): UserProfileInfo
    fun toEntity(domain: UserProfileInfo): UserProfileInfoEntity
}
