package com.rodrigonovoa.readlog.domain.fakes

import com.rodrigonovoa.readlog.domain.model.User
import com.rodrigonovoa.readlog.domain.repository.AuthRepository

class FakeAuthRepository : AuthRepository {

    var fakeCurrentUser: User? = null
    var signInWithGoogleResult: Result<Unit> = Result.success(Unit)
    var continueOfflineResult: Result<Unit> = Result.success(Unit)
    var signOutCalled = false

    override suspend fun signInWithGoogle(idToken: String): Result<Unit> {
        return signInWithGoogleResult
    }

    override suspend fun continueOffline(): Result<Unit> {
        return continueOfflineResult
    }

    override fun getCurrentUser(): User? {
        return fakeCurrentUser
    }

    override fun isUserSignedIn(): Boolean {
        return fakeCurrentUser != null
    }

    override suspend fun signOut() {
        signOutCalled = true
        fakeCurrentUser = null
    }
}
