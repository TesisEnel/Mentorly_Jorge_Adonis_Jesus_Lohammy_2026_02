package com.sagrd.mentorly.presentation.admin.course.form

sealed interface CourseFormUiEvent {
    data class Load(val courseId: String?) : CourseFormUiEvent
    data class TitleChanged(val value: String) : CourseFormUiEvent
    data class DescriptionChanged(val value: String) : CourseFormUiEvent
    data class ImageUrlChanged(val value: String) : CourseFormUiEvent
    data class RequiredPeerReviewsChanged(val value: String) : CourseFormUiEvent
    data object Save : CourseFormUiEvent
    data object ClearError : CourseFormUiEvent
}
