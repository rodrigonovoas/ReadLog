package com.rodrigonovoa.readlog.domain.repository

import com.rodrigonovoa.readlog.domain.model.UserSearchResult

interface UserSearchRepository {
    suspend fun searchByUsername(query: String, limit: Int = 20): Result<List<UserSearchResult>>
}
