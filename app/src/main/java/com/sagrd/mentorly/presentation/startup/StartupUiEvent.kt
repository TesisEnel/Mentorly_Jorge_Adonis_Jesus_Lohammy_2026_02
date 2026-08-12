package com.sagrd.mentorly.presentation.startup

sealed interface StartupUiEvent {
    data object Retry : StartupUiEvent
    data object SignOut : StartupUiEvent
    data object DestinationHandled : StartupUiEvent
}
