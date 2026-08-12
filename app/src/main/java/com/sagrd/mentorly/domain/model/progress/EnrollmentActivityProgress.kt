package com.sagrd.mentorly.domain.model.progress

data class EnrollmentActivityProgress(
    val activityId: String,
    val title: String,
    val isMandatory: Boolean,
    val isApproved: Boolean
)
