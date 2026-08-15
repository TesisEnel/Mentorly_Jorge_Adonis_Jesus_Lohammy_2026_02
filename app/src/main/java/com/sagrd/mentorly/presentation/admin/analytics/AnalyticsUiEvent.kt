package com.sagrd.mentorly.presentation.admin.analytics

sealed interface AnalyticsUiEvent {
    data object Load : AnalyticsUiEvent
    data object Refresh : AnalyticsUiEvent
    data class CourseSelected(val courseId: String) : AnalyticsUiEvent
    data object RetryOverview : AnalyticsUiEvent
    data object RetryCourseAnalytics : AnalyticsUiEvent
    data object ClearErrors : AnalyticsUiEvent
}
