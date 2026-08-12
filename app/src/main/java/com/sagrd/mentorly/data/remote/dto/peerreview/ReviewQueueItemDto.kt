package com.sagrd.mentorly.data.remote.dto.peerreview

import com.sagrd.mentorly.domain.model.peerreview.ReviewQueueItem

data class ReviewQueueItemDto(
    val submissionId: String,
    val activityId: String,
    val activityTitle: String,
    val evidenceUrl: String,
    val submittedAtUtc: String
) {
    fun toDomain() = ReviewQueueItem(
        submissionId = submissionId,
        activityId = activityId,
        activityTitle = activityTitle,
        evidenceUrl = evidenceUrl,
        submittedAtUtc = submittedAtUtc
    )
}