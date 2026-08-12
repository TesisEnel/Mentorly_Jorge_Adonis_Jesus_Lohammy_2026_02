package com.sagrd.mentorly.presentation.admin.content.unit

sealed interface UnitFormUiEvent {
    data class Load(val courseId: String, val unitId: String?) : UnitFormUiEvent
    data class TitleChanged(val value: String) : UnitFormUiEvent
    data class OrderChanged(val value: String) : UnitFormUiEvent
    data object Save : UnitFormUiEvent
    data object ClearError : UnitFormUiEvent
}
