package com.sagrd.mentorly.data.remote.dto.progress

import com.sagrd.mentorly.domain.model.progress.EnrollmentActivityProgress

data class EnrollmentActivityProgressDto(
    val activityId: String,
    val title: String,
    val isMandatory: Boolean,
    val isApproved: Boolean
) {
    fun toDomain() = EnrollmentActivityProgress(
        activityId = activityId,
        title = title,
        isMandatory = isMandatory,
        isApproved = isApproved
    )
}
