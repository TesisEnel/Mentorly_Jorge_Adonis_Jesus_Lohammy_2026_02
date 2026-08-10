package com.sagrd.mentorly.data.remote.dto.enrollment

import com.sagrd.mentorly.domain.model.enrollment.Enrollment
import com.sagrd.mentorly.domain.model.enrollment.EnrollmentStatus

data class EnrollmentDto(
    val id: String,
    val studentId: String,
    val courseId: String,
    val attemptNumber: Int,
    val startedAt: String,
    val expiresAt: String,
    val completedAt: String?,
    val status: Int
) {
    fun toDomain() = Enrollment(
        id = id,
        studentId = studentId,
        courseId = courseId,
        attemptNumber = attemptNumber,
        startedAt = startedAt,
        expiresAt = expiresAt,
        completedAt = completedAt,
        status = EnrollmentStatus.fromApi(status)
    )
}
