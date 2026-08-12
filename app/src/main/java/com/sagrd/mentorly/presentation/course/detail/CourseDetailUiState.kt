package com.sagrd.mentorly.presentation.course.detail

import com.sagrd.mentorly.domain.model.course.Course

data class CourseDetailUiState(
    val isLoading: Boolean = false,
    val course: Course? = null,
    val errorMessage: String? = null
)
