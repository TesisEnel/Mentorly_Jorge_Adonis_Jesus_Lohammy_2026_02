package com.sagrd.mentorly.data.remote.dto.quiz

import com.sagrd.mentorly.domain.model.quiz.QuizQuestion

data class QuizQuestionDto(
    val id: String,
    val prompt: String,
    val orderIndex: Int
) {
    fun toDomain() = QuizQuestion(
        id = id,
        prompt = prompt,
        orderIndex = orderIndex
    )
}
