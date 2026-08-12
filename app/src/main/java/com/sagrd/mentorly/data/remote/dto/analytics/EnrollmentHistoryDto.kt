package com.sagrd.mentorly.data.remote.dto.analytics

import com.sagrd.mentorly.domain.model.analytics.EnrollmentHistory

data class EnrollmentHistoryDto(
    val enrollmentId: String,
    val studentName: String,
    val courseTitle: String,
    val status: String,
    val attemptNumber: Int,
    val startedAt: String,
    val updatedAt: String
) {
    fun toDomain() = EnrollmentHistory(
        enrollmentId = enrollmentId,
        studentName = studentName,
        courseTitle = courseTitle,
        status = status,
        attemptNumber = attemptNumber,
        startedAt = startedAt,
        updatedAt = updatedAt
    )
}
