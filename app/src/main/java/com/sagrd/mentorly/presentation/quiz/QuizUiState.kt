package com.sagrd.mentorly.presentation.quiz

import com.sagrd.mentorly.domain.model.quiz.QuizAttempt
import com.sagrd.mentorly.domain.model.quiz.QuizQuestion

data class QuizUiState(
    val isLoading: Boolean = true,
    val questions: List<QuizQuestion> = emptyList(),
    val answers: Map<String, String> = emptyMap(),
    val isSubmitting: Boolean = false,
    val result: QuizAttempt? = null,
    val errorMessage: String? = null,
    val isSubmitted: Boolean = false
)
