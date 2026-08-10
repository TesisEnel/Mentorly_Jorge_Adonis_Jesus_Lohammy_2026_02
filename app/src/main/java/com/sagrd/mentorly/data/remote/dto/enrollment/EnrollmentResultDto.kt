package com.sagrd.mentorly.data.remote.dto.enrollment

import com.sagrd.mentorly.domain.model.enrollment.EnrollmentResult
import com.sagrd.mentorly.domain.model.enrollment.EnrollmentStatus

data class EnrollmentResultDto(
    val enrollmentId: String,
    val attemptNumber: Int,
    val startedAt: String,
    val expiresAt: String,
    val status: Int
) {
    fun toDomain() = EnrollmentResult(
        enrollmentId = enrollmentId,
        attemptNumber = attemptNumber,
        startedAt = startedAt,
        expiresAt = expiresAt,
        status = EnrollmentStatus.fromApi(status)
    )
}
