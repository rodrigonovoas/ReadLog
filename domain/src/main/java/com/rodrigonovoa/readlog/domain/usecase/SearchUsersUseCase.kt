package com.rodrigonovoa.readlog.domain.usecase

import com.rodrigonovoa.readlog.domain.model.UserSearchResult
import com.rodrigonovoa.readlog.domain.repository.UserSearchRepository
import javax.inject.Inject

class SearchUsersUseCase @Inject constructor(
    private val userSearchRepository: UserSearchRepository,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
) {
    suspend operator fun invoke(query: String): Result<List<UserSearchResult>> {
        val normalizedQuery = query.trim().lowercase()
        if (normalizedQuery.isEmpty()) {
            return Result.success(emptyList())
        }
        val currentUserId = getCurrentUserUseCase()?.uid
        return userSearchRepository.searchByUsername(normalizedQuery).map { results ->
            currentUserId?.let { uid -> results.filter { it.userId != uid } } ?: results
        }
    }
}
