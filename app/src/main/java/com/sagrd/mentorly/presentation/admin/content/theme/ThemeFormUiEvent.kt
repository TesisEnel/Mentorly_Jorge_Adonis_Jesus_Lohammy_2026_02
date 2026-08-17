package com.sagrd.mentorly.presentation.admin.content.theme

sealed interface ThemeFormUiEvent {
    data class Load(val unitId: String, val themeId: String?) : ThemeFormUiEvent
    data class TitleChanged(val value: String) : ThemeFormUiEvent
    data class ContentChanged(val value: String) : ThemeFormUiEvent
    data class OrderChanged(val value: String) : ThemeFormUiEvent
    data object Save : ThemeFormUiEvent
    data object DeleteTheme : ThemeFormUiEvent
    data object ClearError : ThemeFormUiEvent
    data object SavedHandled : ThemeFormUiEvent
    data object DeletedHandled : ThemeFormUiEvent
}
