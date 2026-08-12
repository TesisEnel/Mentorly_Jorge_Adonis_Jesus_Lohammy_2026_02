package com.sagrd.mentorly.presentation.enrollment.detail

import com.sagrd.mentorly.domain.model.enrollment.Certificate
import com.sagrd.mentorly.domain.model.enrollment.Enrollment
import com.sagrd.mentorly.domain.model.enrollment.EnrollmentStatus

data class EnrollmentDetailUiState(
    val isLoading: Boolean = false,
    val isRestarting: Boolean = false,
    val isLoadingCertificate: Boolean = false,
    val enrollment: Enrollment? = null,
    val currentStatus: EnrollmentStatus? = null,
    val certificate: Certificate? = null,
    val isRestartConfirmationVisible: Boolean = false,
    val restartedEnrollmentId: String? = null,
    val errorMessage: String? = null,
    val hasSession: Boolean = true
)
