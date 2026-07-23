package com.rodrigonovoa.readlog.ui.login

import com.rodrigonovoa.readlog.domain.auth.AuthLauncher
import com.rodrigonovoa.readlog.domain.model.User
import com.rodrigonovoa.readlog.domain.model.UserProfileInfo
import com.rodrigonovoa.readlog.domain.usecase.ClaimUsernameResult
import com.rodrigonovoa.readlog.domain.usecase.ClaimUsernameUseCase
import com.rodrigonovoa.readlog.domain.usecase.ContinueOfflineUseCase
import com.rodrigonovoa.readlog.domain.usecase.GetCurrentUserUseCase
import com.rodrigonovoa.readlog.domain.usecase.IsOnlineUseCase
import com.rodrigonovoa.readlog.domain.usecase.RequireUsernameSetupUseCase
import com.rodrigonovoa.readlog.domain.usecase.SignInWithGoogleUseCase
import com.rodrigonovoa.readlog.ui.R
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
    private lateinit var authLauncher: AuthLauncher
    private lateinit var getCurrentUserUseCase: GetCurrentUserUseCase
    private lateinit var isOnlineUseCase: IsOnlineUseCase
    private lateinit var requireUsernameSetupUseCase: RequireUsernameSetupUseCase
    private lateinit var claimUsernameUseCase: ClaimUsernameUseCase
    private lateinit var viewModel: LoginViewModel

    private val currentUser = User(uid = "uid1", email = "elena@example.com", displayName = "Elena Marin")

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        signInUseCase = mockk()
        continueOfflineUseCase = mockk()
        authLauncher = mockk()
        getCurrentUserUseCase = mockk()
        isOnlineUseCase = mockk()
        requireUsernameSetupUseCase = mockk()
        claimUsernameUseCase = mockk()
        viewModel = LoginViewModel(
            signInWithGoogleUseCase = signInUseCase,
            continueOfflineUseCase = continueOfflineUseCase,
            authLauncher = authLauncher,
            getCurrentUserUseCase = getCurrentUserUseCase,
            isOnlineUseCase = isOnlineUseCase,
            requireUsernameSetupUseCase = requireUsernameSetupUseCase,
            claimUsernameUseCase = claimUsernameUseCase,
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
        assertNull(state.usernameSetup)
    }

    @Test
    fun `google sign in success without remote username shows the username setup dialog`() = runTest {
        coEvery { authLauncher.launchGoogleSignIn() } returns Result.success("token")
        coEvery { signInUseCase(any()) } returns Result.success(Unit)
        every { getCurrentUserUseCase() } returns currentUser
        every { isOnlineUseCase() } returns true
        coEvery {
            requireUsernameSetupUseCase("uid1", "elena@example.com", "Elena Marin")
        } returns "elena"

        viewModel.processIntent(LoginIntent.OnGoogleSignInClicked)
        advanceUntilIdle()

        val usernameSetup = viewModel.uiState.value.usernameSetup
        assertTrue(usernameSetup != null)
        assertEquals("elena", usernameSetup?.username)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `google sign in success with existing remote username navigates directly`() = runTest {
        coEvery { authLauncher.launchGoogleSignIn() } returns Result.success("token")
        coEvery { signInUseCase(any()) } returns Result.success(Unit)
        every { getCurrentUserUseCase() } returns currentUser
        every { isOnlineUseCase() } returns true
        coEvery {
            requireUsernameSetupUseCase("uid1", "elena@example.com", "Elena Marin")
        } returns null

        var effect: LoginEffect? = null
        val collectJob = launch { effect = viewModel.effect.first() }

        viewModel.processIntent(LoginIntent.OnGoogleSignInClicked)
        advanceUntilIdle()
        collectJob.join()

        assertNull(viewModel.uiState.value.usernameSetup)
        assertTrue(effect is LoginEffect.NavigateToCollection)
    }

    @Test
    fun `google sign in success while offline navigates directly without checking firestore`() = runTest {
        coEvery { authLauncher.launchGoogleSignIn() } returns Result.success("token")
        coEvery { signInUseCase(any()) } returns Result.success(Unit)
        every { getCurrentUserUseCase() } returns currentUser
        every { isOnlineUseCase() } returns false

        var effect: LoginEffect? = null
        val collectJob = launch { effect = viewModel.effect.first() }

        viewModel.processIntent(LoginIntent.OnGoogleSignInClicked)
        advanceUntilIdle()
        collectJob.join()

        assertNull(viewModel.uiState.value.usernameSetup)
        assertTrue(effect is LoginEffect.NavigateToCollection)
        coVerify(exactly = 0) { requireUsernameSetupUseCase(any(), any(), any()) }
    }

    @Test
    fun `confirming an invalid username shows a format error`() = runTest {
        coEvery { authLauncher.launchGoogleSignIn() } returns Result.success("token")
        coEvery { signInUseCase(any()) } returns Result.success(Unit)
        every { getCurrentUserUseCase() } returns currentUser
        every { isOnlineUseCase() } returns true
        coEvery {
            requireUsernameSetupUseCase("uid1", "elena@example.com", "Elena Marin")
        } returns "elena"
        viewModel.processIntent(LoginIntent.OnGoogleSignInClicked)
        advanceUntilIdle()
        coEvery { claimUsernameUseCase("uid1", "a") } returns ClaimUsernameResult.InvalidFormat

        viewModel.processIntent(LoginIntent.OnUsernameChanged("a"))
        viewModel.processIntent(LoginIntent.OnUsernameConfirmClicked)
        advanceUntilIdle()

        assertEquals(
            R.string.username_setup_error_invalid,
            viewModel.uiState.value.usernameSetup?.errorMessageRes,
        )
    }

    @Test
    fun `confirming a taken username shows an error and keeps the dialog open`() = runTest {
        coEvery { authLauncher.launchGoogleSignIn() } returns Result.success("token")
        coEvery { signInUseCase(any()) } returns Result.success(Unit)
        every { getCurrentUserUseCase() } returns currentUser
        every { isOnlineUseCase() } returns true
        coEvery {
            requireUsernameSetupUseCase("uid1", "elena@example.com", "Elena Marin")
        } returns "elena"
        viewModel.processIntent(LoginIntent.OnGoogleSignInClicked)
        advanceUntilIdle()
        coEvery { claimUsernameUseCase("uid1", "taken_name") } returns ClaimUsernameResult.AlreadyTaken

        viewModel.processIntent(LoginIntent.OnUsernameChanged("taken_name"))
        viewModel.processIntent(LoginIntent.OnUsernameConfirmClicked)
        advanceUntilIdle()

        val usernameSetup = viewModel.uiState.value.usernameSetup
        assertTrue(usernameSetup != null)
        assertEquals(R.string.username_setup_error_taken, usernameSetup?.errorMessageRes)
    }

    @Test
    fun `confirming an available username saves it and navigates to collection`() = runTest {
        coEvery { authLauncher.launchGoogleSignIn() } returns Result.success("token")
        coEvery { signInUseCase(any()) } returns Result.success(Unit)
        every { getCurrentUserUseCase() } returns currentUser
        every { isOnlineUseCase() } returns true
        coEvery {
            requireUsernameSetupUseCase("uid1", "elena@example.com", "Elena Marin")
        } returns "elena"
        viewModel.processIntent(LoginIntent.OnGoogleSignInClicked)
        advanceUntilIdle()
        coEvery { claimUsernameUseCase("uid1", "free_name") } returns ClaimUsernameResult.Success(
            UserProfileInfo(userId = "uid1", username = "free_name")
        )

        var effect: LoginEffect? = null
        val collectJob = launch { effect = viewModel.effect.first() }

        viewModel.processIntent(LoginIntent.OnUsernameChanged("free_name"))
        viewModel.processIntent(LoginIntent.OnUsernameConfirmClicked)
        advanceUntilIdle()
        collectJob.join()

        assertNull(viewModel.uiState.value.usernameSetup)
        assertTrue(effect is LoginEffect.NavigateToCollection)
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
