package com.sagrd.mentorly.data.remote.dto.theme

data class UpdateThemeDto(
    val title: String,
    val contentText: String,
    val orderIndex: Int
)