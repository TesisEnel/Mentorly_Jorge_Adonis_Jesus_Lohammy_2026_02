package com.sagrd.mentorly.data.remote.dto.analytics

import com.sagrd.mentorly.domain.model.analytics.PeerReviewBottleneck

data class PeerReviewBottleneckDto(
    val activityId: String,
    val activityTitle: String,
    val courseTitle: String,
    val pendingSubmissionsCount: Int,
    val averageWaitTimeHours: Double
) {
    fun toDomain() = PeerReviewBottleneck(
        activityId = activityId,
        activityTitle = activityTitle,
        courseTitle = courseTitle,
        pendingSubmissionsCount = pendingSubmissionsCount,
        averageWaitTimeHours = averageWaitTimeHours
    )
}
