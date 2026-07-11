package com.rodrigonovoa.readlog.data.mapper

import com.rodrigonovoa.readlog.data.db.entity.AnnotationEntity
import com.rodrigonovoa.readlog.domain.model.Annotation
import org.junit.Assert.assertEquals
import org.junit.Test

class AnnotationDataMapperImplTest {

    private val mapper = AnnotationDataMapperImpl()

    @Test
    fun `toDomain maps entity to domain model`() {
        val entity = AnnotationEntity(
            annotationId = 1,
            sessionId = 10,
            annotation = "Interesting plot twist",
            creationDate = 12345678L,
        )

        val result = mapper.toDomain(entity)

        assertEquals(
            Annotation(
                annotationId = 1,
                sessionId = 10,
                annotation = "Interesting plot twist",
                creationDate = 12345678L,
            ),
            result
        )
    }

    @Test
    fun `toEntity maps domain model to entity`() {
        val domain = Annotation(
            annotationId = 2,
            sessionId = 20,
            annotation = "Character development noted",
            creationDate = 87654321L,
        )

        val result = mapper.toEntity(domain)

        assertEquals(
            AnnotationEntity(
                annotationId = 2,
                sessionId = 20,
                annotation = "Character development noted",
                creationDate = 87654321L,
            ),
            result
        )
    }

    @Test
    fun `roundtrip conversion preserves data`() {
        val original = Annotation(
            annotationId = 3,
            sessionId = 30,
            annotation = "Foreshadowing on page 42",
            creationDate = 99999999L,
        )

        val entity = mapper.toEntity(original)
        val roundtrip = mapper.toDomain(entity)

        assertEquals(original, roundtrip)
    }
}
