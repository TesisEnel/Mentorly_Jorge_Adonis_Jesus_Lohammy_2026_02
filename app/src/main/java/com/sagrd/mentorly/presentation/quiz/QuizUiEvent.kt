package com.sagrd.mentorly.presentation.quiz

sealed interface QuizUiEvent {
    data object LoadQuiz : QuizUiEvent
    data class QuestionIndexChanged(val index: Int) : QuizUiEvent
    data class AnswerChanged(
        val questionId: String,
        val answer: String
    ) : QuizUiEvent
    data object SubmitQuiz : QuizUiEvent
    data object Retry : QuizUiEvent
    data object ClearError : QuizUiEvent
}
