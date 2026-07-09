package com.rodrigonovoa.readlog.ui.fakes

import com.rodrigonovoa.readlog.domain.usecase.SignInWithGoogleUseCase

class FakeSignInWithGoogleUseCase : SignInWithGoogleUseCase(
    authRepository = FakeAuthRepository()
) {
    var result: Result<Unit> = Result.success(Unit)

    override suspend fun invoke(idToken: String): Result<Unit> {
        return result
    }
}
