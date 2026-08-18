package com.sagrd.mentorly.presentation.admin.student.detail

import com.sagrd.mentorly.domain.model.enrollment.Enrollment
import com.sagrd.mentorly.domain.model.progress.EnrollmentProgress
import com.sagrd.mentorly.domain.model.student.Student

data class AdminStudentDetailUiState(
    val isLoading: Boolean = false,
    val student: Student? = null,
    val enrollments: List<Enrollment> = emptyList(),
    val enrollmentProgress: Map<String, EnrollmentProgress> = emptyMap(),
    val expandedEnrollmentIds: Set<String> = emptySet(),
    val errorMessage: String? = null,
    val hasSession: Boolean = true,
    val hasAdminAccess: Boolean = true
)
