package com.sagrd.mentorly.presentation.admin.quiz

import com.sagrd.mentorly.domain.model.quiz.AdminQuizQuestion

data class AdminQuizQuestionUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isDeleting: Boolean = false,
    val editingQuestionId: String? = null,
    val question: String = "",
    val correctAnswer: String = "",
    val orderIndex: String = "0",
    val questionError: String? = null,
    val correctAnswerError: String? = null,
    val orderError: String? = null,
    val errorMessage: String? = null,
    val isQuestionSaved: Boolean = false,
    val questions: List<AdminQuizQuestion> = emptyList(),
    val hasAdminAccess: Boolean = true,
    val hasSession: Boolean = true,
)
