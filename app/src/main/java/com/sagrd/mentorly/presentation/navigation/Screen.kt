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
    data object Home : Screen()

    @Serializable
    data object CourseList : Screen()

    @Serializable
    data class CourseDetail(
        val courseId: String,
    ) : Screen()


    @Serializable
    data class EnrollmentProgress(
        val enrollmentId: String
    ) : Screen()

    @Serializable
    data class ThemeDetail(
        val enrollmentId: String,
        val themeId: String
    ) : Screen()

    @Serializable
    data object SubmissionList : Screen()

    @Serializable
    data class SubmissionDetail(
        val submissionId: String
    ) : Screen()

    @Serializable
    data class SubmissionForm(
        val enrollmentId: String,
        val activityId: String,
        val submissionId: String? = null
    ) : Screen()

    @Serializable
    data class Quiz(
        val enrollmentId: String,
        val activityId: String
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

    @Serializable
    data class AdminQuizQuestion(val activityId: String) : Screen()

    @Serializable
    data class AdminPeerReviewRubric(val activityId: String) : Screen()

    @Serializable
    data class CourseMembers(val courseId: String) : Screen()

    @Serializable
    data class CourseLeaderboard(val courseId: String) : Screen()

    @Serializable
    data object AdminStudentList : Screen()

    @Serializable
    data class AdminStudentDetail(val studentId: String) : Screen()

    @Serializable
    data object AdminPeerReviewList : Screen()

    @Serializable
    data class PeerReviewAudit(val peerReviewId: String) : Screen()

    @Serializable
    data object AdminSubmissionList : Screen()

    @Serializable
    data class AdminSubmissionAudit(val submissionId: String) : Screen()

}
