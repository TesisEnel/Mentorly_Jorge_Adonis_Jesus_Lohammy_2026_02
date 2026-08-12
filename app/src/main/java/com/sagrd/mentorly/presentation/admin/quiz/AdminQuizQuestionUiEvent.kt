package com.sagrd.mentorly.presentation.admin.quiz

sealed interface AdminQuizQuestionUiEvent {
    data class QuestionChanged(val value: String) : AdminQuizQuestionUiEvent

    data class CorrectAnswerChanged(val value: String) : AdminQuizQuestionUiEvent

    data object SaveQuestion : AdminQuizQuestionUiEvent

    data object ClearError : AdminQuizQuestionUiEvent

    data object ClearCreatedState : AdminQuizQuestionUiEvent
}
