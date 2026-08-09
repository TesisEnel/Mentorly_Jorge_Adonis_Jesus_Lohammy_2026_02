package com.sagrd.mentorly.data.remote.dto.student

data class ProvisionStudentDto(
    val googleUserId: String,
    val email: String,
    val displayName: String
)