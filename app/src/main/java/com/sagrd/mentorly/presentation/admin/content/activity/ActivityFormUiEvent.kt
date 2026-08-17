package com.sagrd.mentorly.presentation.admin.content.activity

import com.sagrd.mentorly.domain.model.content.ActivityType
import com.sagrd.mentorly.domain.model.content.ApprovalStrategy

sealed interface ActivityFormUiEvent {
    data class Load(val themeId: String, val activityId: String?) : ActivityFormUiEvent
    data class TitleChanged(val value: String) : ActivityFormUiEvent
    data class TypeChanged(val value: ActivityType) : ActivityFormUiEvent
    data class MandatoryChanged(val value: Boolean) : ActivityFormUiEvent
    data class StrategyChanged(val value: ApprovalStrategy) : ActivityFormUiEvent
    data class OrderChanged(val value: String) : ActivityFormUiEvent
    data object Save : ActivityFormUiEvent
    data object DeleteActivity : ActivityFormUiEvent
    data object ClearError : ActivityFormUiEvent
    data object SavedHandled : ActivityFormUiEvent
    data object DeletedHandled : ActivityFormUiEvent
}
