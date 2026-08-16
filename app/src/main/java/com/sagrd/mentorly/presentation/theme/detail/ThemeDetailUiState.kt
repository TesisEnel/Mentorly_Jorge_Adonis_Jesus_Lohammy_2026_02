package com.sagrd.mentorly.presentation.theme.detail

import com.sagrd.mentorly.domain.model.progress.EnrollmentThemeProgress

data class ThemeDetailUiState(
    val isLoading: Boolean = false,
    val isCompleting: Boolean = false,
    val unitTitle: String? = null,
    val unitOrderIndex: Int = 1,
    val theme: EnrollmentThemeProgress? = null,
    val isCompleted: Boolean = false,
    val errorMessage: String? = null
)
