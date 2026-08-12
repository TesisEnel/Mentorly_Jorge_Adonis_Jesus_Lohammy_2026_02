package com.sagrd.mentorly.domain.model.analytics

data class EnrollmentHistory(
    val enrollmentId: String,
    val studentId: String,
    val attemptNumber: Int,
    val status: Int,
    val startedAtUtc: String,
    val expiresAtUtc: String,
    val completedAtUtc: String?
)
