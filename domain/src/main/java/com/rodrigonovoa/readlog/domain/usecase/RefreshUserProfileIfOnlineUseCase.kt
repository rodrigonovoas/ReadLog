package com.rodrigonovoa.readlog.domain.usecase

import javax.inject.Inject

class RefreshUserProfileIfOnlineUseCase @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val isOnlineUseCase: IsOnlineUseCase,
    private val refreshUserProfileInfoUseCase: RefreshUserProfileInfoUseCase,
) {
    suspend operator fun invoke() {
        val currentUser = getCurrentUserUseCase() ?: return
        if (!isOnlineUseCase()) return
        runCatching { refreshUserProfileInfoUseCase(currentUser.uid, currentUser.displayName) }
    }
}
