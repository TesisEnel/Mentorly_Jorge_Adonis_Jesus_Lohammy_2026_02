package com.sagrd.mentorly.data.remote.dto.progress

import com.sagrd.mentorly.domain.model.content.ActivityType
import com.sagrd.mentorly.domain.model.progress.EnrollmentActivityProgress

data class EnrollmentActivityProgressDto(
    val activityId: String,
    val title: String,
    val type: Int,
    val isMandatory: Boolean,
    val isApproved: Boolean
) {
    fun toDomain() = EnrollmentActivityProgress(
        activityId = activityId,
        title = title,
        type = ActivityType.fromApi(type),
        isMandatory = isMandatory,
        isApproved = isApproved
    )
}
