package com.sagrd.mentorly.presentation.admin.dashboard

sealed interface AdminDashboardUiEvent {
    data object Load : AdminDashboardUiEvent
    data object Refresh : AdminDashboardUiEvent
    data object ClearError : AdminDashboardUiEvent
}
