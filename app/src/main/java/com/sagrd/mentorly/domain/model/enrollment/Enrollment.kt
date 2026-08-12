package com.sagrd.mentorly.domain.model.enrollment

data class Enrollment(
    val id: String,
    val studentId: String,
    val courseId: String,
    val attemptNumber: Int,
    val startedAt: String,
    val expiresAt: String,
    val completedAt: String?,
    val status: EnrollmentStatus
)
