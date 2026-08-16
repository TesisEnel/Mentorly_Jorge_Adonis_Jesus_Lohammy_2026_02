package com.sagrd.mentorly.domain.model.peerreview

import com.sagrd.mentorly.domain.model.submission.EvidenceType

data class ReviewQueueItem(
    val submissionId: String,
    val activityId: String,
    val activityTitle: String,
    val evidenceType: EvidenceType,
    val evidenceContent: String,
    val submittedAtUtc: String
) {
}
