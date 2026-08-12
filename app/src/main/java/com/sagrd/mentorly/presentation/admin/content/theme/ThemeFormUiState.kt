package com.sagrd.mentorly.presentation.admin.content.theme

data class ThemeFormUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isEditMode: Boolean = false,
    val title: String = "",
    val contentText: String = "",
    val orderIndex: String = "0",
    val errorMessage: String? = null,
    val fieldErrors: Map<String, String> = emptyMap(),
    val isSaved: Boolean = false,
    val hasAdminAccess: Boolean = true
)
