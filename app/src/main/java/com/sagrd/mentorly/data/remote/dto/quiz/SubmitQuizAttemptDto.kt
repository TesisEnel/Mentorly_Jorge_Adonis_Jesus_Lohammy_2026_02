package com.sagrd.mentorly.data.remote.dto.quiz

data class SubmitQuizAttemptDto(
    val studentId: String,
    val answers: List<QuizAnswerDto>
)
