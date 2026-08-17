package com.sagrd.mentorly.data.remote.dto.quiz

import com.sagrd.mentorly.domain.model.quiz.AdminQuizQuestion

data class AdminQuizQuestionDto(
    val id: String,
    val prompt: String,
    val correctAnswer: String,
    val orderIndex: Int
) {
    fun toDomain() = AdminQuizQuestion(
        id = id,
        prompt = prompt,
        correctAnswer = correctAnswer,
        orderIndex = orderIndex
    )
}
