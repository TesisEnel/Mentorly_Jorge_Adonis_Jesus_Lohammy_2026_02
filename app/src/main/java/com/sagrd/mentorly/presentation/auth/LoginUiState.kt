package com.sagrd.mentorly.presentation.auth

import com.sagrd.mentorly.domain.model.student.Student

data class LoginUiState (
    val isLoading: Boolean = false,
    val student: Student? = null,
    val errorMessage: String? = null
)
