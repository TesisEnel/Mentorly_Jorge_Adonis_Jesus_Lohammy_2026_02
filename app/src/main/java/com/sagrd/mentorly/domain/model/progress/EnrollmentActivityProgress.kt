package com.sagrd.mentorly.domain.model.progress

data class EnrollmentActivityProgress(
    val activityId: String,
    val isMandatory: Boolean,
    val isApproved: Boolean,
    val isPending: Boolean,
    val isBlocked: Boolean
)
