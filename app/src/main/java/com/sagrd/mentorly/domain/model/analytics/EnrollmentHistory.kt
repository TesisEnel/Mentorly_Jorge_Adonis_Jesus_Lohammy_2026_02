package com.sagrd.mentorly.domain.model.analytics

data class EnrollmentHistory(
    val enrollmentId: String,
    val studentName: String,
    val courseTitle: String,
    val status: String,
    val attemptNumber: Int,
    val startedAt: String,
    val updatedAt: String
)
