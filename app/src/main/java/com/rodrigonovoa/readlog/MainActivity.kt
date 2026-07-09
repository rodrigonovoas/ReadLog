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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.rodrigonovoa.readlog.domain.usecase.IsUserSignedInUseCase
import com.rodrigonovoa.readlog.ui.bookcollection.BookCollectionScreen
import com.rodrigonovoa.readlog.ui.login.LoginEffect
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

                        LaunchedEffect(Unit) {
                            viewModel.effect.collect { effect ->
                                when (effect) {
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
