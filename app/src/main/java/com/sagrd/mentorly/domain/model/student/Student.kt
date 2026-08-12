package com.sagrd.mentorly.domain.model.student

data class Student(
    val id: String,
    val email: String? = null,
    val displayName: String,
    val role: StudentRole,
    val isLeaderboardPublic: Boolean,
    val totalPoints: Int
)