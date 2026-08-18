package com.sagrd.mentorly.data.repository.auth

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.credentials.CredentialManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class AuthRepositoryImplTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var repository: AuthRepositoryImpl
    private lateinit var auth: FirebaseAuth
    private lateinit var credentialManager: CredentialManager

    @Before
    fun setup() {
        auth = mockk(relaxed = true)
        credentialManager = mockk(relaxed = true)
        repository = AuthRepositoryImpl(auth, credentialManager)
    }

    @Test
    fun `signOut cierra sesion correctamente`() = runTest {
        // When
        repository.signOut()

        // Then
        verify { auth.signOut() }
    }

    @Test
    fun `getCurrentUser retorna usuario autenticado correctamente`() {
        // Given
        val firebaseUser = mockk<FirebaseUser>(relaxed = true)
        every { firebaseUser.uid } returns "user-123"
        every { firebaseUser.email } returns "test@example.com"
        every { firebaseUser.displayName } returns "Test User"
        every { firebaseUser.photoUrl } returns null

        every { auth.currentUser } returns firebaseUser

        // When
        val result = repository.getCurrentUser()

        // Then
        assertNotNull(result)
        assertEquals("user-123", result?.uid)
        assertEquals("test@example.com", result?.email)
        assertEquals("Test User", result?.displayName)
        verify { auth.currentUser }
    }

    @Test
    fun `getCurrentUser retorna null cuando no hay usuario autenticado`() {
        // Given
        every { auth.currentUser } returns null

        // When
        val result = repository.getCurrentUser()

        // Then
        assertNull(result)
        verify { auth.currentUser }
    }
}
