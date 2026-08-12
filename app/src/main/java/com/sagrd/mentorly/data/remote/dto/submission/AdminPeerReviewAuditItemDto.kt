package com.sagrd.mentorly.data.remote.dto.submission

import com.sagrd.mentorly.domain.model.submission.AdminPeerReviewAuditItem

data class AdminPeerReviewAuditItemDto(
    val peerReviewId: String,
    val reviewerStudentId: String,
    val reviewerDisplayName: String,
    val reviewerEmail: String,
    val isApproved: Boolean,
    val feedbackComment: String,
    val createdAtUtc: String
) {
    fun toDomain() = AdminPeerReviewAuditItem(
        peerReviewId = peerReviewId,
        reviewerStudentId = reviewerStudentId,
        reviewerDisplayName = reviewerDisplayName,
        reviewerEmail = reviewerEmail,
        isApproved = isApproved,
        feedbackComment = feedbackComment,
        createdAtUtc = createdAtUtc
    )
}
