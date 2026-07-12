package com.rodrigonovoa.readlog.data.mapper

import com.rodrigonovoa.readlog.domain.model.Session
import org.junit.Assert.assertEquals
import org.junit.Test

class SessionFirestoreMapperImplTest {

    private val mapper = SessionFirestoreMapperImpl()

    @Test
    fun `toFirestoreMap produces correct map`() {
        val session = Session(
            sessionId = 1,
            remoteId = "uuid-s1",
            bookId = 10,
            bookRemoteId = "uuid-b10",
            time = 3600000L,
            creationDate = 1000L,
            lastModified = 2000L,
        )

        val map = mapper.toFirestoreMap(session)

        assertEquals("uuid-b10", map["bookRemoteId"])
        assertEquals(3600000L, map["time"])
        assertEquals(1000L, map["creationDate"])
        assertEquals(2000L, map["lastModified"])
    }

    @Test
    fun `fromFirestoreMap reconstructs Session with defaults for missing fields`() {
        val map = mapOf(
            "bookRemoteId" to "uuid-b20",
            "time" to 7200000L,
        )

        val session = mapper.fromFirestoreMap(map, "uuid-s2")

        assertEquals(
            Session(
                sessionId = 0,
                remoteId = "uuid-s2",
                bookId = 0,
                bookRemoteId = "uuid-b20",
                time = 7200000L,
                creationDate = 0L,
                lastModified = 0L,
            ),
            session
        )
    }
}
