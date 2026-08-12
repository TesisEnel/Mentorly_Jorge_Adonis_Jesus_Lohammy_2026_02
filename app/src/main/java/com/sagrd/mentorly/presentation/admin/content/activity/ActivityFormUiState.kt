package com.sagrd.mentorly.presentation.admin.content.activity

import com.sagrd.mentorly.domain.model.content.ActivityType
import com.sagrd.mentorly.domain.model.content.ApprovalStrategy

data class ActivityFormUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isEditMode: Boolean = false,
    val title: String = "",
    val type: ActivityType = ActivityType.EXERCISE,
    val isMandatory: Boolean = true,
    val approvalStrategy: ApprovalStrategy = ApprovalStrategy.AUTO,
    val orderIndex: String = "0",
    val errorMessage: String? = null,
    val fieldErrors: Map<String, String> = emptyMap(),
    val isSaved: Boolean = false,
    val hasAdminAccess: Boolean = true
)
