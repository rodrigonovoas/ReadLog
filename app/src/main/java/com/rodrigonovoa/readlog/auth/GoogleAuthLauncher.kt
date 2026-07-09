package com.rodrigonovoa.readlog.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.rodrigonovoa.readlog.R
import com.rodrigonovoa.readlog.domain.auth.AuthLauncher
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleAuthLauncher @Inject constructor(
    private val context: Context
) : AuthLauncher {

    override suspend fun launchGoogleSignIn(): Result<String> {
        return try {
            val credentialManager = CredentialManager.create(context)
            val googleIdOption = GetGoogleIdOption.Builder()
                .setServerClientId(context.getString(R.string.default_web_client_id))
                .setFilterByAuthorizedAccounts(false)
                .build()
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()
            val result = credentialManager.getCredential(context, request)
            val credential = result.credential
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            Result.success(googleIdTokenCredential.idToken)
        } catch (e: androidx.credentials.exceptions.GetCredentialException) {
            Result.failure(e)
        } catch (e: com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
