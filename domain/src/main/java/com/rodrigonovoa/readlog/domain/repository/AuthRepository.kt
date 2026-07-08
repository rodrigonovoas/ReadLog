package com.rodrigonovoa.readlog.domain.repository

interface AuthRepository {
    suspend fun signInWithGoogle(): Result<Unit>
    suspend fun continueOffline(): Result<Unit>
    fun isUserSignedIn(): Boolean
    suspend fun signOut()
}
