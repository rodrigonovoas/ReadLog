package com.rodrigonovoa.readlog.data.repository

import com.rodrigonovoa.readlog.data.db.dao.AnnotationDao
import com.rodrigonovoa.readlog.data.db.dao.BookDao
import com.rodrigonovoa.readlog.data.db.dao.SessionDao
import com.rodrigonovoa.readlog.data.db.entity.AnnotationEntity
import com.rodrigonovoa.readlog.data.db.entity.BookEntity
import com.rodrigonovoa.readlog.data.db.entity.SessionEntity
import com.rodrigonovoa.readlog.data.firestore.AnnotationFirestoreDataSource
import com.rodrigonovoa.readlog.data.firestore.BookFirestoreDataSource
import com.rodrigonovoa.readlog.data.firestore.SessionFirestoreDataSource
import com.rodrigonovoa.readlog.data.mapper.AnnotationDataMapperImpl
import com.rodrigonovoa.readlog.data.mapper.BookDataMapperImpl
import com.rodrigonovoa.readlog.data.mapper.SessionDataMapperImpl
import com.rodrigonovoa.readlog.domain.model.Annotation
import com.rodrigonovoa.readlog.domain.model.Book
import com.rodrigonovoa.readlog.domain.model.Session
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class SyncRepositoryImplTest {

    private lateinit var bookDao: BookDao
    private lateinit var sessionDao: SessionDao
    private lateinit var annotationDao: AnnotationDao
    private lateinit var bookFirestoreDataSource: BookFirestoreDataSource
    private lateinit var sessionFirestoreDataSource: SessionFirestoreDataSource
    private lateinit var annotationFirestoreDataSource: AnnotationFirestoreDataSource
    private lateinit var syncRepository: SyncRepositoryImpl

    @Before
    fun setup() {
        bookDao = mockk(relaxed = true)
        sessionDao = mockk(relaxed = true)
        annotationDao = mockk(relaxed = true)
        bookFirestoreDataSource = mockk()
        sessionFirestoreDataSource = mockk()
        annotationFirestoreDataSource = mockk()
        syncRepository = SyncRepositoryImpl(
            bookDao = bookDao,
            sessionDao = sessionDao,
            annotationDao = annotationDao,
            bookDataMapper = BookDataMapperImpl(),
            sessionDataMapper = SessionDataMapperImpl(),
            annotationDataMapper = AnnotationDataMapperImpl(),
            bookFirestoreDataSource = bookFirestoreDataSource,
            sessionFirestoreDataSource = sessionFirestoreDataSource,
            annotationFirestoreDataSource = annotationFirestoreDataSource,
        )
    }

    @Test
    fun `syncAll downloads new books and inserts them locally`() = runTest {
        val cloudBook = Book(
            bookId = 0,
            remoteId = "book-1",
            title = "Cloud Book",
            author = "Author",
            genre = "Fiction",
            releaseDate = "2024",
            numPages = 200,
            currentPage = 10,
            creationDate = 1000L,
            lastModified = 2000L,
        )
        coEvery { bookFirestoreDataSource.downloadAll("uid") } returns Result.success(listOf(cloudBook))
        coEvery { sessionFirestoreDataSource.downloadAll("uid") } returns Result.success(emptyList())
        coEvery { annotationFirestoreDataSource.downloadAll("uid") } returns Result.success(emptyList())
        coEvery { bookDao.getByRemoteId("book-1") } returns null
        coEvery { bookDao.getAllList() } returns emptyList()
        coEvery { sessionDao.getAllList() } returns emptyList()
        coEvery { annotationDao.getAllList() } returns emptyList()

        val result = syncRepository.syncAll("uid")

        assertEquals(true, result.isSuccess)
        coVerify { bookDao.insert(any()) }
    }

    @Test
    fun `syncAll updates local book when cloud is newer`() = runTest {
        val cloudBook = Book(
            bookId = 0,
            remoteId = "book-1",
            title = "Updated Title",
            author = "Author",
            genre = "Fiction",
            releaseDate = "2024",
            numPages = 200,
            currentPage = 10,
            creationDate = 1000L,
            lastModified = 3000L,
        )
        val localEntity = BookEntity(
            bookId = 1,
            remoteId = "book-1",
            title = "Old Title",
            author = "Author",
            genre = "Fiction",
            releaseDate = "2024",
            numPages = 200,
            currentPage = 10,
            creationDate = 1000L,
            lastModified = 2000L,
        )
        coEvery { bookFirestoreDataSource.downloadAll("uid") } returns Result.success(listOf(cloudBook))
        coEvery { sessionFirestoreDataSource.downloadAll("uid") } returns Result.success(emptyList())
        coEvery { annotationFirestoreDataSource.downloadAll("uid") } returns Result.success(emptyList())
        coEvery { bookDao.getByRemoteId("book-1") } returns localEntity
        coEvery { bookDao.getAllList() } returns emptyList()
        coEvery { sessionDao.getAllList() } returns emptyList()
        coEvery { annotationDao.getAllList() } returns emptyList()

        val result = syncRepository.syncAll("uid")

        assertEquals(true, result.isSuccess)
        coVerify { bookDao.update(any()) }
    }

    @Test
    fun `syncAll keeps local book when it is newer than cloud`() = runTest {
        val cloudBook = Book(
            bookId = 0,
            remoteId = "book-1",
            title = "Cloud Title",
            author = "Author",
            genre = "Fiction",
            releaseDate = "2024",
            numPages = 200,
            currentPage = 10,
            creationDate = 1000L,
            lastModified = 1000L,
        )
        val localEntity = BookEntity(
            bookId = 1,
            remoteId = "book-1",
            title = "Local Title",
            author = "Author",
            genre = "Fiction",
            releaseDate = "2024",
            numPages = 200,
            currentPage = 10,
            creationDate = 1000L,
            lastModified = 3000L,
        )
        coEvery { bookFirestoreDataSource.downloadAll("uid") } returns Result.success(listOf(cloudBook))
        coEvery { sessionFirestoreDataSource.downloadAll("uid") } returns Result.success(emptyList())
        coEvery { annotationFirestoreDataSource.downloadAll("uid") } returns Result.success(emptyList())
        coEvery { bookDao.getByRemoteId("book-1") } returns localEntity
        coEvery { bookDao.getAllList() } returns listOf(localEntity)
        coEvery { sessionDao.getAllList() } returns emptyList()
        coEvery { annotationDao.getAllList() } returns emptyList()
        coEvery { bookFirestoreDataSource.upload("uid", any()) } returns Result.success(Unit)

        val result = syncRepository.syncAll("uid")

        assertEquals(true, result.isSuccess)
        coVerify(exactly = 0) { bookDao.update(any()) }
        coVerify { bookFirestoreDataSource.upload("uid", any()) }
    }

    @Test
    fun `syncAll skips orphan sessions when book not found locally`() = runTest {
        val cloudSession = Session(
            sessionId = 0,
            remoteId = "session-1",
            bookId = 0,
            bookRemoteId = "missing-book",
            time = 3600000L,
            creationDate = 1000L,
            lastModified = 2000L,
        )
        coEvery { bookFirestoreDataSource.downloadAll("uid") } returns Result.success(emptyList())
        coEvery { sessionFirestoreDataSource.downloadAll("uid") } returns Result.success(listOf(cloudSession))
        coEvery { annotationFirestoreDataSource.downloadAll("uid") } returns Result.success(emptyList())
        coEvery { bookDao.getByRemoteId("missing-book") } returns null
        coEvery { bookDao.getAllList() } returns emptyList()
        coEvery { sessionDao.getAllList() } returns emptyList()
        coEvery { annotationDao.getAllList() } returns emptyList()

        val result = syncRepository.syncAll("uid")

        assertEquals(true, result.isSuccess)
        coVerify(exactly = 0) { sessionDao.insert(any()) }
    }

    @Test
    fun `syncAll uploads local sessions with resolved bookRemoteId`() = runTest {
        val localBook = BookEntity(
            bookId = 1,
            remoteId = "book-1",
            title = "Title",
            author = "Author",
            genre = "Fiction",
            releaseDate = "2024",
            numPages = 200,
            currentPage = 10,
            creationDate = 1000L,
            lastModified = 5000L,
        )
        val localSession = SessionEntity(
            sessionId = 2,
            remoteId = "session-1",
            bookId = 1,
            bookRemoteId = "",
            time = 3600000L,
            creationDate = 1000L,
            lastModified = 6000L,
        )
        coEvery { bookFirestoreDataSource.downloadAll("uid") } returns Result.success(emptyList())
        coEvery { sessionFirestoreDataSource.downloadAll("uid") } returns Result.success(emptyList())
        coEvery { annotationFirestoreDataSource.downloadAll("uid") } returns Result.success(emptyList())
        coEvery { bookDao.getAllList() } returns listOf(localBook)
        coEvery { sessionDao.getAllList() } returns listOf(localSession)
        coEvery { bookDao.getById(1) } returns localBook
        coEvery { annotationDao.getAllList() } returns emptyList()
        coEvery { bookFirestoreDataSource.upload("uid", any()) } returns Result.success(Unit)
        coEvery { sessionFirestoreDataSource.upload("uid", any()) } returns Result.success(Unit)

        val result = syncRepository.syncAll("uid")

        assertEquals(true, result.isSuccess)
        val slot = slot<Session>()
        coVerify { sessionFirestoreDataSource.upload("uid", capture(slot)) }
        assertEquals("book-1", slot.captured.bookRemoteId)
    }

    @Test
    fun `syncAll returns failure when book download fails`() = runTest {
        coEvery { bookFirestoreDataSource.downloadAll("uid") } returns Result.failure(RuntimeException("Network error"))
        coEvery { sessionFirestoreDataSource.downloadAll("uid") } returns Result.success(emptyList())
        coEvery { annotationFirestoreDataSource.downloadAll("uid") } returns Result.success(emptyList())

        val result = syncRepository.syncAll("uid")

        assertEquals(true, result.isFailure)
        assertEquals("Network error", result.exceptionOrNull()?.message)
    }
}
