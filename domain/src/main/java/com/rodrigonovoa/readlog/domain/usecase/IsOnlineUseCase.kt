package com.rodrigonovoa.readlog.domain.usecase

import com.rodrigonovoa.readlog.domain.repository.ConnectivityRepository
import javax.inject.Inject

class IsOnlineUseCase @Inject constructor(
    private val connectivityRepository: ConnectivityRepository
) {
    operator fun invoke(): Boolean {
        return connectivityRepository.isOnline()
    }
}
