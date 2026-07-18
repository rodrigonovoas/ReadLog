package com.rodrigonovoa.readlog.domain.usecase

import com.rodrigonovoa.readlog.domain.model.UserProfileInfo
import com.rodrigonovoa.readlog.domain.repository.UserProfileRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class RefreshUserProfileInfoUseCaseTest {

    private val repository = mockk<UserProfileRepository>()
    private val useCase = RefreshUserProfileInfoUseCase(repository)

    @Test
    fun `invoke passes the start of the current week to the repository`() = runTest {
        val startOfWeekSlot = slot<Long>()
        val info = UserProfileInfo(userId = "uid")
        coEvery {
            repository.refreshUserProfileInfo("uid", capture(startOfWeekSlot), "Elena Marín")
        } returns Result.success(info)

        val result = useCase("uid", "Elena Marín")

        assertEquals(info, result.getOrNull())
        coVerify { repository.refreshUserProfileInfo("uid", any(), "Elena Marín") }

        val calendar = Calendar.getInstance().apply { timeInMillis = startOfWeekSlot.captured }
        assertEquals(Calendar.MONDAY, calendar.get(Calendar.DAY_OF_WEEK))
        assertEquals(0, calendar.get(Calendar.HOUR_OF_DAY))
        assertEquals(0, calendar.get(Calendar.MINUTE))
        assertTrue(startOfWeekSlot.captured <= System.currentTimeMillis())
    }

    @Test
    fun `invoke returns failure from repository`() = runTest {
        coEvery { repository.refreshUserProfileInfo("uid", any(), any()) } returns
            Result.failure(RuntimeException("network error"))

        val result = useCase("uid", null)

        assertEquals(true, result.isFailure)
        assertEquals("network error", result.exceptionOrNull()?.message)
    }
}
