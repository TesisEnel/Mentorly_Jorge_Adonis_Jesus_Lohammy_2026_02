package com.sagrd.mentorly.presentation.admin.quiz

sealed interface AdminQuizQuestionUiEvent {
    data class Load(val activityId: String) : AdminQuizQuestionUiEvent

    data class QuestionChanged(val value: String) : AdminQuizQuestionUiEvent

    data class CorrectAnswerChanged(val value: String) : AdminQuizQuestionUiEvent

    data class OrderChanged(val value: String) : AdminQuizQuestionUiEvent

    data class EditQuestion(val questionId: String) : AdminQuizQuestionUiEvent

    data object SaveQuestion : AdminQuizQuestionUiEvent

    data object CancelEdit : AdminQuizQuestionUiEvent

    data object DeleteQuestion : AdminQuizQuestionUiEvent

    data object ClearError : AdminQuizQuestionUiEvent

    data object QuestionSavedHandled : AdminQuizQuestionUiEvent
}
