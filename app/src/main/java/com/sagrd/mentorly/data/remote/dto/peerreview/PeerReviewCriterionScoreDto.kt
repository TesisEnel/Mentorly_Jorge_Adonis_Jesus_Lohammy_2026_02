package com.sagrd.mentorly.data.remote.dto.peerreview

import com.sagrd.mentorly.domain.model.peerreview.PeerReviewCriterionScore

data class PeerReviewCriterionScoreDto(
    val rubricCriterionId: String,
    val title: String,
    val description: String,
    val score: Int,
    val maxScore: Int
) {
    fun toDomain() = PeerReviewCriterionScore(
        rubricCriterionId = rubricCriterionId,
        title = title,
        description = description,
        score = score,
        maxScore = maxScore
    )
}
