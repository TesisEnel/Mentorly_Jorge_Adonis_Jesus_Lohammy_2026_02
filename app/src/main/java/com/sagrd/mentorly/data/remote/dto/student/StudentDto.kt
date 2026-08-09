package com.sagrd.mentorly.data.remote.dto.student

import com.sagrd.mentorly.domain.model.student.Student
import com.sagrd.mentorly.domain.model.student.StudentRole

data class StudentDto(
    val id: String,
    val email: String? = null,
    val displayName: String,
    val role: Int,
    val isLeaderboardPublic: Boolean,
    val totalPoints: Int
) {
    fun toDomain() = Student(
            id = id,
            email = email,
            displayName = displayName,
            role = StudentRole.fromApi(role),
            isLeaderboardPublic = isLeaderboardPublic,
            totalPoints = totalPoints
        )
    }