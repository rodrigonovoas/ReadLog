package com.rodrigonovoa.readlog.data.repository

import com.rodrigonovoa.readlog.data.db.dao.UserProfileInfoDao
import com.rodrigonovoa.readlog.data.firestore.UserProfileInfoFirestoreDataSource
import com.rodrigonovoa.readlog.data.mapper.UserProfileInfoDataMapper
import com.rodrigonovoa.readlog.domain.model.UserProfileInfo
import com.rodrigonovoa.readlog.domain.repository.BookRepository
import com.rodrigonovoa.readlog.domain.repository.SessionRepository
import com.rodrigonovoa.readlog.domain.repository.UserProfileRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserProfileRepositoryImpl @Inject constructor(
    private val userProfileInfoDao: UserProfileInfoDao,
    private val bookRepository: BookRepository,
    private val sessionRepository: SessionRepository,
    private val userProfileInfoDataMapper: UserProfileInfoDataMapper,
    private val userProfileInfoFirestoreDataSource: UserProfileInfoFirestoreDataSource,
) : UserProfileRepository {

    override suspend fun getUserProfileInfo(userId: String): UserProfileInfo {
        return userProfileInfoDao.getByUserId(userId)
            ?.let { userProfileInfoDataMapper.toDomain(it) }
            ?: UserProfileInfo(userId = userId)
    }

    override suspend fun refreshUserProfileInfo(
        userId: String,
        startOfWeekMillis: Long,
        displayName: String?,
    ): Result<UserProfileInfo> {
        return try {
            val weekSessions = sessionRepository.getAllSessionsSince(startOfWeekMillis)
            val books = bookRepository.getAllBooksList()
            val remoteInfo = userProfileInfoFirestoreDataSource.download(userId).getOrNull()
            val resolvedDisplayName = displayName ?: remoteInfo?.displayName

            val merged = UserProfileInfo(
                userId = userId,
                followersCount = remoteInfo?.followersCount ?: 0,
                likesCount = remoteInfo?.likesCount ?: 0,
                sessionsThisWeek = weekSessions.size,
                weekTimeSeconds = weekSessions.sumOf { it.time },
                bookCollection = books.map { it.title },
                lastModified = System.currentTimeMillis(),
                displayName = resolvedDisplayName,
                username = remoteInfo?.username?.ifBlank { null },
            )

            userProfileInfoDao.upsert(userProfileInfoDataMapper.toEntity(merged))
            userProfileInfoFirestoreDataSource.upload(userId, merged)
            Result.success(merged)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getRemoteUserProfileInfo(userId: String): Result<UserProfileInfo> {
        return userProfileInfoFirestoreDataSource.download(userId).mapCatching { remoteInfo ->
            val info = remoteInfo ?: UserProfileInfo(userId = userId)
            userProfileInfoDao.upsert(userProfileInfoDataMapper.toEntity(info))
            info
        }
    }

    override suspend fun setUsername(userId: String, username: String): Result<UserProfileInfo> {
        return try {
            val current = getUserProfileInfo(userId)
            val updated = current.copy(
                userId = userId,
                username = username,
                lastModified = System.currentTimeMillis(),
            )
            userProfileInfoDao.upsert(userProfileInfoDataMapper.toEntity(updated))
            userProfileInfoFirestoreDataSource.upload(userId, updated)
            Result.success(updated)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
