package com.sagrd.mentorly.domain.model.session

import com.sagrd.mentorly.domain.model.student.StudentRole

data class AppSession(
    val studentId: String,
    val firebaseUserId: String,
    val displayName: String,
    val email: String?,
    val role: StudentRole,
    val isLeaderboardPublic: Boolean
)
