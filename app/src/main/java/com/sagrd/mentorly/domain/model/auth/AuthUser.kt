package com.sagrd.mentorly.domain.model.auth

data class AuthUser(
    val uid: String,
    val email: String?,
    val displayName: String?,
    val photoUrl: String?
)