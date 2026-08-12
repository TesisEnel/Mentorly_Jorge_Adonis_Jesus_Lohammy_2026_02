package com.sagrd.mentorly.presentation.course.list

import com.sagrd.mentorly.domain.model.course.Course

data class CourseListUiState(
    val isLoading: Boolean = false,
    val courses: List<Course> = emptyList(),
    val errorMessage: String? = null
)
