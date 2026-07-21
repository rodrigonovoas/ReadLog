package com.rodrigonovoa.readlog.domain.usecase

import com.rodrigonovoa.readlog.domain.model.UserSearchResult
import com.rodrigonovoa.readlog.domain.repository.UserSearchRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class SearchUsersUseCaseTest {

    private lateinit var userSearchRepository: UserSearchRepository
    private lateinit var useCase: SearchUsersUseCase

    @Before
    fun setup() {
        userSearchRepository = mockk()
        useCase = SearchUsersUseCase(userSearchRepository)
    }

    @Test
    fun `returns empty list without querying repository when query is blank`() = runTest {
        val result = useCase("   ")

        assertEquals(true, result.isSuccess)
        assertEquals(emptyList<UserSearchResult>(), result.getOrThrow())
        coVerify(exactly = 0) { userSearchRepository.searchByUsername(any(), any()) }
    }

    @Test
    fun `normalizes query before delegating to repository`() = runTest {
        val expected = listOf(UserSearchResult(userId = "1", username = "elenalee"))
        coEvery { userSearchRepository.searchByUsername("elen", 20) } returns Result.success(expected)

        val result = useCase("  Elen  ")

        assertEquals(expected, result.getOrThrow())
        coVerify { userSearchRepository.searchByUsername("elen", 20) }
    }
}
