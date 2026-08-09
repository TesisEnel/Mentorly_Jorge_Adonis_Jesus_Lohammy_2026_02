package com.sagrd.mentorly.data.remote.dto.student

import com.sagrd.mentorly.domain.model.student.StudentRole
import com.sagrd.mentorly.domain.model.student.StudentStatistics
import kotlin.collections.map

data class StudentStatisticsDto(
    val studentId: String,
    val role: Int,
    val isLeaderboardPublic: Boolean,
    val totalPoints: Int,
    val badges: List<BadgeDto>
) {
    fun toDomain() = StudentStatistics (
            studentId = studentId,
            role = StudentRole.fromApi(role),
            isLeaderboardPublic = isLeaderboardPublic,
            totalPoints = totalPoints,
            badges = badges.map { it.toDomain() }
    )
}