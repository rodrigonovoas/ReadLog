package com.rodrigonovoa.readlog.domain.repository

import com.rodrigonovoa.readlog.domain.model.User

interface AuthRepository {
    suspend fun signInWithGoogle(idToken: String): Result<Unit>
    suspend fun continueOffline(): Result<Unit>
    fun getCurrentUser(): User?
    fun isUserSignedIn(): Boolean
    suspend fun signOut()
}
