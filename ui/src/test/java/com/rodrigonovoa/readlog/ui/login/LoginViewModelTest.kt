package com.rodrigonovoa.readlog.ui.login

import com.rodrigonovoa.readlog.ui.fakes.FakeAuthLauncher
import com.rodrigonovoa.readlog.ui.fakes.FakeContinueOfflineUseCase
import com.rodrigonovoa.readlog.ui.fakes.FakeSignInWithGoogleUseCase
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
    private lateinit var signInUseCase: FakeSignInWithGoogleUseCase
    private lateinit var continueOfflineUseCase: FakeContinueOfflineUseCase
    private lateinit var authLauncher: FakeAuthLauncher
    private lateinit var viewModel: LoginViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        signInUseCase = FakeSignInWithGoogleUseCase()
        continueOfflineUseCase = FakeContinueOfflineUseCase()
        authLauncher = FakeAuthLauncher()
        viewModel = LoginViewModel(
            signInWithGoogleUseCase = signInUseCase,
            continueOfflineUseCase = continueOfflineUseCase,
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
    fun `google sign in success navigates to collection`() = runTest {
        authLauncher.launchResult = Result.success("token")
        signInUseCase.result = Result.success(Unit)

        var effect: LoginEffect? = null
        val collectJob = launch { effect = viewModel.effect.first() }

        viewModel.processIntent(LoginIntent.OnGoogleSignInClicked)
        advanceUntilIdle()
        collectJob.join()

        assertFalse(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.errorMessage)
        assertTrue(effect is LoginEffect.NavigateToCollection)
    }

    @Test
    fun `google sign in token failure shows error`() = runTest {
        authLauncher.launchResult = Result.failure(RuntimeException("Google error"))

        viewModel.processIntent(LoginIntent.OnGoogleSignInClicked)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals("Google error", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `google sign in auth failure shows error`() = runTest {
        authLauncher.launchResult = Result.success("token")
        signInUseCase.result = Result.failure(RuntimeException("Auth error"))

        viewModel.processIntent(LoginIntent.OnGoogleSignInClicked)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals("Auth error", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `continue offline navigates to collection`() = runTest {
        var effect: LoginEffect? = null
        val collectJob = launch { effect = viewModel.effect.first() }

        viewModel.processIntent(LoginIntent.OnContinueOfflineClicked)
        advanceUntilIdle()
        collectJob.join()

        assertTrue(effect is LoginEffect.NavigateToCollection)
    }

    @Test
    fun `dismiss error clears error message`() = runTest {
        authLauncher.launchResult = Result.failure(RuntimeException("Error"))
        viewModel.processIntent(LoginIntent.OnGoogleSignInClicked)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.errorMessage != null)

        viewModel.processIntent(LoginIntent.DismissError)
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.errorMessage)
    }
}
