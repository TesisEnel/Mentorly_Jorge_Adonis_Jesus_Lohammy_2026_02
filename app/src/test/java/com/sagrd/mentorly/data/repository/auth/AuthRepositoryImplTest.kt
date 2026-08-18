package com.sagrd.mentorly.data.repository.auth

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.PasswordCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
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
    fun `signInWithGoogle retorna error cuando falla el credential manager`() = runTest {
        // Given
        val context = mockk<Context>(relaxed = true)
        coEvery {
            credentialManager.getCredential(any<Context>(), any<GetCredentialRequest>())
        } throws Exception("Error del gestor")

        // When
        val result = repository.signInWithGoogle(context)

        // Then
        assertTrue(result.isFailure)
        assertEquals("Error del gestor", result.exceptionOrNull()?.message)
    }

    @Test
    fun `signInWithGoogle retorna error cuando la credencial no es valida`() = runTest {
        // Given
        val context = mockk<Context>(relaxed = true)
        val getCredentialResponse = mockk<GetCredentialResponse>(relaxed = true)
        val credential = mockk<PasswordCredential>(relaxed = true)

        every { getCredentialResponse.credential } returns credential
        coEvery {
            credentialManager.getCredential(any<Context>(), any<GetCredentialRequest>())
        } returns getCredentialResponse

        // When
        val result = repository.signInWithGoogle(context)

        // Then
        assertTrue(result.isFailure)
        assertEquals("La credencial de Google no es válida.", result.exceptionOrNull()?.message)
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
