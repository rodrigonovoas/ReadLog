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
            likesCount = 10,
            sessionsThisWeek = 3,
            weekTimeSeconds = 3600L,
            bookCollection = listOf("Book A", "Book B"),
            lastModified = 2000L,
            displayName = "Elena Marín",
            username = "Elena_Marin",
            followeds = listOf("uid-2", "uid-3"),
        )

        val map = mapper.toFirestoreMap(stats)

        assertEquals(10, map["likesCount"])
        assertEquals(3, map["sessionsThisWeek"])
        assertEquals(3600L, map["weekTimeSeconds"])
        assertEquals(listOf("Book A", "Book B"), map["bookCollection"])
        assertEquals(2000L, map["lastModified"])
        assertEquals("Elena Marín", map["displayName"])
        assertEquals("Elena_Marin", map["username"])
        assertEquals("elena_marin", map["usernameLower"])
        assertEquals(listOf("uid-2", "uid-3"), map["followeds"])
    }

    @Test
    fun `toFirestoreMap defaults displayName and username to empty string when null`() {
        val stats = UserProfileInfo(userId = "uid-1")

        val map = mapper.toFirestoreMap(stats)

        assertEquals("", map["displayName"])
        assertEquals("", map["username"])
        assertEquals("", map["usernameLower"])
        assertEquals(emptyList<String>(), map["followeds"])
    }

    @Test
    fun `fromFirestoreMap reconstructs UserProfileInfo with defaults for missing fields`() {
        val map = mapOf(
            "likesCount" to 10,
        )

        val stats = mapper.fromFirestoreMap(map, "uid-2")

        assertEquals(
            UserProfileInfo(
                userId = "uid-2",
                likesCount = 10,
                sessionsThisWeek = 0,
                weekTimeSeconds = 0L,
                bookCollection = emptyList(),
                lastModified = 0L,
                displayName = null,
                username = null,
                followeds = emptyList(),
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

    @Test
    fun `fromFirestoreMap reconstructs followeds when present`() {
        val map = mapOf("followeds" to listOf("uid-7", "uid-8"))

        val stats = mapper.fromFirestoreMap(map, "uid-2")

        assertEquals(listOf("uid-7", "uid-8"), stats.followeds)
    }
}
