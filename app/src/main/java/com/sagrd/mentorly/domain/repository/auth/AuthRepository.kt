package com.sagrd.mentorly.domain.repository.auth

import android.content.Context
import com.sagrd.mentorly.domain.model.auth.AuthUser

interface AuthRepository {
    suspend fun signInWithGoogle(context: Context): Result<AuthUser>

    suspend fun signOut()

    fun getCurrentUser(): AuthUser?
}