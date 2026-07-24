package com.rodrigonovoa.readlog.data.mapper

import com.google.firebase.auth.FirebaseUser
import com.rodrigonovoa.readlog.domain.model.User
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class UserDataMapperImplTest {

    private val mapper = UserDataMapperImpl()

    @Test
    fun `toDomain maps all fields correctly`() {
        val firebaseUser = mockk<FirebaseUser>()
        every { firebaseUser.uid } returns "uid123"
        every { firebaseUser.email } returns "test@example.com"
        every { firebaseUser.displayName } returns "Test User"
        every { firebaseUser.isAnonymous } returns false

        val result = mapper.toDomain(firebaseUser)

        assertEquals(User("uid123", "test@example.com", "Test User", isAnonymous = false), result)
    }

    @Test
    fun `toDomain handles null email and displayName`() {
        val firebaseUser = mockk<FirebaseUser>()
        every { firebaseUser.uid } returns "uid456"
        every { firebaseUser.email } returns null
        every { firebaseUser.displayName } returns null
        every { firebaseUser.isAnonymous } returns false

        val result = mapper.toDomain(firebaseUser)

        assertEquals("uid456", result.uid)
        assertNull(result.email)
        assertNull(result.displayName)
    }

    @Test
    fun `toDomain maps isAnonymous true for anonymous users`() {
        val firebaseUser = mockk<FirebaseUser>()
        every { firebaseUser.uid } returns "uid789"
        every { firebaseUser.email } returns null
        every { firebaseUser.displayName } returns null
        every { firebaseUser.isAnonymous } returns true

        val result = mapper.toDomain(firebaseUser)

        assertEquals(true, result.isAnonymous)
    }
}
