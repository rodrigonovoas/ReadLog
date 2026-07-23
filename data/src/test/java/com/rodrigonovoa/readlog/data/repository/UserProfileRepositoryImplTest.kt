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
            likesCount = 7,
            sessionsThisWeek = 2,
            weekTimeSeconds = 600L,
            bookCollection = listOf("Book A"),
            lastModified = 1000L,
        )

        val result = repository.getUserProfileInfo("uid")

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
            UserProfileInfo(userId = "uid", likesCount = 9, followeds = listOf("uid-2"))
        )
        coEvery { userProfileInfoFirestoreDataSource.upload("uid", any()) } returns Result.success(Unit)

        val result = repository.refreshUserProfileInfo("uid", 500L, "Elena Marín")

        assertEquals(true, result.isSuccess)
        val info = result.getOrThrow()
        assertEquals(9, info.likesCount)
        assertEquals(2, info.sessionsThisWeek)
        assertEquals(300L, info.weekTimeSeconds)
        assertEquals(listOf("Book A", "Book B"), info.bookCollection)
        assertEquals("Elena Marín", info.displayName)
        assertEquals(null, info.username)
        assertEquals(listOf("uid-2"), info.followeds)
        coVerify { userProfileInfoDao.upsert(any()) }
        coVerify { userProfileInfoFirestoreDataSource.upload("uid", any()) }
    }

    @Test
    fun `refreshUserProfileInfo keeps the existing remote username instead of regenerating it`() = runTest {
        coEvery { sessionRepository.getAllSessionsSince(500L) } returns emptyList()
        coEvery { bookRepository.getAllBooksList() } returns emptyList()
        coEvery { userProfileInfoFirestoreDataSource.download("uid") } returns Result.success(
            UserProfileInfo(userId = "uid", username = "already_set")
        )
        coEvery { userProfileInfoFirestoreDataSource.upload("uid", any()) } returns Result.success(Unit)

        val result = repository.refreshUserProfileInfo("uid", 500L, "Different Name")

        assertEquals("already_set", result.getOrThrow().username)
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
        assertEquals(0, info.likesCount)
    }

    @Test
    fun `refreshUserProfileInfo returns failure when session lookup throws`() = runTest {
        coEvery { sessionRepository.getAllSessionsSince(500L) } throws RuntimeException("db error")

        val result = repository.refreshUserProfileInfo("uid", 500L, "Elena Marín")

        assertEquals(true, result.isFailure)
        assertEquals("db error", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getRemoteUserProfileInfo caches and returns the downloaded profile`() = runTest {
        val remoteInfo = UserProfileInfo(
            userId = "uid",
            likesCount = 34,
            displayName = "Elena Marín",
            username = "elena_marin",
        )
        coEvery { userProfileInfoFirestoreDataSource.download("uid") } returns Result.success(remoteInfo)

        val result = repository.getRemoteUserProfileInfo("uid")

        assertEquals(true, result.isSuccess)
        assertEquals(remoteInfo, result.getOrThrow())
        coVerify { userProfileInfoDao.upsert(any()) }
    }

    @Test
    fun `getRemoteUserProfileInfo returns default info when no remote profile exists`() = runTest {
        coEvery { userProfileInfoFirestoreDataSource.download("uid") } returns Result.success(null)

        val result = repository.getRemoteUserProfileInfo("uid")

        assertEquals(UserProfileInfo(userId = "uid"), result.getOrThrow())
        coVerify { userProfileInfoDao.upsert(any()) }
    }

    @Test
    fun `getRemoteUserProfileInfo returns failure when download fails`() = runTest {
        val exception = RuntimeException("network error")
        coEvery { userProfileInfoFirestoreDataSource.download("uid") } returns Result.failure(exception)

        val result = repository.getRemoteUserProfileInfo("uid")

        assertEquals(true, result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }

    @Test
    fun `getRemoteUserProfileInfo does not touch local book or session data`() = runTest {
        coEvery { userProfileInfoFirestoreDataSource.download("uid") } returns Result.success(
            UserProfileInfo(userId = "uid")
        )

        repository.getRemoteUserProfileInfo("uid")

        coVerify(exactly = 0) { bookRepository.getAllBooksList() }
        coVerify(exactly = 0) { sessionRepository.getAllSessionsSince(any()) }
    }

    @Test
    fun `setUsername stores the new username locally and remotely`() = runTest {
        coEvery { userProfileInfoDao.getByUserId("uid") } returns null
        coEvery { userProfileInfoFirestoreDataSource.upload("uid", any()) } returns Result.success(Unit)

        val result = repository.setUsername("uid", "elena_marin")

        assertEquals(true, result.isSuccess)
        val info = result.getOrThrow()
        assertEquals("uid", info.userId)
        assertEquals("elena_marin", info.username)
        coVerify { userProfileInfoDao.upsert(any()) }
        coVerify { userProfileInfoFirestoreDataSource.upload("uid", any()) }
    }

    @Test
    fun `setUsername keeps existing profile stats while updating the username`() = runTest {
        coEvery { userProfileInfoDao.getByUserId("uid") } returns UserProfileInfoEntity(
            userId = "uid",
            likesCount = 7,
            sessionsThisWeek = 2,
            weekTimeSeconds = 600L,
            bookCollection = listOf("Book A"),
            lastModified = 1000L,
        )
        coEvery { userProfileInfoFirestoreDataSource.upload("uid", any()) } returns Result.success(Unit)

        val result = repository.setUsername("uid", "elena_marin")

        val info = result.getOrThrow()
        assertEquals(7, info.likesCount)
        assertEquals("elena_marin", info.username)
    }

    @Test
    fun `setUsername returns failure when persisting locally throws`() = runTest {
        coEvery { userProfileInfoDao.getByUserId("uid") } throws RuntimeException("db error")

        val result = repository.setUsername("uid", "elena_marin")

        assertEquals(true, result.isFailure)
        assertEquals("db error", result.exceptionOrNull()?.message)
    }

    @Test
    fun `setLiked returns failure when liking own profile`() = runTest {
        val result = repository.setLiked("me", "me", true)

        assertEquals(true, result.isFailure)
        coVerify(exactly = 0) { userProfileInfoFirestoreDataSource.incrementLikesCount(any(), any()) }
    }

    @Test
    fun `setLiked adds target userId to own followeds when liking`() = runTest {
        coEvery { userProfileInfoFirestoreDataSource.incrementLikesCount("target", 1) } returns Result.success(Unit)
        coEvery { userProfileInfoDao.getByUserId("target") } returns null
        coEvery { userProfileInfoDao.getByUserId("me") } returns null
        coEvery { userProfileInfoFirestoreDataSource.upload("me", any()) } returns Result.success(Unit)

        val result = repository.setLiked("me", "target", true)

        assertEquals(true, result.isSuccess)
        coVerify {
            userProfileInfoFirestoreDataSource.upload(
                "me",
                withArg { assertEquals(listOf("target"), it.followeds) },
            )
        }
    }

    @Test
    fun `setLiked removes target userId from own followeds when unliking`() = runTest {
        coEvery { userProfileInfoFirestoreDataSource.incrementLikesCount("target", -1) } returns Result.success(Unit)
        coEvery { userProfileInfoDao.getByUserId("target") } returns null
        coEvery { userProfileInfoDao.getByUserId("me") } returns UserProfileInfoEntity(
            userId = "me",
            followeds = listOf("target", "other"),
        )
        coEvery { userProfileInfoFirestoreDataSource.upload("me", any()) } returns Result.success(Unit)

        val result = repository.setLiked("me", "target", false)

        assertEquals(true, result.isSuccess)
        coVerify {
            userProfileInfoFirestoreDataSource.upload(
                "me",
                withArg { assertEquals(listOf("other"), it.followeds) },
            )
        }
    }

    @Test
    fun `setLiked returns failure and makes no local writes when remote increment fails`() = runTest {
        val exception = RuntimeException("network error")
        coEvery { userProfileInfoFirestoreDataSource.incrementLikesCount("target", 1) } returns Result.failure(exception)

        val result = repository.setLiked("me", "target", true)

        assertEquals(true, result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        coVerify(exactly = 0) { userProfileInfoFirestoreDataSource.upload(any(), any()) }
    }

    @Test
    fun `setLiked reverts the remote increment when updating own profile fails`() = runTest {
        coEvery { userProfileInfoFirestoreDataSource.incrementLikesCount("target", 1) } returns Result.success(Unit)
        coEvery { userProfileInfoFirestoreDataSource.incrementLikesCount("target", -1) } returns Result.success(Unit)
        coEvery { userProfileInfoDao.getByUserId("target") } returns null
        coEvery { userProfileInfoDao.getByUserId("me") } returns null
        coEvery { userProfileInfoFirestoreDataSource.upload("me", any()) } returns Result.failure(RuntimeException("upload failed"))

        val result = repository.setLiked("me", "target", true)

        assertEquals(true, result.isFailure)
        coVerify { userProfileInfoFirestoreDataSource.incrementLikesCount("target", -1) }
    }
}
