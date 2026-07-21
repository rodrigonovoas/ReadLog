package com.rodrigonovoa.readlog.data.firestore

import com.google.firebase.firestore.FirebaseFirestore
import com.rodrigonovoa.readlog.data.mapper.UserSearchFirestoreMapper
import com.rodrigonovoa.readlog.domain.model.UserSearchResult
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

private const val UNICODE_PREFIX_END_MARKER = ''

@Singleton
class UserSearchFirestoreDataSourceImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val userSearchFirestoreMapper: UserSearchFirestoreMapper,
) : UserSearchFirestoreDataSource {

    override suspend fun searchByUsernamePrefix(
        usernameLowerPrefix: String,
        limit: Int,
    ): Result<List<UserSearchResult>> {
        return try {
            val snapshot = firestore
                .collectionGroup("profile")
                .orderBy("usernameLower")
                .startAt(usernameLowerPrefix)
                .endAt(usernameLowerPrefix + UNICODE_PREFIX_END_MARKER)
                .limit(limit.toLong())
                .get()
                .await()

            val results = snapshot.documents.mapNotNull { userSearchFirestoreMapper.toDomain(it) }
            Result.success(results)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
