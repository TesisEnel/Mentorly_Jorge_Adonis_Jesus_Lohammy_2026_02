package com.sagrd.mentorly.presentation.admin.dashboard

import com.sagrd.mentorly.domain.model.analytics.AnalyticsOverview
import androidx.compose.ui.graphics.vector.ImageVector

data class AdminDashboardUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val adminName: String = "",
    val overview: AnalyticsOverview? = null,
    val errorMessage: String? = null,
    val hasSession: Boolean = true,
    val hasAdminAccess: Boolean = true,
    val isSignOutDialogVisible: Boolean = false,
    val isSignedOut: Boolean = false,
)

data class DashboardMetric(
    val label: String,
    val value: String,
    val icon: ImageVector,
    val isAlert: Boolean = false,
)

data class DashboardAction(
    val label: String,
    val description: String,
    val icon: ImageVector,
    val onClick: () -> Unit,
)
