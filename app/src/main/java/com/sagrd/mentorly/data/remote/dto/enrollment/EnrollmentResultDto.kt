package com.sagrd.mentorly.data.remote.dto.enrollment

import com.sagrd.mentorly.domain.model.enrollment.EnrollmentResult
import com.sagrd.mentorly.domain.model.enrollment.EnrollmentStatus

data class EnrollmentResultDto(
    val enrollmentId: String,
    val attemptNumber: Int,
    val startedAtUtc: String,
    val expiresAtUtc: String,
    val status: Int
) {
    fun toDomain() = EnrollmentResult(
        enrollmentId = enrollmentId,
        attemptNumber = attemptNumber,
        startedAt = startedAtUtc,
        expiresAt = expiresAtUtc,
        status = EnrollmentStatus.fromApi(status)
    )
}
