package com.sagrd.mentorly.presentation.admin.course.list

import com.sagrd.mentorly.domain.model.course.Course

data class AdminCourseListUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val courses: List<Course> = emptyList(),
    val deletingCourseId: String? = null,
    val publishingCourseId: String? = null,
    val errorMessage: String? = null,
    val hasSession: Boolean = true,
    val hasAdminAccess: Boolean = true,
    val searchQuery: String = "",
)
