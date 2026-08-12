package com.sagrd.mentorly.domain.model.content

data class Activity(
    val id: String,
    val themeId: String,
    val title: String,
    val type: ActivityType,
    val isMandatory: Boolean,
    val approvalStrategy: ApprovalStrategy,
    val orderIndex: Int
)