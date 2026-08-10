package com.sagrd.mentorly.data.remote.dto.enrollment

import com.sagrd.mentorly.domain.model.enrollment.EnrollmentStatus

data class EnrollmentStatusDto(
    val status: Int
) {
    fun toDomain() = EnrollmentStatus.fromApi(status)
}
