package com.sagrd.mentorly.presentation.admin.student.list

import com.sagrd.mentorly.domain.model.student.Student

data class AdminStudentListUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val students: List<Student> = emptyList(),
    val searchQuery: String = "",
    val promotingStudentId: String? = null,
    val studentPendingPromotion: Student? = null,
    val successMessage: String? = null,
    val errorMessage: String? = null,
    val hasSession: Boolean = true,
    val hasAdminAccess: Boolean = true,
    val currentAdminId: String? = null
)
