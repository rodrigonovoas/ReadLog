package com.rodrigonovoa.readlog.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.rodrigonovoa.readlog.data.mapper.UserDataMapper
import com.rodrigonovoa.readlog.domain.model.User
import com.rodrigonovoa.readlog.domain.repository.AuthRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val userDataMapper: UserDataMapper,
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
        return try {
            suspendCoroutine { continuation ->
                firebaseAuth.signInAnonymously()
                    .addOnSuccessListener { continuation.resume(Result.success(Unit)) }
                    .addOnFailureListener { continuation.resume(Result.failure(it)) }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getCurrentUser(): User? {
        return firebaseAuth.currentUser?.let { userDataMapper.toDomain(it) }
    }

    override fun isUserSignedIn(): Boolean {
        return firebaseAuth.currentUser != null
    }

    override suspend fun signOut() {
        firebaseAuth.signOut()
    }
}
