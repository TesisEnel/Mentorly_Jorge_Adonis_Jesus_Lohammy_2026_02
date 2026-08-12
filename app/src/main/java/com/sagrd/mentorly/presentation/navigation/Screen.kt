package com.sagrd.mentorly.presentation.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed class Screen : NavKey {

    @Serializable
    data object Startup : Screen()

    @Serializable
    data object Login : Screen()

    @Serializable
    data object CourseList : Screen()

    @Serializable
    data class CourseDetail(
        val courseId: String
    ) : Screen()

    @Serializable
    data object EnrollmentList : Screen()

    @Serializable
    data class EnrollmentDetail(
        val enrollmentId: String
    ) : Screen()

    @Serializable
    data class EnrollmentProgress(
        val enrollmentId: String
    ) : Screen()

    @Serializable
    data object PeerReviewQueue : Screen()

    @Serializable
    data class PeerReviewDetail(
        val submissionId: String
    ) : Screen()

    @Serializable
    data object PeerReviewHistory : Screen()

    @Serializable
    data object Profile : Screen()

    @Serializable
    data object AdminDashboard : Screen()

    @Serializable
    data object AdminCourseList : Screen()

    @Serializable
    data class AdminCourseForm(
        val courseId: String?
    ) : Screen()

    @Serializable
    data class ContentManagement(val courseId: String) : Screen()

    @Serializable
    data class UnitForm(val courseId: String, val unitId: String?) : Screen()

    @Serializable
    data class ThemeForm(val unitId: String, val themeId: String?) : Screen()

    @Serializable
    data class ActivityForm(val themeId: String, val activityId: String?) : Screen()
}
