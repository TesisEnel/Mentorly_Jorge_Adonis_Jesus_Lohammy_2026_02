package com.sagrd.mentorly.data.remote.dto.community

import com.sagrd.mentorly.domain.model.community.LeaderboardEntry

data class LeaderboardEntryDto(
    val position: Int,
    val studentId: String,
    val displayName: String,
    val totalPoints: Int,
    val isLeaderboardPublic: Boolean
) {
    fun toDomain() = LeaderboardEntry(
        studentId = studentId,
        displayName = displayName,
        totalPoints = totalPoints,
        rank = position,
        isLeaderboardPublic = isLeaderboardPublic
    )
}
