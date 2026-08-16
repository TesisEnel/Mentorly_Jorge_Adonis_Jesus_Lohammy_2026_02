package com.sagrd.mentorly.data.remote.dto.peerreview

import com.sagrd.mentorly.domain.model.peerreview.ReviewQueueItem
import com.sagrd.mentorly.domain.model.submission.EvidenceType

data class ReviewQueueItemDto(
    val submissionId: String,
    val activityId: String,
    val activityTitle: String,
    val evidenceType: Int,
    val evidenceContent: String,
    val submittedAtUtc: String
) {
    fun toDomain() = ReviewQueueItem(
        submissionId = submissionId,
        activityId = activityId,
        activityTitle = activityTitle,
        evidenceType = EvidenceType.fromApi(evidenceType),
        evidenceContent = evidenceContent,
        submittedAtUtc = submittedAtUtc
    )
}
