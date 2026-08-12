package com.sagrd.mentorly.presentation.admin.course.form

data class CourseFormUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isEditMode: Boolean = false,
    val title: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val requiredPeerReviews: String = "",
    val isPublished: Boolean = false,
    val errorMessage: String? = null,
    val fieldErrors: Map<String, String> = emptyMap(),
    val savedCourseId: String? = null,
    val hasAdminAccess: Boolean = true
)
