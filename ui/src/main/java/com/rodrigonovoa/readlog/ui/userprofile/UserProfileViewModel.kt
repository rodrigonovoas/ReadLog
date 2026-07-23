package com.rodrigonovoa.readlog.ui.userprofile

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rodrigonovoa.readlog.domain.model.UserProfileInfo
import com.rodrigonovoa.readlog.domain.usecase.GetCurrentUserUseCase
import com.rodrigonovoa.readlog.domain.usecase.GetRemoteUserProfileInfoUseCase
import com.rodrigonovoa.readlog.domain.usecase.GetUserDisplayNameUseCase
import com.rodrigonovoa.readlog.domain.usecase.GetUserProfileInfoUseCase
import com.rodrigonovoa.readlog.domain.usecase.ToggleUserLikeUseCase
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
    private val toggleUserLikeUseCase: ToggleUserLikeUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserProfileUiState())
    val uiState: StateFlow<UserProfileUiState> = _uiState.asStateFlow()

    private val targetUserId: String? = savedStateHandle.get<String>("userId")?.takeIf { it.isNotBlank() }

    init {
        val targetUserId = targetUserId
        _uiState.update { it.copy(isOwnProfile = targetUserId == null) }
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
            val info = getUserProfileInfoUseCase(userId)
            applyProfileInfo(info)
            _uiState.update {
                it.copy(username = info.username?.let { name -> "@$name" } ?: it.username)
            }
        }
    }

    private fun loadOtherUserProfile(userId: String) {
        viewModelScope.launch {
            applyProfileInfo(getUserProfileInfoUseCase(userId))
            val currentUserId = getCurrentUserUseCase()?.uid
            if (currentUserId != null) {
                val ownInfo = getUserProfileInfoUseCase(currentUserId)
                _uiState.update { it.copy(isLiked = ownInfo.followeds.contains(userId)) }
            }
            getRemoteUserProfileInfoUseCase(userId).getOrNull()?.let {
                applyProfileInfo(it)
                applyIdentity(it)
            }
        }
    }

    fun onLikeClick() {
        val targetId = targetUserId ?: return
        val currentUser = getCurrentUserUseCase() ?: return
        if (currentUser.uid == targetId) return
        val newLikedState = !_uiState.value.isLiked
        viewModelScope.launch {
            _uiState.update { it.copy(hasLikeError = false) }
            toggleUserLikeUseCase(currentUser.uid, targetId, newLikedState).fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isLiked = newLikedState,
                            likesCount = (it.likesCount + if (newLikedState) 1 else -1).coerceAtLeast(0),
                        )
                    }
                },
                onFailure = {
                    _uiState.update { it.copy(hasLikeError = true) }
                },
            )
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
