package com.sagrd.mentorly.domain.model.content

data class Theme(
    val id: String,
    val unitId: String,
    val title: String,
    val contentText: String,
    val orderIndex: Int,
    val activities: List<Activity> = emptyList()
)
