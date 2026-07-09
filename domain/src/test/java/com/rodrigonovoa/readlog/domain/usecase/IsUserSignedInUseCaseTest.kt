package com.rodrigonovoa.readlog.domain.usecase

import com.rodrigonovoa.readlog.domain.fakes.FakeAuthRepository
import com.rodrigonovoa.readlog.domain.model.User
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class IsUserSignedInUseCaseTest {

    private val repository = FakeAuthRepository()
    private val useCase = IsUserSignedInUseCase(repository)

    @Test
    fun `invoke returns false when no user is signed in`() {
        repository.fakeCurrentUser = null

        val result = useCase()

        assertFalse(result)
    }

    @Test
    fun `invoke returns true when user is signed in`() {
        repository.fakeCurrentUser = User(uid = "123", email = "a@b.com", displayName = "User")

        val result = useCase()

        assertTrue(result)
    }
}
