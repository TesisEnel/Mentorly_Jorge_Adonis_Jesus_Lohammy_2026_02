package com.sagrd.mentorly.presentation.admin.analytics

import com.sagrd.mentorly.domain.model.analytics.*
import com.sagrd.mentorly.domain.model.course.Course

data class AnalyticsUiState(
    val isLoadingOverview: Boolean = false,
    val isLoadingCourses: Boolean = false,
    val isLoadingCourseAnalytics: Boolean = false,
    val isRefreshing: Boolean = false,

    val overview: AnalyticsOverview? = null,
    val courses: List<Course> = emptyList(),
    val selectedCourseId: String? = null,

    val dropOff: List<DropOff> = emptyList(),
    val completionTime: CompletionTimeReport? = null,
    val peerReviewBottlenecks: List<PeerReviewBottleneck> = emptyList(),
    val enrollmentHistory: List<EnrollmentHistory> = emptyList(),

    val overviewErrorMessage: String? = null,
    val coursesErrorMessage: String? = null,
    val courseAnalyticsErrorMessage: String? = null,
    val hasSession: Boolean = true,
    val hasAdminAccess: Boolean = true
)
