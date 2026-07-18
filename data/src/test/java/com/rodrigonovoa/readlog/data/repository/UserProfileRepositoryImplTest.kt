package com.rodrigonovoa.readlog.data.repository

import com.rodrigonovoa.readlog.data.db.dao.UserProfileInfoDao
import com.rodrigonovoa.readlog.data.db.entity.UserProfileInfoEntity
import com.rodrigonovoa.readlog.data.firestore.UserProfileInfoFirestoreDataSource
import com.rodrigonovoa.readlog.data.mapper.UserProfileInfoDataMapperImpl
import com.rodrigonovoa.readlog.domain.model.Book
import com.rodrigonovoa.readlog.domain.model.Session
import com.rodrigonovoa.readlog.domain.model.UserProfileInfo
import com.rodrigonovoa.readlog.domain.repository.BookRepository
import com.rodrigonovoa.readlog.domain.repository.SessionRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class UserProfileRepositoryImplTest {

    private lateinit var userProfileInfoDao: UserProfileInfoDao
    private lateinit var bookRepository: BookRepository
    private lateinit var sessionRepository: SessionRepository
    private lateinit var userProfileInfoFirestoreDataSource: UserProfileInfoFirestoreDataSource
    private lateinit var repository: UserProfileRepositoryImpl

    @Before
    fun setup() {
        userProfileInfoDao = mockk(relaxed = true)
        bookRepository = mockk()
        sessionRepository = mockk()
        userProfileInfoFirestoreDataSource = mockk()
        repository = UserProfileRepositoryImpl(
            userProfileInfoDao = userProfileInfoDao,
            bookRepository = bookRepository,
            sessionRepository = sessionRepository,
            userProfileInfoDataMapper = UserProfileInfoDataMapperImpl(),
            userProfileInfoFirestoreDataSource = userProfileInfoFirestoreDataSource,
        )
    }

    @Test
    fun `getUserProfileInfo returns default info when no local cache exists`() = runTest {
        coEvery { userProfileInfoDao.getByUserId("uid") } returns null

        val result = repository.getUserProfileInfo("uid")

        assertEquals(UserProfileInfo(userId = "uid"), result)
    }

    @Test
    fun `getUserProfileInfo returns mapped cached info`() = runTest {
        coEvery { userProfileInfoDao.getByUserId("uid") } returns UserProfileInfoEntity(
            userId = "uid",
            followersCount = 3,
            likesCount = 7,
            sessionsThisWeek = 2,
            weekTimeSeconds = 600L,
            bookCollection = listOf("Book A"),
            lastModified = 1000L,
        )

        val result = repository.getUserProfileInfo("uid")

        assertEquals(3, result.followersCount)
        assertEquals(7, result.likesCount)
        assertEquals(2, result.sessionsThisWeek)
        assertEquals(600L, result.weekTimeSeconds)
        assertEquals(listOf("Book A"), result.bookCollection)
    }

    @Test
    fun `refreshUserProfileInfo recomputes weekly stats and collection, keeps remote counters`() = runTest {
        val weekSessions = listOf(
            Session(sessionId = 1, bookId = 1, time = 100L),
            Session(sessionId = 2, bookId = 1, time = 200L),
        )
        val books = listOf(
            Book(bookId = 1, title = "Book A", author = "", genre = "", releaseDate = "", numPages = 0, currentPage = 0),
            Book(bookId = 2, title = "Book B", author = "", genre = "", releaseDate = "", numPages = 0, currentPage = 0),
        )
        coEvery { sessionRepository.getAllSessionsSince(500L) } returns weekSessions
        coEvery { bookRepository.getAllBooksList() } returns books
        coEvery { userProfileInfoFirestoreDataSource.download("uid") } returns Result.success(
            UserProfileInfo(userId = "uid", followersCount = 4, likesCount = 9)
        )
        coEvery { userProfileInfoFirestoreDataSource.upload("uid", any()) } returns Result.success(Unit)

        val result = repository.refreshUserProfileInfo("uid", 500L, "Elena Marín")

        assertEquals(true, result.isSuccess)
        val info = result.getOrThrow()
        assertEquals(4, info.followersCount)
        assertEquals(9, info.likesCount)
        assertEquals(2, info.sessionsThisWeek)
        assertEquals(300L, info.weekTimeSeconds)
        assertEquals(listOf("Book A", "Book B"), info.bookCollection)
        assertEquals("Elena Marín", info.displayName)
        coVerify { userProfileInfoDao.upsert(any()) }
        coVerify { userProfileInfoFirestoreDataSource.upload("uid", any()) }
    }

    @Test
    fun `refreshUserProfileInfo falls back to the remote displayName when none is provided`() = runTest {
        coEvery { sessionRepository.getAllSessionsSince(500L) } returns emptyList()
        coEvery { bookRepository.getAllBooksList() } returns emptyList()
        coEvery { userProfileInfoFirestoreDataSource.download("uid") } returns Result.success(
            UserProfileInfo(userId = "uid", displayName = "Cached Name")
        )
        coEvery { userProfileInfoFirestoreDataSource.upload("uid", any()) } returns Result.success(Unit)

        val result = repository.refreshUserProfileInfo("uid", 500L, null)

        assertEquals("Cached Name", result.getOrThrow().displayName)
    }

    @Test
    fun `refreshUserProfileInfo defaults counters to zero when there is no remote profile yet`() = runTest {
        coEvery { sessionRepository.getAllSessionsSince(500L) } returns emptyList()
        coEvery { bookRepository.getAllBooksList() } returns emptyList()
        coEvery { userProfileInfoFirestoreDataSource.download("uid") } returns Result.success(null)
        coEvery { userProfileInfoFirestoreDataSource.upload("uid", any()) } returns Result.success(Unit)

        val result = repository.refreshUserProfileInfo("uid", 500L, "Elena Marín")

        assertEquals(true, result.isSuccess)
        val info = result.getOrThrow()
        assertEquals(0, info.followersCount)
        assertEquals(0, info.likesCount)
    }

    @Test
    fun `refreshUserProfileInfo returns failure when session lookup throws`() = runTest {
        coEvery { sessionRepository.getAllSessionsSince(500L) } throws RuntimeException("db error")

        val result = repository.refreshUserProfileInfo("uid", 500L, "Elena Marín")

        assertEquals(true, result.isFailure)
        assertEquals("db error", result.exceptionOrNull()?.message)
    }
}
