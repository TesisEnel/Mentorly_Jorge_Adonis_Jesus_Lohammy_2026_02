package com.sagrd.mentorly.domain.model.peerreview

data class PeerReviewCriterionScore(
    val rubricCriterionId: String,
    val title: String,
    val description: String,
    val score: Int,
    val maxScore: Int
)
