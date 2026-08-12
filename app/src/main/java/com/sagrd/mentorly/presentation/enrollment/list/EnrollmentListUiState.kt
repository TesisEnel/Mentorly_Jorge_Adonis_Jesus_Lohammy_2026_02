package com.sagrd.mentorly.presentation.enrollment.list

import com.sagrd.mentorly.domain.model.enrollment.Enrollment

data class EnrollmentListUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val enrollments: List<Enrollment> = emptyList(),
    val errorMessage: String? = null,
    val hasSession: Boolean = true
)
