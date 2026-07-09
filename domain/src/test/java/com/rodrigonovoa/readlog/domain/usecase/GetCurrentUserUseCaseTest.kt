package com.rodrigonovoa.readlog.domain.usecase

import com.rodrigonovoa.readlog.domain.fakes.FakeAuthRepository
import com.rodrigonovoa.readlog.domain.model.User
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GetCurrentUserUseCaseTest {

    private val repository = FakeAuthRepository()
    private val useCase = GetCurrentUserUseCase(repository)

    @Test
    fun `invoke returns null when no user is signed in`() {
        repository.fakeCurrentUser = null

        val result = useCase()

        assertNull(result)
    }

    @Test
    fun `invoke returns current user when signed in`() {
        val user = User(uid = "123", email = "a@b.com", displayName = "User")
        repository.fakeCurrentUser = user

        val result = useCase()

        assertEquals(user, result)
    }
}
