package com.sagrd.mentorly.domain.model.theme

data class Theme(
    val id: String,
    val unitId: String,
    val title: String,
    val contentText: String,
    val orderIndex: Int
)