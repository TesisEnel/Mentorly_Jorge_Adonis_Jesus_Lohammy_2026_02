package com.sagrd.mentorly.data.remote.dto.peerreview

import com.sagrd.mentorly.domain.model.peerreview.PeerReviewCriterionScore

data class PeerReviewCriterionScoreDto(
    val rubricCriterionId: String,
    val score: Int,
    val criterionTitle: String? = null,
    val criterionDescription: String? = null,
    val maxScore: Int? = null
) {
    fun toDomain() = PeerReviewCriterionScore(
        rubricCriterionId = rubricCriterionId,
        score = score,
        criterionTitle = criterionTitle ?: "Criterio",
        criterionDescription = criterionDescription,
        maxScore = maxScore ?: 5
    )
}
