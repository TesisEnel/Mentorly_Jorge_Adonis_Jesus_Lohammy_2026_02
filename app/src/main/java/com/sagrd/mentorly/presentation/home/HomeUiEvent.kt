package com.sagrd.mentorly.presentation.home

sealed interface HomeUiEvent {
    data object Load : HomeUiEvent
    data object Refresh : HomeUiEvent
    data object ClearError : HomeUiEvent
}
