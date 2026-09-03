package com.rodrigonovoa.readlog.data.firestore

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.rodrigonovoa.readlog.data.mapper.UserProfileInfoFirestoreMapper
import com.rodrigonovoa.readlog.domain.model.UserProfileInfo
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserProfileInfoFirestoreDataSourceImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val userProfileInfoFirestoreMapper: UserProfileInfoFirestoreMapper,
) : UserProfileInfoFirestoreDataSource {

    override suspend fun upload(userId: String, info: UserProfileInfo): Result<Unit> {
        return try {
            firestore
                .collection("users")
                .document(userId)
                .collection("profile")
                .document("info")
                .set(userProfileInfoFirestoreMapper.toFirestoreMap(info))
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun download(userId: String): Result<UserProfileInfo?> {
        return try {
            val snapshot = firestore
                .collection("users")
                .document(userId)
                .collection("profile")
                .document("info")
                .get()
                .await()
            val info = if (snapshot.exists()) {
                userProfileInfoFirestoreMapper.fromFirestoreMap(snapshot.data ?: emptyMap(), userId)
            } else null
            Result.success(info)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun setLike(currentUserId: String, targetUserId: String, liked: Boolean): Result<Boolean> {
        return try {
            val targetRef = profileInfoReference(targetUserId)
            val currentRef = profileInfoReference(currentUserId)

            val changed = firestore.runTransaction { transaction ->
                val targetSnapshot = transaction.get(targetRef)
                val currentSnapshot = transaction.get(currentRef)
                val currentInfo = if (currentSnapshot.exists()) {
                    userProfileInfoFirestoreMapper.fromFirestoreMap(
                        currentSnapshot.data ?: emptyMap(),
                        currentUserId,
                    )
                } else {
                    UserProfileInfo(userId = currentUserId)
                }
                val alreadyLiked = currentInfo.followeds.contains(targetUserId)

                if (alreadyLiked != liked) {
                    val targetInfo = if (targetSnapshot.exists()) {
                        userProfileInfoFirestoreMapper.fromFirestoreMap(
                            targetSnapshot.data ?: emptyMap(),
                            targetUserId,
                        )
                    } else {
                        UserProfileInfo(userId = targetUserId)
                    }
                    val updatedCurrent = currentInfo.copy(
                        followeds = if (liked) {
                            (currentInfo.followeds + targetUserId).distinct()
                        } else {
                            currentInfo.followeds - targetUserId
                        },
                        lastModified = System.currentTimeMillis(),
                    )
                    val updatedTarget = targetInfo.copy(
                        likesCount = (targetInfo.likesCount + if (liked) 1 else -1).coerceAtLeast(0),
                        lastModified = System.currentTimeMillis(),
                    )
                    transaction.set(currentRef, userProfileInfoFirestoreMapper.toFirestoreMap(updatedCurrent), SetOptions.merge())
                    transaction.set(targetRef, userProfileInfoFirestoreMapper.toFirestoreMap(updatedTarget), SetOptions.merge())
                }
                alreadyLiked != liked
            }.await()
            Result.success(changed)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun profileInfoReference(userId: String) = firestore
        .collection("users")
        .document(userId)
        .collection("profile")
        .document("info")
}
