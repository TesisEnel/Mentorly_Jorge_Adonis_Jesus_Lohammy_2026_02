package com.sagrd.mentorly.data.remote.dto.peerreview

import com.sagrd.mentorly.domain.model.peerreview.PeerReviewCriterionScore

data class PeerReviewCriterionScoreDto(
    val rubricCriterionId: String,
    val score: Int
) {
    fun toDomain() = PeerReviewCriterionScore(
        rubricCriterionId = rubricCriterionId,
        score = score
    )
}
