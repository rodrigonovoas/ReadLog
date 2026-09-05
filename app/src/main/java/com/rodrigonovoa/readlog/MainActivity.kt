package com.rodrigonovoa.readlog

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.os.LocaleListCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import java.util.Locale
import kotlinx.coroutines.launch
import com.rodrigonovoa.readlog.domain.usecase.IsUserSignedInUseCase
import com.rodrigonovoa.readlog.ui.addbook.AddBookEffect
import com.rodrigonovoa.readlog.ui.addbook.AddBookIntent
import com.rodrigonovoa.readlog.ui.addbook.AddBookScreen
import com.rodrigonovoa.readlog.ui.addbook.AddBookViewModel
import com.rodrigonovoa.readlog.ui.bookcollection.BookCollectionEffect
import com.rodrigonovoa.readlog.ui.bookcollection.BookCollectionScreen
import com.rodrigonovoa.readlog.ui.bookcollection.BookCollectionViewModel
import com.rodrigonovoa.readlog.ui.booksession.BookSessionEffect
import com.rodrigonovoa.readlog.ui.booksession.BookSessionScreen
import com.rodrigonovoa.readlog.ui.booksession.BookSessionViewModel
import com.rodrigonovoa.readlog.ui.bookdetail.BookDetailScreen
import com.rodrigonovoa.readlog.ui.bookdetail.BookDetailViewModel
import com.rodrigonovoa.readlog.ui.login.LoginEffect
import com.rodrigonovoa.readlog.ui.login.LoginScreen
import com.rodrigonovoa.readlog.ui.login.LoginViewModel
import com.rodrigonovoa.readlog.ui.theme.ReadLogTheme
import com.rodrigonovoa.readlog.ui.userprofile.UserProfileScreen
import com.rodrigonovoa.readlog.ui.userprofile.UserProfileViewModel
import com.rodrigonovoa.readlog.ui.usersearch.UserSearchMode
import com.rodrigonovoa.readlog.ui.usersearch.UserSearchScreen
import com.rodrigonovoa.readlog.ui.usersearch.UserSearchViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var isUserSignedInUseCase: IsUserSignedInUseCase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val startDestination = if (isUserSignedInUseCase()) "bookCollection" else "login"

        setContent {
            ReadLogTheme {
                val navController = rememberNavController()
                val snackbarHostState = remember { SnackbarHostState() }
                val snackbarScope = rememberCoroutineScope()

                Scaffold(
                    snackbarHost = {
                        SnackbarHost(
                            hostState = snackbarHostState,
                            modifier = Modifier.navigationBarsPadding(),
                        )
                    },
                    contentWindowInsets = WindowInsets(0, 0, 0, 0),
                ) {
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
                                            popUpTo(navController.graph.id) { inclusive = true }
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
                        val viewModel: BookCollectionViewModel = hiltViewModel()
                        val uiState by viewModel.uiState.collectAsState()

                        LaunchedEffect(Unit) {
                            viewModel.effect.collect { effect ->
                                when (effect) {
                                    is BookCollectionEffect.NavigateToLogin -> {
                                        navController.navigate("login") {
                                            popUpTo(navController.graph.id) { inclusive = true }
                                        }
                                    }
                                }
                            }
                        }

                        BookCollectionScreen(
                            modifier = Modifier.fillMaxSize(),
                            uiState = uiState,
                            currentLanguage = run {
                                val appLocales = AppCompatDelegate.getApplicationLocales()
                                if (!appLocales.isEmpty) {
                                    appLocales[0]?.language ?: "en"
                                } else {
                                    val systemLanguage = Locale.getDefault().language
                                    if (systemLanguage == "es") "es" else "en"
                                }
                            },
                            onAddBookClick = { navController.navigate("addBook") },
                            onEditIconClick = { bookId ->
                                navController.navigate("addBook?bookId=$bookId")
                            },
                            onDeleteIconClick = viewModel::onDeleteIconClick,
                            onSessionClick = { bookId ->
                                navController.navigate("bookSession?bookId=$bookId")
                            },
                            onBookClick = { bookId ->
                                navController.navigate("bookDetail?bookId=$bookId")
                            },
                            onDismissDialog = viewModel::dismissDialog,
                            onConfirmDelete = viewModel::confirmDelete,
                            onProfileMenuProfileClick = { navController.navigate("userProfile") },
                            onProfileMenuLikesClick = { navController.navigate("likes") },
                            onProfileMenuLoginClick = { navController.navigate("login") },
                            onProfileMenuLogoutClick = viewModel::onLogoutClicked,
                            onDismissLogoutDialog = viewModel::dismissLogoutDialog,
                            onConfirmLogout = viewModel::confirmLogout,
                            onLanguageSelected = { languageTag ->
                                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(languageTag))
                            },
                            onSearchUsersClick = { navController.navigate("userSearch") },
                            onUsernameChange = viewModel::onUsernameChanged,
                            onUsernameConfirm = viewModel::onUsernameConfirmClicked,
                            onFilterClick = viewModel::onFilterClick,
                            onFilterAccepted = viewModel::onFilterAccepted,
                            onFilterDismissed = viewModel::dismissFilterDialog,
                            onFilterCleared = viewModel::clearFilters,
                        )
                    }
                    composable(
                        "userProfile?userId={userId}",
                        arguments = listOf(
                            androidx.navigation.navArgument("userId") {
                                type = androidx.navigation.NavType.StringType
                                defaultValue = ""
                            }
                        )
                    ) {
                        val viewModel: UserProfileViewModel = hiltViewModel()
                        val uiState by viewModel.uiState.collectAsState()

                        UserProfileScreen(
                            modifier = Modifier.fillMaxSize(),
                            uiState = uiState,
                            onBackClick = { navController.popBackStack() },
                            onLikeClick = viewModel::onLikeClick,
                            onSearchVisibilityChange = viewModel::onSearchVisibilityChange,
                        )
                    }
                    composable("userSearch") {
                        val viewModel: UserSearchViewModel = hiltViewModel()
                        val uiState by viewModel.uiState.collectAsState()

                        UserSearchScreen(
                            modifier = Modifier.fillMaxSize(),
                            uiState = uiState,
                            onQueryChange = viewModel::onQueryChange,
                            onBackClick = { navController.popBackStack() },
                            onUserClick = { userId -> navController.navigate("userProfile?userId=$userId") },
                            onUnlikeClick = viewModel::onUnlikeClick,
                        )
                    }
                    composable(
                        "likes?mode={mode}",
                        arguments = listOf(
                            androidx.navigation.navArgument("mode") {
                                type = androidx.navigation.NavType.StringType
                                defaultValue = UserSearchMode.LIKES.name
                            }
                        )
                    ) {
                        val viewModel: UserSearchViewModel = hiltViewModel()
                        val uiState by viewModel.uiState.collectAsState()

                        UserSearchScreen(
                            modifier = Modifier.fillMaxSize(),
                            uiState = uiState,
                            onQueryChange = viewModel::onQueryChange,
                            onBackClick = { navController.popBackStack() },
                            onUserClick = { userId -> navController.navigate("userProfile?userId=$userId") },
                            onUnlikeClick = viewModel::onUnlikeClick,
                        )
                    }
                    composable(
                        "bookSession?bookId={bookId}",
                        arguments = listOf(
                            androidx.navigation.navArgument("bookId") {
                                type = androidx.navigation.NavType.IntType
                                defaultValue = -1
                            }
                        )
                    ) {
                        val viewModel: BookSessionViewModel = hiltViewModel()
                        val uiState by viewModel.uiState.collectAsState()

                        LaunchedEffect(Unit) {
                            viewModel.effect.collect { effect ->
                                when (effect) {
                                    is BookSessionEffect.NavigateBack -> {
                                        navController.popBackStack()
                                    }
                                    is BookSessionEffect.NavigateBackWithSnackbar -> {
                                        navController.popBackStack()
                                        snackbarScope.launch {
                                            snackbarHostState.showSnackbar(getString(effect.messageResId))
                                        }
                                    }
                                }
                            }
                        }

                        BookSessionScreen(
                            modifier = Modifier.fillMaxSize(),
                            uiState = uiState,
                            onIntent = viewModel::processIntent,
                        )
                    }
                    composable(
                        "bookDetail?bookId={bookId}",
                        arguments = listOf(
                            androidx.navigation.navArgument("bookId") {
                                type = androidx.navigation.NavType.IntType
                                defaultValue = -1
                            }
                        )
                    ) {
                        val viewModel: BookDetailViewModel = hiltViewModel()
                        val uiState by viewModel.uiState.collectAsState()

                        BookDetailScreen(
                            modifier = Modifier.fillMaxSize(),
                            uiState = uiState,
                            onBackClick = { navController.popBackStack() },
                            onPreviousMonth = { viewModel.onPreviousMonth() },
                            onNextMonth = { viewModel.onNextMonth() },
                        )
                    }
                    composable(
                        "addBook?bookId={bookId}",
                        arguments = listOf(
                            androidx.navigation.navArgument("bookId") {
                                type = androidx.navigation.NavType.IntType
                                defaultValue = -1
                            }
                        )
                    ) { backStackEntry ->
                        val viewModel: AddBookViewModel = hiltViewModel()
                        val state by viewModel.uiState.collectAsState()
                        val cameraPermissionWasRequested = rememberSaveable { mutableStateOf(false) }

                        val cameraPermissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
                            contract = ActivityResultContracts.RequestPermission(),
                        ) { isGranted ->
                            viewModel.processIntent(AddBookIntent.OnCameraPermissionResult(isGranted))
                        }
                        val appSettingsLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
                            contract = ActivityResultContracts.StartActivityForResult(),
                        ) {
                            val isGranted = ContextCompat.checkSelfPermission(
                                this@MainActivity,
                                Manifest.permission.CAMERA,
                            ) == PackageManager.PERMISSION_GRANTED
                            viewModel.processIntent(AddBookIntent.OnCameraPermissionResult(isGranted))
                        }

                        LaunchedEffect(Unit) {
                            viewModel.effect.collect { effect ->
                                when (effect) {
                                    is AddBookEffect.NavigateBack -> {
                                        navController.popBackStack()
                                    }
                                    is AddBookEffect.RequestCameraPermission -> {
                                        val shouldOpenSettings = cameraPermissionWasRequested.value &&
                                            !ActivityCompat.shouldShowRequestPermissionRationale(
                                                this@MainActivity,
                                                Manifest.permission.CAMERA,
                                            )

                                        if (shouldOpenSettings) {
                                            appSettingsLauncher.launch(
                                                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                                    data = Uri.parse("package:$packageName")
                                                },
                                            )
                                        } else {
                                            cameraPermissionWasRequested.value = true
                                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                        }
                                    }
                                }
                            }
                        }

                        AddBookScreen(
                            modifier = Modifier.fillMaxSize(),
                            state = state,
                            onIntent = viewModel::processIntent,
                        )
                    }
                    }
                }
            }
        }
    }
}
