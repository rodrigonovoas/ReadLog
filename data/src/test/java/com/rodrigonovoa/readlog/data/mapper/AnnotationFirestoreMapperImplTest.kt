package com.rodrigonovoa.readlog.data.mapper

import com.rodrigonovoa.readlog.domain.model.Annotation
import org.junit.Assert.assertEquals
import org.junit.Test

class AnnotationFirestoreMapperImplTest {

    private val mapper = AnnotationFirestoreMapperImpl()

    @Test
    fun `toFirestoreMap produces correct map`() {
        val annotation = Annotation(
            annotationId = 1,
            remoteId = "uuid-a1",
            sessionId = 10,
            sessionRemoteId = "uuid-s10",
            annotation = "Note",
            creationDate = 1000L,
            lastModified = 2000L,
        )

        val map = mapper.toFirestoreMap(annotation)

        assertEquals("uuid-s10", map["sessionRemoteId"])
        assertEquals("Note", map["annotation"])
        assertEquals(1000L, map["creationDate"])
        assertEquals(2000L, map["lastModified"])
    }

    @Test
    fun `fromFirestoreMap reconstructs Annotation with defaults for missing fields`() {
        val map = mapOf(
            "sessionRemoteId" to "uuid-s20",
            "annotation" to "Another note",
        )

        val annotation = mapper.fromFirestoreMap(map, "uuid-a2")

        assertEquals(
            Annotation(
                annotationId = 0,
                remoteId = "uuid-a2",
                sessionId = 0,
                sessionRemoteId = "uuid-s20",
                annotation = "Another note",
                creationDate = 0L,
                lastModified = 0L,
            ),
            annotation
        )
    }
}
