package com.sagrd.mentorly.domain.model.quiz

data class QuizAttempt(
    val id: String,
    val score: Double,
    val passed: Boolean,
    val submittedAtUtc: String
)
