package com.rodrigonovoa.readlog.ui.fakes

import com.rodrigonovoa.readlog.domain.usecase.ContinueOfflineUseCase

class FakeContinueOfflineUseCase : ContinueOfflineUseCase(
    authRepository = FakeAuthRepository()
) {
    var result: Result<Unit> = Result.success(Unit)

    override suspend fun invoke(): Result<Unit> {
        return result
    }
}
