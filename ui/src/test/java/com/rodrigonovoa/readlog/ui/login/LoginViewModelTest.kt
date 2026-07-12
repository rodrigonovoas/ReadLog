package com.rodrigonovoa.readlog.ui.login

import com.rodrigonovoa.readlog.domain.auth.AuthLauncher
import com.rodrigonovoa.readlog.domain.model.User
import com.rodrigonovoa.readlog.domain.usecase.ContinueOfflineUseCase
import com.rodrigonovoa.readlog.domain.usecase.GetCurrentUserUseCase
import com.rodrigonovoa.readlog.domain.usecase.SignInWithGoogleUseCase
import com.rodrigonovoa.readlog.domain.usecase.SyncUserDataUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var signInUseCase: SignInWithGoogleUseCase
    private lateinit var continueOfflineUseCase: ContinueOfflineUseCase
    private lateinit var getCurrentUserUseCase: GetCurrentUserUseCase
    private lateinit var syncUserDataUseCase: SyncUserDataUseCase
    private lateinit var authLauncher: AuthLauncher
    private lateinit var viewModel: LoginViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        signInUseCase = mockk()
        continueOfflineUseCase = mockk()
        getCurrentUserUseCase = mockk()
        syncUserDataUseCase = mockk()
        authLauncher = mockk()
        viewModel = LoginViewModel(
            signInWithGoogleUseCase = signInUseCase,
            continueOfflineUseCase = continueOfflineUseCase,
            getCurrentUserUseCase = getCurrentUserUseCase,
            syncUserDataUseCase = syncUserDataUseCase,
            authLauncher = authLauncher,
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is not loading and has no error`() = runTest {
        val state = viewModel.uiState.value

        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
    }

    @Test
    fun `google sign in success navigates to collection and triggers sync`() = runTest {
        coEvery { authLauncher.launchGoogleSignIn() } returns Result.success("token")
        coEvery { signInUseCase(any()) } returns Result.success(Unit)
        every { getCurrentUserUseCase() } returns User("uid123", "test@test.com", "Test User")
        coEvery { syncUserDataUseCase(any()) } returns Result.success(Unit)

        var effect: LoginEffect? = null
        val collectJob = launch { effect = viewModel.effect.first() }

        viewModel.processIntent(LoginIntent.OnGoogleSignInClicked)
        advanceUntilIdle()
        collectJob.join()

        assertFalse(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.errorMessage)
        assertTrue(effect is LoginEffect.NavigateToCollection)
        coVerify { syncUserDataUseCase("uid123") }
    }

    @Test
    fun `google sign in token failure shows error`() = runTest {
        coEvery { authLauncher.launchGoogleSignIn() } returns Result.failure(RuntimeException("Google error"))

        viewModel.processIntent(LoginIntent.OnGoogleSignInClicked)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals("Google error", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `google sign in auth failure shows error`() = runTest {
        coEvery { authLauncher.launchGoogleSignIn() } returns Result.success("token")
        coEvery { signInUseCase(any()) } returns Result.failure(RuntimeException("Auth error"))

        viewModel.processIntent(LoginIntent.OnGoogleSignInClicked)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals("Auth error", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `continue offline navigates to collection`() = runTest {
        coEvery { continueOfflineUseCase() } returns Result.success(Unit)

        var effect: LoginEffect? = null
        val collectJob = launch { effect = viewModel.effect.first() }

        viewModel.processIntent(LoginIntent.OnContinueOfflineClicked)
        advanceUntilIdle()
        collectJob.join()

        assertTrue(effect is LoginEffect.NavigateToCollection)
    }

    @Test
    fun `dismiss error clears error message`() = runTest {
        coEvery { authLauncher.launchGoogleSignIn() } returns Result.failure(RuntimeException("Error"))
        viewModel.processIntent(LoginIntent.OnGoogleSignInClicked)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.errorMessage != null)

        viewModel.processIntent(LoginIntent.DismissError)
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.errorMessage)
    }
}
