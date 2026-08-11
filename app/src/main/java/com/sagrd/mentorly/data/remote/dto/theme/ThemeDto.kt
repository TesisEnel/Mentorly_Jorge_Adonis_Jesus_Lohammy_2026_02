package com.sagrd.mentorly.data.remote.dto.theme

import com.sagrd.mentorly.domain.model.content.Theme

data class ThemeDto(
    val id: String,
    val unitId: String,
    val title: String,
    val contentText: String,
    val orderIndex: Int
) {
    fun toDomain() = Theme(
        id = id,
        unitId = unitId,
        title = title,
        contentText = contentText,
        orderIndex = orderIndex
    )
}
