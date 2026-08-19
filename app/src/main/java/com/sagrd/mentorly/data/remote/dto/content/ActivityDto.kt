package com.sagrd.mentorly.data.remote.dto.content

import com.sagrd.mentorly.domain.model.content.Activity
import com.sagrd.mentorly.domain.model.content.ActivityType
import com.sagrd.mentorly.domain.model.content.ApprovalStrategy

data class ActivityDto(
    val id: String,
    val themeId: String? = null,
    val title: String,
    val description: String = "",
    val type: Int,
    val isMandatory: Boolean,
    val approvalStrategy: Int,
    val orderIndex: Int
) {
    fun toDomain(parentThemeId: String) = Activity(
            id = id,
            themeId = themeId ?: parentThemeId,
            title = title,
            description = description,
            type = ActivityType.fromApi(type),
            isMandatory = isMandatory,
            approvalStrategy = ApprovalStrategy.fromApi(approvalStrategy),
            orderIndex = orderIndex
        )
}