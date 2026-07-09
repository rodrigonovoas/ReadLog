package com.rodrigonovoa.readlog.domain.auth

interface AuthLauncher {
    suspend fun launchGoogleSignIn(): Result<String>
}
