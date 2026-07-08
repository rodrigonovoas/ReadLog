package com.rodrigonovoa.readlog.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.rodrigonovoa.readlog.domain.repository.AuthRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : AuthRepository {

    override suspend fun signInWithGoogle(): Result<Unit> {
        return try {
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun continueOffline(): Result<Unit> {
        return Result.success(Unit)
    }

    override fun isUserSignedIn(): Boolean {
        return firebaseAuth.currentUser != null
    }

    override suspend fun signOut() {
        firebaseAuth.signOut()
    }
}
