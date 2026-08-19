package com.sagrd.mentorly.data.remote.dto.community

import com.sagrd.mentorly.domain.model.community.CourseMember

data class CourseMemberDto(
    val studentId: String,
    val displayName: String,
    val isLeaderboardPublic: Boolean,
    val totalPoints: Int = 0
) {
    fun toDomain() = CourseMember(
        studentId = studentId,
        displayName = displayName,
        isLeaderboardPublic = isLeaderboardPublic,
        totalPoints = totalPoints
    )
}
