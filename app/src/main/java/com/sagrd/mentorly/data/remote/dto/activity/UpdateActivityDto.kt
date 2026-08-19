package com.sagrd.mentorly.data.remote.dto.activity

data class UpdateActivityDto(
    val title: String,
    val description: String = "",
    val type: Int,
    val isMandatory: Boolean,
    val approvalStrategy: Int,
    val orderIndex: Int
)
