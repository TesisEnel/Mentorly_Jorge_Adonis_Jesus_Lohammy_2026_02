package com.sagrd.mentorly.domain.model.progress

import com.sagrd.mentorly.domain.model.content.ActivityType

data class EnrollmentActivityProgress(
    val activityId: String,
    val title: String,
    val description: String = "",
    val isMandatory: Boolean,
    val isApproved: Boolean,
    val type: ActivityType = ActivityType.EXERCISE
)
