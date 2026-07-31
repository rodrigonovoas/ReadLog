package com.rodrigonovoa.readlog.domain.usecase

import com.rodrigonovoa.readlog.domain.model.UserProfileInfo
import com.rodrigonovoa.readlog.domain.repository.UserProfileRepository
import java.util.Calendar
import javax.inject.Inject

class RefreshUserProfileInfoUseCase @Inject constructor(
    private val userProfileRepository: UserProfileRepository,
) {
    suspend operator fun invoke(userId: String, displayName: String?): Result<UserProfileInfo> {
        val startOfMonth = startOfCurrentMonthMillis()
        return userProfileRepository.refreshUserProfileInfo(userId, startOfMonth, displayName)
    }

    private fun startOfCurrentMonthMillis(): Long {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }
}
