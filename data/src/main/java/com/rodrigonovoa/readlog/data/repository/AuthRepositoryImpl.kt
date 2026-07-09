package com.rodrigonovoa.readlog.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.rodrigonovoa.readlog.domain.model.User
import com.rodrigonovoa.readlog.domain.repository.AuthRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : AuthRepository {

    override suspend fun signInWithGoogle(idToken: String): Result<Unit> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            suspendCoroutine { continuation ->
                firebaseAuth.signInWithCredential(credential)
                    .addOnSuccessListener { continuation.resume(Result.success(Unit)) }
                    .addOnFailureListener { continuation.resume(Result.failure(it)) }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun continueOffline(): Result<Unit> {
        return Result.success(Unit)
    }

    override fun getCurrentUser(): User? {
        return firebaseAuth.currentUser?.toDomainUser()
    }

    override fun isUserSignedIn(): Boolean {
        return firebaseAuth.currentUser != null
    }

    override suspend fun signOut() {
        firebaseAuth.signOut()
    }

    private fun com.google.firebase.auth.FirebaseUser.toDomainUser(): User {
        return User(
            uid = uid,
            email = email,
            displayName = displayName,
        )
    }
}
