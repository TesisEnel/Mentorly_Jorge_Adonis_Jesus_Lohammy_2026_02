package com.sagrd.mentorly.data.remote.dto.progress

import com.sagrd.mentorly.domain.model.progress.EnrollmentActivityProgress

data class EnrollmentActivityProgressDto(
    val activityId: String,
    val isMandatory: Boolean,
    val isApproved: Boolean,
    val isPending: Boolean,
    val isBlocked: Boolean
) {
    fun toDomain() = EnrollmentActivityProgress(
        activityId = activityId,
        isMandatory = isMandatory,
        isApproved = isApproved,
        isPending = isPending,
        isBlocked = isBlocked
    )
}
