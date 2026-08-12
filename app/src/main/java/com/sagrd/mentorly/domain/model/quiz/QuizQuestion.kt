package com.sagrd.mentorly.domain.model.quiz

data class QuizQuestion(
    val id: String,
    val prompt: String,
    val orderIndex: Int
)
