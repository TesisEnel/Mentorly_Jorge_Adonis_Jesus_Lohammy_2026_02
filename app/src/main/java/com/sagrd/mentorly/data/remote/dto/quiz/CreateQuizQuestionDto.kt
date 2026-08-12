package com.sagrd.mentorly.data.remote.dto.quiz

data class CreateQuizQuestionDto(
    val prompt: String,
    val correctAnswer: String,
    val orderIndex: Int
)
