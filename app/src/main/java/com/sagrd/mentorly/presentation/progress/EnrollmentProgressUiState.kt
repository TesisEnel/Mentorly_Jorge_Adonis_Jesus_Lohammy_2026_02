package com.sagrd.mentorly.presentation.progress

import com.sagrd.mentorly.domain.model.progress.EnrollmentProgress

data class EnrollmentProgressUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val progress: EnrollmentProgress? = null,
    val completingThemeIds: Set<String> = emptySet(),
    val errorMessage: String? = null
)
