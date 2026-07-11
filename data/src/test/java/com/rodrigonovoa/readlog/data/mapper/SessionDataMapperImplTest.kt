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
            bookId = 10,
            time = 3600000L,
            creationDate = 12345678L,
        )

        val result = mapper.toDomain(entity)

        assertEquals(
            Session(
                sessionId = 1,
                bookId = 10,
                time = 3600000L,
                creationDate = 12345678L,
            ),
            result
        )
    }

    @Test
    fun `toEntity maps domain model to entity`() {
        val domain = Session(
            sessionId = 2,
            bookId = 20,
            time = 7200000L,
            creationDate = 87654321L,
        )

        val result = mapper.toEntity(domain)

        assertEquals(
            SessionEntity(
                sessionId = 2,
                bookId = 20,
                time = 7200000L,
                creationDate = 87654321L,
            ),
            result
        )
    }

    @Test
    fun `roundtrip conversion preserves data`() {
        val original = Session(
            sessionId = 3,
            bookId = 30,
            time = 1800000L,
            creationDate = 99999999L,
        )

        val entity = mapper.toEntity(original)
        val roundtrip = mapper.toDomain(entity)

        assertEquals(original, roundtrip)
    }
}
