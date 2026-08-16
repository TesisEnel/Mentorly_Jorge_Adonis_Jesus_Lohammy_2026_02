package com.sagrd.mentorly.data.remote.dto.peerreview

data class UpdatePeerReviewRubricCriterionDto(
    val title: String,
    val description: String,
    val maxScore: Int,
    val orderIndex: Int
)
