package com.rodrigonovoa.readlog.domain.usecase

import com.rodrigonovoa.readlog.domain.model.User
import com.rodrigonovoa.readlog.domain.repository.AuthRepository
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Test

class GetUserDisplayNameUseCaseTest {

    private val authRepository = mockk<AuthRepository>()
    private val useCase = GetUserDisplayNameUseCase(authRepository)

    @Test
    fun `invoke returns only the first token of a multi-word display name`() {
        every { authRepository.getCurrentUser() } returns User("uid", "test@test.com", "Rodrigo Novoa Salgado")

        assertEquals("Rodrigo", useCase())
    }

    @Test
    fun `invoke returns the display name unchanged when it is a single word`() {
        every { authRepository.getCurrentUser() } returns User("uid", "test@test.com", "Elena")

        assertEquals("Elena", useCase())
    }

    @Test
    fun `invoke falls back to reader when display name is blank`() {
        every { authRepository.getCurrentUser() } returns User("uid", "test@test.com", "   ")

        assertEquals("reader", useCase())
    }

    @Test
    fun `invoke falls back to reader when display name is null`() {
        every { authRepository.getCurrentUser() } returns User("uid", "test@test.com", null)

        assertEquals("reader", useCase())
    }

    @Test
    fun `invoke falls back to reader when no user is signed in`() {
        every { authRepository.getCurrentUser() } returns null

        assertEquals("reader", useCase())
    }
}
