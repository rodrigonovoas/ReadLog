package com.rodrigonovoa.readlog.domain.usecase

import com.rodrigonovoa.readlog.domain.model.UserProfileInfo
import com.rodrigonovoa.readlog.domain.repository.UserProfileRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetUserProfileInfoUseCaseTest {

    private val repository = mockk<UserProfileRepository>()
    private val useCase = GetUserProfileInfoUseCase(repository)

    @Test
    fun `invoke returns info from repository`() = runTest {
        val info = UserProfileInfo(userId = "uid", likesCount = 5)
        coEvery { repository.getUserProfileInfo("uid") } returns info

        val result = useCase("uid")

        assertEquals(info, result)
    }
}
