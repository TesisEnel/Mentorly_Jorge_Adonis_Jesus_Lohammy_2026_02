package com.sagrd.mentorly.data.repository.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.sagrd.mentorly.R
import com.sagrd.mentorly.domain.model.auth.AuthUser
import com.sagrd.mentorly.domain.repository.auth.AuthRepository
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val credentialManager: CredentialManager
) : AuthRepository {

    override suspend fun signInWithGoogle(context: Context): Result<AuthUser> {
        return try {
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(context.getString(R.string.web_client_id))
                .setAutoSelectEnabled(true)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val credential = credentialManager
                .getCredential(context, request)
                .credential

            if (
                credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleCredential = GoogleIdTokenCredential.createFrom(credential.data)

                val firebaseCredential = GoogleAuthProvider.getCredential(
                    googleCredential.idToken,
                    null
                )

                val firebaseUser = auth
                    .signInWithCredential(firebaseCredential)
                    .await()
                    .user
                    ?: return Result.failure(
                        Exception("No se pudo obtener el usuario autenticado.")
                    )

                Result.success(firebaseUser.toAuthUser())
            } else {
                Result.failure(Exception("La credencial de Google no es válida."))
            }
        } catch (exception: Exception) {
            Result.failure(
                Exception(
                    exception.message ?: "No se pudo iniciar sesión con Google.",
                    exception
                )
            )
        }
    }

    override suspend fun signOut() {
        auth.signOut()
    }

    override fun getCurrentUser(): AuthUser? {
        return auth.currentUser?.toAuthUser()
    }

    private fun FirebaseUser.toAuthUser(): AuthUser {
        return AuthUser(
            uid = uid,
            email = email,
            displayName = displayName,
            photoUrl = photoUrl?.toString()
        )
    }
}