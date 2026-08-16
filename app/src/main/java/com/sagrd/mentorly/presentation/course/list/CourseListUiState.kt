package com.sagrd.mentorly.presentation.course.list

import com.sagrd.mentorly.domain.model.course.Course
import com.sagrd.mentorly.domain.model.enrollment.Enrollment

data class CourseListUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val searchQuery: String = "",
    val studentName: String = "",
    val userPhotoUrl: String? = null,
    val activeEnrollments: List<Enrollment> = emptyList(),
    val filteredEnrollments: List<Enrollment> = emptyList(),
    val enrollmentProgressMap: Map<String, Int> = emptyMap(),
    val courses: List<Course> = emptyList(),
    val filteredCourses: List<Course> = emptyList(),
    val errorMessage: String? = null,
    val hasSession: Boolean = true
)
