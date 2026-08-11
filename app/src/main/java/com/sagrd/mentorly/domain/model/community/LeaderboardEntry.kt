package com.sagrd.mentorly.domain.model.community

data class LeaderboardEntry(
    val studentId: String,
    val displayName: String,
    val totalPoints: Int,
    val rank: Int
)
