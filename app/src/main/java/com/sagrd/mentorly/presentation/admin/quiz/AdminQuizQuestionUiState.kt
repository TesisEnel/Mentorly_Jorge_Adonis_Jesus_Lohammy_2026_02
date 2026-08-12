package com.sagrd.mentorly.presentation.admin.quiz

data class AdminQuizQuestionUiState(
    val isSaving: Boolean = false,
    val question: String = "",
    val correctAnswer: String = "",
    val questionError: String? = null,
    val correctAnswerError: String? = null,
    val errorMessage: String? = null,
    val isQuestionCreated: Boolean = false,
    val hasAdminAccess: Boolean = false,
    val hasSession: Boolean = false,
)
