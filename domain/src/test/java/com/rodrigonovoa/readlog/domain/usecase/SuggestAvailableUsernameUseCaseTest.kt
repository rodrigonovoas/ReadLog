package com.rodrigonovoa.readlog.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SuggestAvailableUsernameUseCaseTest {

    private lateinit var isUsernameAvailableUseCase: IsUsernameAvailableUseCase
    private lateinit var useCase: SuggestAvailableUsernameUseCase

    @Before
    fun setup() {
        isUsernameAvailableUseCase = mockk()
        useCase = SuggestAvailableUsernameUseCase(GenerateUsernameUseCase(), isUsernameAvailableUseCase)
    }

    @Test
    fun `returns the email based slug when it is available`() = runTest {
        coEvery { isUsernameAvailableUseCase(any()) } returns Result.success(true)

        val result = useCase("elena_marin@example.com", "Elena Marin", "uid123456")

        assertEquals("elena_marin", result)
    }

    @Test
    fun `prefers the email local part over the display name`() = runTest {
        coEvery { isUsernameAvailableUseCase(any()) } returns Result.success(true)

        val result = useCase("readingfan@example.com", "Elena Marin", "uid123456")

        assertEquals("readingfan", result)
    }

    @Test
    fun `falls back to the display name when there is no email`() = runTest {
        coEvery { isUsernameAvailableUseCase(any()) } returns Result.success(true)

        val result = useCase(null, "Elena Marin", "uid123456")

        assertEquals("elena_marin", result)
    }

    @Test
    fun `retries with a random numeric suffix when the base slug is taken`() = runTest {
        coEvery { isUsernameAvailableUseCase("elena_marin") } returns Result.success(false)
        coEvery { isUsernameAvailableUseCase(match { it.startsWith("elena_marin") && it != "elena_marin" }) } returns Result.success(true)

        val result = useCase("elena_marin@example.com", null, "uid123456")

        assertTrue(result.matches(Regex("^elena_marin\\d+$")))
        coVerify(atLeast = 2) { isUsernameAvailableUseCase(any()) }
    }

    @Test
    fun `falls back to the userId based username when all suffixed attempts are taken`() = runTest {
        coEvery { isUsernameAvailableUseCase(any()) } returns Result.success(false)

        val result = useCase("elena_marin@example.com", null, "uid123456")

        assertEquals("useruid123", result)
    }

    @Test
    fun `treats an availability check failure as available`() = runTest {
        coEvery { isUsernameAvailableUseCase(any()) } returns Result.failure(RuntimeException("offline"))

        val result = useCase("elena_marin@example.com", null, "uid123456")

        assertEquals("elena_marin", result)
    }
}
