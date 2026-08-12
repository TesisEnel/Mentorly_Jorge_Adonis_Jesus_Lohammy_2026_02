package com.sagrd.mentorly.data.remote.dto.analytics

import com.sagrd.mentorly.domain.model.analytics.PeerReviewBottleneck

data class PeerReviewBottleneckDto(
    val activityId: String,
    val activityTitle: String,
    val pendingSubmissions: Int,
    val escalatedSubmissions: Int,
    val oldestPendingAtUtc: String?
) {
    fun toDomain() = PeerReviewBottleneck(
        activityId = activityId,
        activityTitle = activityTitle,
        pendingSubmissions = pendingSubmissions,
        escalatedSubmissions = escalatedSubmissions,
        oldestPendingAtUtc = oldestPendingAtUtc
    )
}
