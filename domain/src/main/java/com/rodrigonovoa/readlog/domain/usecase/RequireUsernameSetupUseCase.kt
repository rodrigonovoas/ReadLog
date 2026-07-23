package com.rodrigonovoa.readlog.domain.usecase

import javax.inject.Inject

class RequireUsernameSetupUseCase @Inject constructor(
    private val getRemoteUserProfileInfoUseCase: GetRemoteUserProfileInfoUseCase,
    private val suggestAvailableUsernameUseCase: SuggestAvailableUsernameUseCase,
) {
    suspend operator fun invoke(userId: String, email: String?, displayName: String?): String? {
        val remoteUsername = getRemoteUserProfileInfoUseCase(userId).getOrNull()?.username
        if (!remoteUsername.isNullOrBlank()) return null
        return suggestAvailableUsernameUseCase(email, displayName, userId)
    }
}
