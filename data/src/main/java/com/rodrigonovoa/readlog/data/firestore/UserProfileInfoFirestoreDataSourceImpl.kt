package com.rodrigonovoa.readlog.data.firestore

import com.google.firebase.firestore.FirebaseFirestore
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
}
