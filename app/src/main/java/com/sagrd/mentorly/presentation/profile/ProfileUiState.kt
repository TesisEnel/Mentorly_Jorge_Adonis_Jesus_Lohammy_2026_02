package com.sagrd.mentorly.presentation.profile

import com.sagrd.mentorly.domain.model.enrollment.Certificate
import com.sagrd.mentorly.domain.model.enrollment.Enrollment
import com.sagrd.mentorly.domain.model.student.Student
import com.sagrd.mentorly.domain.model.student.StudentStatistics

data class ProfileUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val student: Student? = null,
    val userPhotoUrl: String? = null,
    val statistics: StudentStatistics? = null,
    val peerReviewsCount: Int = 0,
    val certificatesCount: Int = 0,
    val completedEnrollments: List<Enrollment> = emptyList(),
    val isCertificatesListDialogVisible: Boolean = false,
    val selectedCertificateEnrollment: Enrollment? = null,
    val selectedCertificate: Certificate? = null,
    val isEditDialogVisible: Boolean = false,
    val editedDisplayName: String = "",
    val editedEmail: String = "",
    val isSignOutDialogVisible: Boolean = false,
    val isSignedOut: Boolean = false,
    val errorMessage: String? = null
)