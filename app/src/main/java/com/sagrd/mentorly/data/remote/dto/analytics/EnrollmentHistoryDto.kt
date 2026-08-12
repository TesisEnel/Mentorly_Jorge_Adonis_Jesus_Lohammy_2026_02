package com.sagrd.mentorly.data.remote.dto.analytics

import com.sagrd.mentorly.domain.model.analytics.EnrollmentHistory

data class EnrollmentHistoryDto(
    val enrollmentId: String,
    val studentId: String,
    val attemptNumber: Int,
    val status: Int,
    val startedAtUtc: String,
    val expiresAtUtc: String,
    val completedAtUtc: String?
) {
    fun toDomain() = EnrollmentHistory(
        enrollmentId = enrollmentId,
        studentId = studentId,
        attemptNumber = attemptNumber,
        status = status,
        startedAtUtc = startedAtUtc,
        expiresAtUtc = expiresAtUtc,
        completedAtUtc = completedAtUtc
    )
}
