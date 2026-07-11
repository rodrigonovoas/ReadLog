package com.rodrigonovoa.readlog.domain.usecase

import com.rodrigonovoa.readlog.domain.repository.AuthRepository
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class IsUserSignedInUseCaseTest {

    private val repository = mockk<AuthRepository>()
    private val useCase = IsUserSignedInUseCase(repository)

    @Test
    fun `invoke returns false when no user is signed in`() {
        every { repository.isUserSignedIn() } returns false

        val result = useCase()

        assertFalse(result)
    }

    @Test
    fun `invoke returns true when user is signed in`() {
        every { repository.isUserSignedIn() } returns true

        val result = useCase()

        assertTrue(result)
    }
}
