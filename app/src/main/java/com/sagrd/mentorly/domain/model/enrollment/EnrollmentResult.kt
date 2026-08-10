package com.sagrd.mentorly.domain.model.enrollment

data class EnrollmentResult(
    val enrollmentId: String,
    val attemptNumber: Int,
    val startedAt: String,
    val expiresAt: String,
    val status: EnrollmentStatus
)
