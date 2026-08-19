package com.sagrd.mentorly.domain.model.community

data class CourseMember(
    val studentId: String,
    val displayName: String,
    val totalPoints: Int = 0,
    val isLeaderboardPublic: Boolean = true
)
