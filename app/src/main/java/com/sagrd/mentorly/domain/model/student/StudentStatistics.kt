package com.sagrd.mentorly.domain.model.student

data class StudentStatistics(
    val studentId: String,
    val role: StudentRole,
    val isLeaderboardPublic: Boolean,
    val totalPoints: Int,
    val badges: List<Badge>
)