package com.sagrd.mentorly.data.remote.dto.quiz

import com.sagrd.mentorly.domain.model.quiz.QuizAttempt

data class QuizAttemptDto(
    val id: String,
    val score: Double,
    val passed: Boolean,
    val submittedAtUtc: String
) {
    fun toDomain() = QuizAttempt(
        id = id,
        score = score,
        passed = passed,
        submittedAtUtc = submittedAtUtc
    )
}
