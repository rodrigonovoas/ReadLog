package com.rodrigonovoa.readlog.data.repository

import com.rodrigonovoa.readlog.data.firestore.UserSearchFirestoreDataSource
import com.rodrigonovoa.readlog.domain.model.UserSearchResult
import com.rodrigonovoa.readlog.domain.repository.UserSearchRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserSearchRepositoryImpl @Inject constructor(
    private val userSearchFirestoreDataSource: UserSearchFirestoreDataSource,
) : UserSearchRepository {

    override suspend fun searchByUsername(query: String, limit: Int): Result<List<UserSearchResult>> {
        return userSearchFirestoreDataSource.searchByUsernamePrefix(query, limit)
    }
}
