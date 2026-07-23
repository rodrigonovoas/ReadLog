package com.rodrigonovoa.readlog.domain.usecase

import com.rodrigonovoa.readlog.domain.model.UserProfileInfo
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ClaimUsernameUseCaseTest {

    private lateinit var isUsernameAvailableUseCase: IsUsernameAvailableUseCase
    private lateinit var setUsernameUseCase: SetUsernameUseCase
    private lateinit var useCase: ClaimUsernameUseCase

    @Before
    fun setup() {
        isUsernameAvailableUseCase = mockk()
        setUsernameUseCase = mockk()
        useCase = ClaimUsernameUseCase(isUsernameAvailableUseCase, setUsernameUseCase)
    }

    @Test
    fun `returns InvalidFormat for a username that does not match the allowed pattern`() = runTest {
        val result = useCase("uid1", "a")

        assertEquals(ClaimUsernameResult.InvalidFormat, result)
        coVerify(exactly = 0) { isUsernameAvailableUseCase(any()) }
    }

    @Test
    fun `returns AlreadyTaken when the username is not available`() = runTest {
        coEvery { isUsernameAvailableUseCase("taken_name") } returns Result.success(false)

        val result = useCase("uid1", "taken_name")

        assertEquals(ClaimUsernameResult.AlreadyTaken, result)
        coVerify(exactly = 0) { setUsernameUseCase(any(), any()) }
    }

    @Test
    fun `returns Success and persists the username when it is available`() = runTest {
        val savedProfile = UserProfileInfo(userId = "uid1", username = "free_name")
        coEvery { isUsernameAvailableUseCase("free_name") } returns Result.success(true)
        coEvery { setUsernameUseCase("uid1", "free_name") } returns Result.success(savedProfile)

        val result = useCase("uid1", " free_name ")

        assertEquals(ClaimUsernameResult.Success(savedProfile), result)
    }

    @Test
    fun `returns Error when the availability check fails`() = runTest {
        coEvery { isUsernameAvailableUseCase("free_name") } returns Result.failure(RuntimeException("offline"))

        val result = useCase("uid1", "free_name")

        assertTrue(result is ClaimUsernameResult.Error)
    }

    @Test
    fun `returns Error when saving the username fails`() = runTest {
        coEvery { isUsernameAvailableUseCase("free_name") } returns Result.success(true)
        coEvery { setUsernameUseCase("uid1", "free_name") } returns Result.failure(RuntimeException("db error"))

        val result = useCase("uid1", "free_name")

        assertTrue(result is ClaimUsernameResult.Error)
    }
}
