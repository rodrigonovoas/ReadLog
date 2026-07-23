package com.rodrigonovoa.readlog.domain.usecase

import com.rodrigonovoa.readlog.domain.model.UserProfileInfo
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class RequireUsernameSetupUseCaseTest {

    private lateinit var getRemoteUserProfileInfoUseCase: GetRemoteUserProfileInfoUseCase
    private lateinit var suggestAvailableUsernameUseCase: SuggestAvailableUsernameUseCase
    private lateinit var useCase: RequireUsernameSetupUseCase

    @Before
    fun setup() {
        getRemoteUserProfileInfoUseCase = mockk()
        suggestAvailableUsernameUseCase = mockk()
        useCase = RequireUsernameSetupUseCase(getRemoteUserProfileInfoUseCase, suggestAvailableUsernameUseCase)
    }

    @Test
    fun `returns null when the remote profile already has a username`() = runTest {
        coEvery { getRemoteUserProfileInfoUseCase("uid1") } returns Result.success(
            UserProfileInfo(userId = "uid1", username = "already_set")
        )

        val result = useCase("uid1", "elena@example.com", "Elena Marin")

        assertNull(result)
        coVerify(exactly = 0) { suggestAvailableUsernameUseCase(any(), any(), any()) }
    }

    @Test
    fun `returns a suggestion when the remote profile has no username`() = runTest {
        coEvery { getRemoteUserProfileInfoUseCase("uid1") } returns Result.success(
            UserProfileInfo(userId = "uid1", username = null)
        )
        coEvery { suggestAvailableUsernameUseCase("elena@example.com", "Elena Marin", "uid1") } returns "elena"

        val result = useCase("uid1", "elena@example.com", "Elena Marin")

        assertEquals("elena", result)
    }

    @Test
    fun `returns a suggestion when the remote profile lookup fails`() = runTest {
        coEvery { getRemoteUserProfileInfoUseCase("uid1") } returns Result.failure(RuntimeException("offline"))
        coEvery { suggestAvailableUsernameUseCase("elena@example.com", "Elena Marin", "uid1") } returns "elena"

        val result = useCase("uid1", "elena@example.com", "Elena Marin")

        assertEquals("elena", result)
    }
}
