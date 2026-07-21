package com.rodrigonovoa.readlog.data.repository

import com.rodrigonovoa.readlog.data.firestore.UserSearchFirestoreDataSource
import com.rodrigonovoa.readlog.domain.model.UserSearchResult
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class UserSearchRepositoryImplTest {

    private lateinit var userSearchFirestoreDataSource: UserSearchFirestoreDataSource
    private lateinit var repository: UserSearchRepositoryImpl

    @Before
    fun setup() {
        userSearchFirestoreDataSource = mockk()
        repository = UserSearchRepositoryImpl(userSearchFirestoreDataSource)
    }

    @Test
    fun `delegates search to the firestore data source`() = runTest {
        val expected = listOf(UserSearchResult(userId = "1", username = "elenalee"))
        coEvery { userSearchFirestoreDataSource.searchByUsernamePrefix("elen", 20) } returns Result.success(expected)

        val result = repository.searchByUsername("elen", 20)

        assertEquals(expected, result.getOrThrow())
    }

    @Test
    fun `propagates failure from the firestore data source`() = runTest {
        coEvery { userSearchFirestoreDataSource.searchByUsernamePrefix("elen", 20) } returns Result.failure(RuntimeException("offline"))

        val result = repository.searchByUsername("elen", 20)

        assertEquals(true, result.isFailure)
    }
}
