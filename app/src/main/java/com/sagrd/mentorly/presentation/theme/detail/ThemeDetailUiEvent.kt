package com.sagrd.mentorly.presentation.theme.detail

sealed interface ThemeDetailUiEvent {
    data class LoadTheme(val enrollmentId: String, val themeId: String) : ThemeDetailUiEvent
    data object CompleteTheme : ThemeDetailUiEvent
    data object ClearError : ThemeDetailUiEvent
}
