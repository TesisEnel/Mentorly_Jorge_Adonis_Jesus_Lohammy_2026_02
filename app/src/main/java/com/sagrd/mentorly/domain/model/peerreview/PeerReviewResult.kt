package com.sagrd.mentorly.domain.model.peerreview

import com.sagrd.mentorly.domain.model.submission.SubmissionStatus

data class PeerReviewResult(
    val peerReviewId: String,
    val submissionId: String,
    val reviewerStudentId: String,
    val isApproved: Boolean,
    val feedbackComment: String,
    val createdAt: String,
    val positiveReviews: Int,
    val requiredPositiveReviews: Int,
    val submissionStatus: SubmissionStatus
)
