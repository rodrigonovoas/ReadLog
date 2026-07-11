package com.rodrigonovoa.readlog.data.repository

import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.rodrigonovoa.readlog.data.mapper.UserDataMapperImpl
import com.rodrigonovoa.readlog.domain.model.User
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AuthRepositoryImplTest {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var repository: AuthRepositoryImpl

    @Before
    fun setup() {
        firebaseAuth = mockk(relaxed = true)
        repository = AuthRepositoryImpl(firebaseAuth, UserDataMapperImpl())
        mockkStatic(GoogleAuthProvider::class)
    }

    @After
    fun tearDown() {
        unmockkStatic(GoogleAuthProvider::class)
    }

    @Test
    fun `signInWithGoogle returns success when task succeeds`() = runTest {
        val credential = mockk<com.google.firebase.auth.AuthCredential>(relaxed = true)
        every { GoogleAuthProvider.getCredential("token", null) } returns credential

        val task = mockk<Task<AuthResult>>()
        every { task.addOnSuccessListener(any()) } answers {
            val listener = firstArg<OnSuccessListener<AuthResult>>()
            listener.onSuccess(mockk())
            task
        }
        every { task.addOnFailureListener(any()) } returns task
        every { firebaseAuth.signInWithCredential(credential) } returns task

        val result = repository.signInWithGoogle("token")

        assertTrue(result.isSuccess)
    }

    @Test
    fun `signInWithGoogle returns failure when task fails`() = runTest {
        val exception = RuntimeException("Auth failed")
        val credential = mockk<com.google.firebase.auth.AuthCredential>(relaxed = true)
        every { GoogleAuthProvider.getCredential("token", null) } returns credential

        val task = mockk<Task<AuthResult>>()
        every { task.addOnFailureListener(any()) } answers {
            val listener = firstArg<OnFailureListener>()
            listener.onFailure(exception)
            task
        }
        every { task.addOnSuccessListener(any()) } returns task
        every { firebaseAuth.signInWithCredential(credential) } returns task

        val result = repository.signInWithGoogle("token")

        assertTrue(result.isFailure)
        assertEquals("Auth failed", result.exceptionOrNull()?.message)
    }

    @Test
    fun `continueOffline returns success`() = runTest {
        val result = repository.continueOffline()

        assertTrue(result.isSuccess)
    }

    @Test
    fun `getCurrentUser returns mapped user when current user exists`() {
        val firebaseUser = mockk<FirebaseUser>()
        every { firebaseUser.uid } returns "uid123"
        every { firebaseUser.email } returns "test@example.com"
        every { firebaseUser.displayName } returns "Test User"
        every { firebaseAuth.currentUser } returns firebaseUser

        val user = repository.getCurrentUser()

        assertEquals(User("uid123", "test@example.com", "Test User"), user)
    }

    @Test
    fun `getCurrentUser returns null when no current user`() {
        every { firebaseAuth.currentUser } returns null

        val user = repository.getCurrentUser()

        assertNull(user)
    }

    @Test
    fun `isUserSignedIn returns true when current user is not null`() {
        every { firebaseAuth.currentUser } returns mockk()

        assertTrue(repository.isUserSignedIn())
    }

    @Test
    fun `isUserSignedIn returns false when current user is null`() {
        every { firebaseAuth.currentUser } returns null

        assertFalse(repository.isUserSignedIn())
    }

    @Test
    fun `signOut calls firebaseAuth signOut`() = runTest {
        repository.signOut()

        verify { firebaseAuth.signOut() }
    }
}
