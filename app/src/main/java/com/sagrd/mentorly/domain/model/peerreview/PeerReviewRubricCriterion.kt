package com.sagrd.mentorly.domain.model.peerreview

data class PeerReviewRubricCriterion(
    val id: String,
    val activityId: String,
    val title: String,
    val description: String,
    val maxScore: Int,
    val orderIndex: Int
)
