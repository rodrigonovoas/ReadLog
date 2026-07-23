package com.rodrigonovoa.readlog.domain.usecase

import com.rodrigonovoa.readlog.domain.model.UserProfileInfo
import com.rodrigonovoa.readlog.domain.repository.UserProfileRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetRemoteUserProfileInfoUseCaseTest {

    private val repository = mockk<UserProfileRepository>()
    private val useCase = GetRemoteUserProfileInfoUseCase(repository)

    @Test
    fun `invoke returns info from repository`() = runTest {
        val info = UserProfileInfo(userId = "uid", likesCount = 5)
        coEvery { repository.getRemoteUserProfileInfo("uid") } returns Result.success(info)

        val result = useCase("uid")

        assertEquals(Result.success(info), result)
    }

    @Test
    fun `invoke returns failure from repository`() = runTest {
        val exception = RuntimeException("network error")
        coEvery { repository.getRemoteUserProfileInfo("uid") } returns Result.failure(exception)

        val result = useCase("uid")

        assertEquals(true, result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }
}
