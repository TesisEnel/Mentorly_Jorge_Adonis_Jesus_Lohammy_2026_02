package com.sagrd.mentorly.presentation.home

import com.sagrd.mentorly.domain.model.course.Course
import com.sagrd.mentorly.domain.model.enrollment.Enrollment

data class HomeUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val studentName: String = "",
    val activeEnrollments: List<Enrollment> = emptyList(),
    val publishedCourses: List<Course> = emptyList(),
    val errorMessage: String? = null,
    val hasSession: Boolean = true
)
