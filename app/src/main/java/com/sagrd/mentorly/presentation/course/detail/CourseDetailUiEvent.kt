package com.sagrd.mentorly.presentation.course.detail

sealed interface CourseDetailUiEvent {
    data class LoadCourseContent(
        val courseId: String
    ) : CourseDetailUiEvent

    data object Retry : CourseDetailUiEvent
    data object Enroll : CourseDetailUiEvent
    data object ClearEnrollmentError : CourseDetailUiEvent
}
