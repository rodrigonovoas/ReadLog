package com.rodrigonovoa.readlog.data.mapper

import com.rodrigonovoa.readlog.data.db.entity.UserProfileInfoEntity
import com.rodrigonovoa.readlog.domain.model.UserProfileInfo
import org.junit.Assert.assertEquals
import org.junit.Test

class UserProfileInfoDataMapperImplTest {

    private val mapper = UserProfileInfoDataMapperImpl()

    @Test
    fun `toDomain maps entity to domain model`() {
        val entity = UserProfileInfoEntity(
            userId = "uid-1",
            followersCount = 5,
            likesCount = 10,
            sessionsThisWeek = 3,
            weekTimeSeconds = 3600L,
            bookCollection = listOf("Book A", "Book B"),
            lastModified = 11111111L,
            displayName = "Elena Marín",
            username = "elena_marin",
        )

        val result = mapper.toDomain(entity)

        assertEquals(
            UserProfileInfo(
                userId = "uid-1",
                followersCount = 5,
                likesCount = 10,
                sessionsThisWeek = 3,
                weekTimeSeconds = 3600L,
                bookCollection = listOf("Book A", "Book B"),
                lastModified = 11111111L,
                displayName = "Elena Marín",
                username = "elena_marin",
            ),
            result
        )
    }

    @Test
    fun `toEntity maps domain model to entity`() {
        val domain = UserProfileInfo(
            userId = "uid-2",
            followersCount = 8,
            likesCount = 20,
            sessionsThisWeek = 4,
            weekTimeSeconds = 7200L,
            bookCollection = listOf("Book C"),
            lastModified = 22222222L,
            displayName = "Diego Pérez",
            username = "diego_perez",
        )

        val result = mapper.toEntity(domain)

        assertEquals(
            UserProfileInfoEntity(
                userId = "uid-2",
                followersCount = 8,
                likesCount = 20,
                sessionsThisWeek = 4,
                weekTimeSeconds = 7200L,
                bookCollection = listOf("Book C"),
                lastModified = 22222222L,
                displayName = "Diego Pérez",
                username = "diego_perez",
            ),
            result
        )
    }

    @Test
    fun `roundtrip conversion preserves data`() {
        val original = UserProfileInfo(
            userId = "uid-3",
            followersCount = 1,
            likesCount = 2,
            sessionsThisWeek = 3,
            weekTimeSeconds = 1800L,
            bookCollection = listOf("Book D", "Book E", "Book F"),
            lastModified = 33333333L,
            displayName = "Elena Marín",
            username = "elena_marin",
        )

        val entity = mapper.toEntity(original)
        val roundtrip = mapper.toDomain(entity)

        assertEquals(original, roundtrip)
    }
}
