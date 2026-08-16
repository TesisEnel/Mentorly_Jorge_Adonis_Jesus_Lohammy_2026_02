package com.sagrd.mentorly.data.remote.dto.peerreview

import com.sagrd.mentorly.domain.model.peerreview.PeerReviewRubricCriterion

data class PeerReviewRubricCriterionDto(
    val id: String,
    val activityId: String,
    val title: String,
    val description: String,
    val maxScore: Int,
    val orderIndex: Int
) {
    fun toDomain() = PeerReviewRubricCriterion(
        id = id,
        activityId = activityId,
        title = title,
        description = description,
        maxScore = maxScore,
        orderIndex = orderIndex
    )
}
