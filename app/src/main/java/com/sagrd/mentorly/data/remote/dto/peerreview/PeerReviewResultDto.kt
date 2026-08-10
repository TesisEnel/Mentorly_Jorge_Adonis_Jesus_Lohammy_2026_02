package com.sagrd.mentorly.data.remote.dto.peerreview

import com.sagrd.mentorly.domain.model.peerreview.PeerReviewResult
import com.sagrd.mentorly.domain.model.submission.SubmissionStatus

data class PeerReviewResultDto(
    val peerReviewId: String,
    val submissionId: String,
    val reviewerStudentId: String,
    val isApproved: Boolean,
    val feedbackComment: String,
    val createdAtUtc: String,
    val positiveReviews: Int,
    val requiredPositiveReviews: Int,
    val submissionStatus: Int
) {
    fun toDomain() = PeerReviewResult(
        peerReviewId = peerReviewId,
        submissionId = submissionId,
        reviewerStudentId = reviewerStudentId,
        isApproved = isApproved,
        feedbackComment = feedbackComment,
        createdAt = createdAtUtc,
        positiveReviews = positiveReviews,
        requiredPositiveReviews = requiredPositiveReviews,
        submissionStatus = SubmissionStatus.fromApi(submissionStatus)
    )
}
