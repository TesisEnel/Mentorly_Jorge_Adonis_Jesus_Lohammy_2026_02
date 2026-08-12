package com.sagrd.mentorly.presentation.admin.dashboard

import com.sagrd.mentorly.domain.model.analytics.AnalyticsOverview

data class AdminDashboardUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val adminName: String = "",
    val overview: AnalyticsOverview? = null,
    val errorMessage: String? = null,
    val hasSession: Boolean = true,
    val hasAdminAccess: Boolean = true
)
