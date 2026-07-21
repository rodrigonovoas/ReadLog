package com.rodrigonovoa.readlog.data.mapper

import com.rodrigonovoa.readlog.domain.model.UserProfileInfo
import org.junit.Assert.assertEquals
import org.junit.Test

class UserProfileInfoFirestoreMapperImplTest {

    private val mapper = UserProfileInfoFirestoreMapperImpl()

    @Test
    fun `toFirestoreMap produces correct map`() {
        val stats = UserProfileInfo(
            userId = "uid-1",
            followersCount = 5,
            likesCount = 10,
            sessionsThisWeek = 3,
            weekTimeSeconds = 3600L,
            bookCollection = listOf("Book A", "Book B"),
            lastModified = 2000L,
            displayName = "Elena Marín",
            username = "Elena_Marin",
        )

        val map = mapper.toFirestoreMap(stats)

        assertEquals(5, map["followersCount"])
        assertEquals(10, map["likesCount"])
        assertEquals(3, map["sessionsThisWeek"])
        assertEquals(3600L, map["weekTimeSeconds"])
        assertEquals(listOf("Book A", "Book B"), map["bookCollection"])
        assertEquals(2000L, map["lastModified"])
        assertEquals("Elena Marín", map["displayName"])
        assertEquals("Elena_Marin", map["username"])
        assertEquals("elena_marin", map["usernameLower"])
    }

    @Test
    fun `toFirestoreMap defaults displayName and username to empty string when null`() {
        val stats = UserProfileInfo(userId = "uid-1")

        val map = mapper.toFirestoreMap(stats)

        assertEquals("", map["displayName"])
        assertEquals("", map["username"])
        assertEquals("", map["usernameLower"])
    }

    @Test
    fun `fromFirestoreMap reconstructs UserProfileInfo with defaults for missing fields`() {
        val map = mapOf(
            "followersCount" to 5,
            "likesCount" to 10,
        )

        val stats = mapper.fromFirestoreMap(map, "uid-2")

        assertEquals(
            UserProfileInfo(
                userId = "uid-2",
                followersCount = 5,
                likesCount = 10,
                sessionsThisWeek = 0,
                weekTimeSeconds = 0L,
                bookCollection = emptyList(),
                lastModified = 0L,
                displayName = null,
                username = null,
            ),
            stats
        )
    }

    @Test
    fun `fromFirestoreMap reconstructs displayName and username when present`() {
        val map = mapOf("displayName" to "Elena Marín", "username" to "elena_marin")

        val stats = mapper.fromFirestoreMap(map, "uid-2")

        assertEquals("Elena Marín", stats.displayName)
        assertEquals("elena_marin", stats.username)
    }
}
