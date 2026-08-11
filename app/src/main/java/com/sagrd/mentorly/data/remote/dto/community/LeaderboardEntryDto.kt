package com.sagrd.mentorly.data.remote.dto.community

import com.sagrd.mentorly.domain.model.community.LeaderboardEntry

data class LeaderboardEntryDto(
    val studentId: String,
    val displayName: String,
    val totalPoints: Int,
    val rank: Int
) {
    fun toDomain() = LeaderboardEntry(
        studentId = studentId,
        displayName = displayName,
        totalPoints = totalPoints,
        rank = rank
    )
}
