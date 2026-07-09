package com.rodrigonovoa.readlog.domain.usecase

import com.rodrigonovoa.readlog.domain.repository.AuthRepository
import javax.inject.Inject

open class SignInWithGoogleUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    open suspend operator fun invoke(idToken: String): Result<Unit> {
        return authRepository.signInWithGoogle(idToken)
    }
}
