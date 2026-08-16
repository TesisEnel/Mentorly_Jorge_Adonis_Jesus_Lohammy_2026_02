package com.sagrd.mentorly.presentation.home

import com.sagrd.mentorly.domain.model.course.Course
import com.sagrd.mentorly.domain.model.enrollment.Enrollment

data class HomeUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val studentName: String = "",
    val userPhotoUrl: String? = null,
    val activeEnrollments: List<Enrollment> = emptyList(),
    val enrollmentProgressMap: Map<String, Int> = emptyMap(),
    val publishedCourses: List<Course> = emptyList(),
    val errorMessage: String? = null,
    val hasSession: Boolean = true
)
