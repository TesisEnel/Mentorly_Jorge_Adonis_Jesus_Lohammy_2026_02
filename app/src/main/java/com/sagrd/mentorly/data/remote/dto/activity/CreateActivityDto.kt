package com.sagrd.mentorly.data.remote.dto.activity

data class CreateActivityDto(
    val title: String,
    val type: Int,
    val isMandatory: Boolean,
    val approvalStrategy: Int,
    val orderIndex: Int
)
