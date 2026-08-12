package com.sagrd.mentorly.presentation.auth

import android.content.Context

interface LoginUiEvent {
    data class SignInWithGoogle(
        val context: Context
    ): LoginUiEvent

    data object SignOut: LoginUiEvent

    data object LoginCompletedHandled : LoginUiEvent
}
