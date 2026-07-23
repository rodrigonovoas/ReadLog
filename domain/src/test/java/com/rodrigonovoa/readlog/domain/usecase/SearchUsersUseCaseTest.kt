package com.rodrigonovoa.readlog.domain.usecase

import com.rodrigonovoa.readlog.domain.model.User
import com.rodrigonovoa.readlog.domain.model.UserSearchResult
import com.rodrigonovoa.readlog.domain.repository.UserSearchRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class SearchUsersUseCaseTest {

    private lateinit var userSearchRepository: UserSearchRepository
    private lateinit var getCurrentUserUseCase: GetCurrentUserUseCase
    private lateinit var useCase: SearchUsersUseCase

    @Before
    fun setup() {
        userSearchRepository = mockk()
        getCurrentUserUseCase = mockk()
        every { getCurrentUserUseCase() } returns null
        useCase = SearchUsersUseCase(userSearchRepository, getCurrentUserUseCase)
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

    @Test
    fun `filters out the current signed-in user's own result`() = runTest {
        every { getCurrentUserUseCase() } returns User(uid = "1", email = null, displayName = null)
        val results = listOf(
            UserSearchResult(userId = "1", username = "elenalee"),
            UserSearchResult(userId = "2", username = "elenaperez"),
        )
        coEvery { userSearchRepository.searchByUsername("elen", 20) } returns Result.success(results)

        val result = useCase("elen")

        assertEquals(listOf(UserSearchResult(userId = "2", username = "elenaperez")), result.getOrThrow())
    }
}
