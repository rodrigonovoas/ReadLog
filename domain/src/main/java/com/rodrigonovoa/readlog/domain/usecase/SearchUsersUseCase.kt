package com.rodrigonovoa.readlog.domain.usecase

import com.rodrigonovoa.readlog.domain.model.UserSearchResult
import com.rodrigonovoa.readlog.domain.repository.UserSearchRepository
import javax.inject.Inject

class SearchUsersUseCase @Inject constructor(
    private val userSearchRepository: UserSearchRepository,
) {
    suspend operator fun invoke(query: String): Result<List<UserSearchResult>> {
        val normalizedQuery = query.trim().lowercase()
        if (normalizedQuery.isEmpty()) {
            return Result.success(emptyList())
        }
        return userSearchRepository.searchByUsername(normalizedQuery)
    }
}
