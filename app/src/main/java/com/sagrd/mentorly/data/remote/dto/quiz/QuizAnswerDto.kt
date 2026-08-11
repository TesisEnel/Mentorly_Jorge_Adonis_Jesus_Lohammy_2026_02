package com.sagrd.mentorly.data.remote.dto.quiz

import com.sagrd.mentorly.domain.model.quiz.QuizAnswer

data class QuizAnswerDto(
    val questionId: String,
    val answer: String
) {
    fun toDomain() = QuizAnswer(
        questionId = questionId,
        answer = answer
    )
}
