package com.rodrigonovoa.readlog.data.mapper

import com.rodrigonovoa.readlog.data.db.entity.SessionEntity
import com.rodrigonovoa.readlog.domain.model.Session
import org.junit.Assert.assertEquals
import org.junit.Test

class SessionDataMapperImplTest {

    private val mapper = SessionDataMapperImpl()

    @Test
    fun `toDomain maps entity to domain model`() {
        val entity = SessionEntity(
            sessionId = 1,
            remoteId = "session-uuid-1",
            bookId = 10,
            bookRemoteId = "book-uuid-10",
            time = 3600000L,
            creationDate = 12345678L,
            lastModified = 11111111L,
        )

        val result = mapper.toDomain(entity)

        assertEquals(
            Session(
                sessionId = 1,
                remoteId = "session-uuid-1",
                bookId = 10,
                bookRemoteId = "book-uuid-10",
                time = 3600000L,
                creationDate = 12345678L,
                lastModified = 11111111L,
            ),
            result
        )
    }

    @Test
    fun `toEntity maps domain model to entity`() {
        val domain = Session(
            sessionId = 2,
            remoteId = "session-uuid-2",
            bookId = 20,
            bookRemoteId = "book-uuid-20",
            time = 7200000L,
            creationDate = 87654321L,
            lastModified = 22222222L,
        )

        val result = mapper.toEntity(domain)

        assertEquals(
            SessionEntity(
                sessionId = 2,
                remoteId = "session-uuid-2",
                bookId = 20,
                bookRemoteId = "book-uuid-20",
                time = 7200000L,
                creationDate = 87654321L,
                lastModified = 22222222L,
            ),
            result
        )
    }

    @Test
    fun `roundtrip conversion preserves data`() {
        val original = Session(
            sessionId = 3,
            remoteId = "session-uuid-3",
            bookId = 30,
            bookRemoteId = "book-uuid-30",
            time = 1800000L,
            creationDate = 99999999L,
            lastModified = 33333333L,
        )

        val entity = mapper.toEntity(original)
        val roundtrip = mapper.toDomain(entity)

        assertEquals(original, roundtrip)
    }
}
