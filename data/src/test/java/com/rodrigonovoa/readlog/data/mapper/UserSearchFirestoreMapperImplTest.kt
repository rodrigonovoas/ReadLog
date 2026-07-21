package com.rodrigonovoa.readlog.data.mapper

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class UserSearchFirestoreMapperImplTest {

    private val mapper = UserSearchFirestoreMapperImpl()

    private fun mockSnapshot(userId: String?, username: String?): DocumentSnapshot {
        val userDocumentReference = mockk<DocumentReference> {
            every { id } returns (userId ?: "")
        }
        val profileDocumentReference = mockk<DocumentReference> {
            every { parent.parent } returns if (userId != null) userDocumentReference else null
        }
        return mockk {
            every { reference } returns profileDocumentReference
            every { getString("username") } returns username
        }
    }

    @Test
    fun `toDomain maps snapshot to UserSearchResult using parent document id as userId`() {
        val snapshot = mockSnapshot(userId = "uid-1", username = "elenalee")

        val result = mapper.toDomain(snapshot)

        assertEquals("uid-1", result?.userId)
        assertEquals("elenalee", result?.username)
    }

    @Test
    fun `toDomain returns null when username is missing`() {
        val snapshot = mockSnapshot(userId = "uid-1", username = null)

        val result = mapper.toDomain(snapshot)

        assertNull(result)
    }

    @Test
    fun `toDomain returns null when username is blank`() {
        val snapshot = mockSnapshot(userId = "uid-1", username = "   ")

        val result = mapper.toDomain(snapshot)

        assertNull(result)
    }

    @Test
    fun `toDomain returns null when parent user document is missing`() {
        val snapshot = mockSnapshot(userId = null, username = "elenalee")

        val result = mapper.toDomain(snapshot)

        assertNull(result)
    }
}
