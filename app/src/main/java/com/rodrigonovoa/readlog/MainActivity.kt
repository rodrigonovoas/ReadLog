package com.rodrigonovoa.readlog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.rodrigonovoa.readlog.domain.usecase.IsUserSignedInUseCase
import com.rodrigonovoa.readlog.ui.bookcollection.BookCollectionScreen
import com.rodrigonovoa.readlog.ui.login.LoginEffect
import com.rodrigonovoa.readlog.ui.login.LoginIntent
import com.rodrigonovoa.readlog.ui.login.LoginScreen
import com.rodrigonovoa.readlog.ui.login.LoginViewModel
import com.rodrigonovoa.readlog.ui.theme.ReadLogTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var isUserSignedInUseCase: IsUserSignedInUseCase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val startDestination = if (isUserSignedInUseCase()) "bookCollection" else "login"

        setContent {
            ReadLogTheme {
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = startDestination,
                ) {
                    composable("login") {
                        val viewModel: LoginViewModel = hiltViewModel()
                        val state by viewModel.uiState.collectAsState()
                        val context = LocalContext.current

                        LaunchedEffect(Unit) {
                            viewModel.effect.collect { effect ->
                                when (effect) {
                                    is LoginEffect.LaunchGoogleSignIn -> {
                                        try {
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
                                            val idToken = googleIdTokenCredential.idToken
                                            viewModel.processIntent(
                                                LoginIntent.OnGoogleTokenReceived(idToken)
                                            )
                                        } catch (e: androidx.credentials.exceptions.GetCredentialException) {
                                            print("ERROR")
                                            print(e)
                                            viewModel.processIntent(
                                                LoginIntent.OnGoogleSignInFailed(null)
                                            )
                                        } catch (e: com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException) {
                                            viewModel.processIntent(
                                                LoginIntent.OnGoogleSignInFailed("Invalid Google credentials")
                                            )
                                        } catch (e: Exception) {
                                            viewModel.processIntent(
                                                LoginIntent.OnGoogleSignInFailed(e.localizedMessage)
                                            )
                                        }
                                    }
                                    is LoginEffect.NavigateToCollection -> {
                                        navController.navigate("bookCollection") {
                                            popUpTo("login") { inclusive = true }
                                        }
                                    }
                                }
                            }
                        }

                        LoginScreen(
                            modifier = Modifier.fillMaxSize(),
                            state = state,
                            onIntent = viewModel::processIntent,
                        )
                    }
                    composable("bookCollection") {
                        BookCollectionScreen(modifier = Modifier.fillMaxSize())
                    }
                }
            }
        }
    }
}
