package com.sagrd.mentorly.domain.model.peerreview

data class PeerReviewCriterionScore(
    val rubricCriterionId: String,
    val score: Int,
    val criterionTitle: String = "Criterio",
    val criterionDescription: String? = null,
    val maxScore: Int = 5
)
