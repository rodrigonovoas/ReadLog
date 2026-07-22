package com.rodrigonovoa.readlog.ui.userprofile

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rodrigonovoa.readlog.domain.model.UserProfileInfo
import com.rodrigonovoa.readlog.domain.usecase.GetCurrentUserUseCase
import com.rodrigonovoa.readlog.domain.usecase.GetRemoteUserProfileInfoUseCase
import com.rodrigonovoa.readlog.domain.usecase.GetUserDisplayNameUseCase
import com.rodrigonovoa.readlog.domain.usecase.GetUserProfileInfoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserProfileViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getUserDisplayNameUseCase: GetUserDisplayNameUseCase,
    private val getUserProfileInfoUseCase: GetUserProfileInfoUseCase,
    private val getRemoteUserProfileInfoUseCase: GetRemoteUserProfileInfoUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserProfileUiState())
    val uiState: StateFlow<UserProfileUiState> = _uiState.asStateFlow()

    init {
        val targetUserId = savedStateHandle.get<String>("userId")?.takeIf { it.isNotBlank() }
        if (targetUserId != null) {
            loadOtherUserProfile(targetUserId)
        } else {
            loadOwnProfile()
        }
    }

    private fun loadOwnProfile() {
        val currentUser = getCurrentUserUseCase()
        val userId = currentUser?.uid.orEmpty()
        _uiState.update {
            it.copy(
                userName = getUserDisplayNameUseCase(),
                username = "",
            )
        }

        viewModelScope.launch {
            applyProfileInfo(getUserProfileInfoUseCase(userId))
        }
    }

    private fun loadOtherUserProfile(userId: String) {
        viewModelScope.launch {
            applyProfileInfo(getUserProfileInfoUseCase(userId))
            getRemoteUserProfileInfoUseCase(userId).getOrNull()?.let {
                applyProfileInfo(it)
                applyIdentity(it)
            }
        }
    }

    private fun applyIdentity(info: UserProfileInfo) {
        _uiState.update {
            it.copy(
                userName = info.displayName?.split(" ")?.firstOrNull()?.takeIf { name -> name.isNotBlank() }
                    ?: it.userName,
                username = info.username?.let { name -> "@$name" } ?: it.username,
            )
        }
    }

    private fun applyProfileInfo(info: UserProfileInfo) {
        _uiState.update {
            it.copy(
                followersCount = info.followersCount,
                likesCount = info.likesCount,
                weeklySessionsCount = info.sessionsThisWeek,
                weeklyTimeLabel = formatDuration(info.weekTimeSeconds),
                collectionBooks = info.bookCollection.map { title -> UserProfileBook(title = title) },
            )
        }
    }

    private fun formatDuration(totalSeconds: Long): String {
        val minutes = totalSeconds / 60
        return if (minutes < 60) {
            "$minutes min"
        } else {
            val hours = minutes / 60
            val remainingMinutes = minutes % 60
            "%dh %02dmin".format(hours, remainingMinutes)
        }
    }
}
