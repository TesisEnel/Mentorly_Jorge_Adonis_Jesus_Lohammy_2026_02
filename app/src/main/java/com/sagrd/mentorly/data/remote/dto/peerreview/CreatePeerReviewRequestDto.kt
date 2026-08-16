package com.sagrd.mentorly.data.remote.dto.peerreview

data class CreatePeerReviewRequestDto(
    val submissionId: String,
    val isApproved: Boolean,
    val feedbackComment: String,
    val criterionScores: List<PeerReviewCriterionScoreDto> = emptyList()
)
