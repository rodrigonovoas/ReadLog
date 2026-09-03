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
        startOfMonthMillis: Long,
        displayName: String?,
    ): Result<UserProfileInfo> {
        return try {
            val monthSessions = sessionRepository.getAllSessionsSince(startOfMonthMillis)
            val books = bookRepository.getAllBooksList()
            val remoteInfo = userProfileInfoFirestoreDataSource.download(userId).getOrNull()
            val resolvedDisplayName = displayName ?: remoteInfo?.displayName

            val merged = UserProfileInfo(
                userId = userId,
                likesCount = remoteInfo?.likesCount ?: 0,
                sessionsThisMonth = monthSessions.size,
                monthTimeSeconds = monthSessions.sumOf { it.time },
                bookCollection = books.map { it.title },
                lastModified = System.currentTimeMillis(),
                displayName = resolvedDisplayName,
                username = remoteInfo?.username?.ifBlank { null },
                followeds = remoteInfo?.followeds ?: emptyList(),
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
            val remoteResult = userProfileInfoFirestoreDataSource.claimUsername(userId, updated)
            if (remoteResult.isFailure) {
                return remoteResult.map { updated }
            }
            userProfileInfoDao.upsert(userProfileInfoDataMapper.toEntity(updated))
            Result.success(updated)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun setLiked(
        currentUserId: String,
        targetUserId: String,
        liked: Boolean,
    ): Result<Unit> {
        if (currentUserId == targetUserId) {
            return Result.failure(IllegalArgumentException("Cannot like own profile"))
        }
        val result = userProfileInfoFirestoreDataSource.setLike(currentUserId, targetUserId, liked)
        if (result.getOrNull() == true) {
            runCatching {
                val currentInfo = getUserProfileInfo(currentUserId)
                val updatedCurrent = currentInfo.copy(
                    followeds = if (liked) {
                        (currentInfo.followeds + targetUserId).distinct()
                    } else {
                        currentInfo.followeds - targetUserId
                    },
                    lastModified = System.currentTimeMillis(),
                )
                userProfileInfoDao.upsert(userProfileInfoDataMapper.toEntity(updatedCurrent))

                val targetInfo = getUserProfileInfo(targetUserId)
                userProfileInfoDao.upsert(
                    userProfileInfoDataMapper.toEntity(
                        targetInfo.copy(
                            likesCount = (targetInfo.likesCount + if (liked) 1 else -1).coerceAtLeast(0),
                        ),
                    ),
                )
            }
        }
        return result.map { Unit }
    }

    override suspend fun getCachedLikedProfiles(currentUserId: String): Result<List<UserProfileInfo>> {
        return try {
            val followeds = getUserProfileInfo(currentUserId).followeds.distinct()
            Result.success(
                followeds.mapNotNull { userId ->
                    getUserProfileInfo(userId).takeIf { !it.username.isNullOrBlank() }
                },
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getLikedProfiles(currentUserId: String): Result<List<UserProfileInfo>> {
        return try {
            val ownInfoResult = userProfileInfoFirestoreDataSource.download(currentUserId)
            if (ownInfoResult.isFailure) {
                return Result.failure(ownInfoResult.exceptionOrNull()!!)
            }
            val followeds = (ownInfoResult.getOrNull()?.followeds ?: getUserProfileInfo(currentUserId).followeds)
                .distinct()

            val profiles = followeds.mapNotNull { userId ->
                userProfileInfoFirestoreDataSource.download(userId).getOrNull()
                    ?.takeIf { !it.username.isNullOrBlank() }
                    ?.also { userProfileInfoDao.upsert(userProfileInfoDataMapper.toEntity(it)) }
            }

            Result.success(profiles)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
