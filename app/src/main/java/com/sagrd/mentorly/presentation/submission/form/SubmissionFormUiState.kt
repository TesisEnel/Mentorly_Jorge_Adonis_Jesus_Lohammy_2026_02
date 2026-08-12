package com.sagrd.mentorly.presentation.submission.form

data class SubmissionFormUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isEditing: Boolean = false,
    val evidenceUrl: String = "",
    val evidenceUrlError: String? = null,
    val errorMessage: String? = null,
    val isSaved: Boolean = false
)