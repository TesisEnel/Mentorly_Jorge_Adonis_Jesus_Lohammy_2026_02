package com.sagrd.mentorly.domain.model.quiz

data class AdminQuizQuestion(
    val id: String,
    val prompt: String,
    val correctAnswer: String,
    val orderIndex: Int
)
