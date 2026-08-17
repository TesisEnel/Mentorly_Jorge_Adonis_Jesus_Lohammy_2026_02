package com.sagrd.mentorly.presentation.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.sagrd.mentorly.presentation.admin.analytics.AnalyticsScreen
import com.sagrd.mentorly.presentation.admin.course.form.CourseFormScreen
import com.sagrd.mentorly.presentation.admin.course.list.AdminCourseListScreen
import com.sagrd.mentorly.presentation.admin.content.ContentManagementScreen
import com.sagrd.mentorly.presentation.admin.content.activity.ActivityFormScreen
import com.sagrd.mentorly.presentation.admin.content.theme.ThemeFormScreen
import com.sagrd.mentorly.presentation.admin.content.unit.UnitFormScreen
import com.sagrd.mentorly.presentation.admin.dashboard.AdminDashboardScreen
import com.sagrd.mentorly.presentation.admin.peerreview.audit.PeerReviewAuditScreen
import com.sagrd.mentorly.presentation.admin.peerreview.list.AdminPeerReviewListScreen
import com.sagrd.mentorly.presentation.admin.quiz.AdminQuizQuestionScreen
import com.sagrd.mentorly.presentation.admin.student.list.AdminStudentListScreen
import com.sagrd.mentorly.presentation.admin.submission.audit.AdminSubmissionAuditScreen
import com.sagrd.mentorly.presentation.admin.submission.list.AdminSubmissionListScreen
import com.sagrd.mentorly.presentation.auth.LoginScreen
import com.sagrd.mentorly.presentation.community.leaderboard.LeaderboardScreen
import com.sagrd.mentorly.presentation.community.members.CourseMembersScreen
import com.sagrd.mentorly.presentation.course.detail.CourseDetailScreen
import com.sagrd.mentorly.presentation.course.list.CourseListScreen
import com.sagrd.mentorly.presentation.home.HomeScreen
import com.sagrd.mentorly.presentation.peerreview.detail.PeerReviewDetailScreen
import com.sagrd.mentorly.presentation.peerreview.history.PeerReviewHistoryScreen
import com.sagrd.mentorly.presentation.peerreview.queue.PeerReviewQueueScreen
import com.sagrd.mentorly.presentation.progress.EnrollmentProgressScreen
import com.sagrd.mentorly.presentation.profile.ProfileScreen
import com.sagrd.mentorly.presentation.quiz.QuizScreen
import com.sagrd.mentorly.presentation.startup.StartupScreen
import com.sagrd.mentorly.presentation.submission.detail.SubmissionDetailScreen
import com.sagrd.mentorly.presentation.submission.form.SubmissionFormScreen
import com.sagrd.mentorly.presentation.submission.list.SubmissionListScreen
import com.sagrd.mentorly.presentation.theme.detail.ThemeDetailScreen
import com.sagrd.mentorly.domain.model.student.StudentRole

@Composable
fun AppNavigation() {
    val backStack = rememberNavBackStack(Screen.Startup)

    AppNavigationDisplay(backStack)
}

@Composable
private fun AppNavigationDisplay(
    backStack: NavBackStack<NavKey>,
) {
    val currentDestination = backStack.lastOrNull()
    val showBottomNavigation = (currentDestination is Screen.Home ||
        currentDestination is Screen.CourseList ||
        currentDestination is Screen.PeerReviewQueue ||
        currentDestination is Screen.Profile)

    Scaffold(
        bottomBar = {
            if (showBottomNavigation) {
                MentorlyBottomNavigation(
                    currentSection = when (currentDestination) {
                        is Screen.Home -> MentorlySection.HOME
                        is Screen.Profile -> MentorlySection.PROFILE
                        is Screen.PeerReviewQueue -> MentorlySection.REVIEWS
                        else -> MentorlySection.COURSES
                    },
                    onSectionSelected = { section ->
                        when (section) {
                            MentorlySection.HOME -> {
                                if (currentDestination !is Screen.Home) {
                                    replaceRoot(backStack, Screen.Home)
                                }
                            }
                            MentorlySection.COURSES -> {
                                if (currentDestination !is Screen.CourseList) {
                                    replaceRoot(backStack, Screen.CourseList)
                                }
                            }
                            MentorlySection.PROFILE -> {
                                if (currentDestination !is Screen.Profile) {
                                    replaceRoot(backStack, Screen.Profile)
                                }
                            }
                            MentorlySection.REVIEWS -> {
                                if (currentDestination !is Screen.PeerReviewQueue) {
                                    replaceRoot(backStack, Screen.PeerReviewQueue)
                                }
                            }
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavigationContent(
            backStack = backStack,
            innerPadding = innerPadding
        )
    }
}

@Composable
private fun NavigationContent(
    backStack: NavBackStack<NavKey>,
    innerPadding: PaddingValues
) {
    NavDisplay(
        backStack = backStack,
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
        onBack = {
            popBackStackSafely(backStack)
        },
        entryProvider = entryProvider {
            entry<Screen.Startup> {
                StartupScreen(
                    onNavigateToLogin = {
                        replaceRoot(backStack, Screen.Login)
                    },
                    onNavigateToCourseList = {
                        replaceRoot(backStack, Screen.Home)
                    },
                    onNavigateToAdminDashboard = {
                        replaceRoot(backStack, Screen.AdminDashboard)
                    }
                )
            }

            entry<Screen.Login> {
                LoginScreen(
                    onLoginCompleted = { student ->
                        replaceRoot(
                            backStack,
                            if (student.role == StudentRole.ADMIN) {
                                Screen.AdminDashboard
                            } else {
                                Screen.Home
                            }
                        )
                    },
                )
            }

            entry<Screen.Home> {
                HomeScreen(
                    onCourseClick = { courseId ->
                        backStack.add(Screen.CourseDetail(courseId))
                    },
                    onEnrollmentClick = { enrollmentId ->
                        backStack.add(Screen.EnrollmentProgress(enrollmentId))
                    },
                    onProfileClick = {
                        replaceRoot(backStack, Screen.Profile)
                    },
                    onSeeAllEnrollments = {
                        replaceRoot(backStack, Screen.CourseList)
                    },
                    onSeeAllCourses = {
                        replaceRoot(backStack, Screen.CourseList)
                    }
                )
            }

            entry<Screen.CourseList> {
                CourseListScreen(
                    onCourseClick = { courseId ->
                        backStack.add(Screen.CourseDetail(courseId))
                    },
                    onEnrollmentClick = { enrollmentId ->
                        backStack.add(Screen.EnrollmentProgress(enrollmentId))
                    },
                    onSubmissionsClick = {
                        backStack.add(Screen.SubmissionList)
                    },
                    onProfileClick = {
                        replaceRoot(backStack, Screen.Profile)
                    }
                )
            }

            entry<Screen.CourseDetail> { destination ->
                CourseDetailScreen(
                    courseId = destination.courseId,
                    onBackClick = {
                        popBackStackSafely(backStack)
                    },
                    onEnrollmentCreated = { enrollmentId ->
                        popBackStackSafely(backStack)
                        backStack.add(Screen.EnrollmentProgress(enrollmentId))
                    },
                    onActiveEnrollmentClick = { enrollmentId ->
                        backStack.add(Screen.EnrollmentProgress(enrollmentId))
                    },
                    onMembersClick = { courseId ->
                        backStack.add(Screen.CourseMembers(courseId))
                    },
                    onLeaderboardClick = { courseId ->
                        backStack.add(Screen.CourseLeaderboard(courseId))
                    }
                )
            }

            entry<Screen.EnrollmentProgress> { destination ->
                EnrollmentProgressScreen(
                    enrollmentId = destination.enrollmentId,
                    onBackClick = {
                        popBackStackSafely(backStack)
                    },
                    onActivityClick = { activityId ->
                        backStack.add(
                            Screen.SubmissionForm(
                                enrollmentId = destination.enrollmentId,
                                activityId = activityId
                            )
                        )
                    },
                    onQuizClick = { activityId ->
                        backStack.add(
                            Screen.Quiz(
                                enrollmentId = destination.enrollmentId,
                                activityId = activityId
                            )
                        )
                    },
                    onThemeClick = { themeId ->
                        backStack.add(
                            Screen.ThemeDetail(
                                enrollmentId = destination.enrollmentId,
                                themeId = themeId
                            )
                        )
                    }
                )
            }

            entry<Screen.ThemeDetail> { destination ->
                ThemeDetailScreen(
                    enrollmentId = destination.enrollmentId,
                    themeId = destination.themeId,
                    onBackClick = {
                        popBackStackSafely(backStack)
                    }
                )
            }

            entry<Screen.Quiz> { destination ->
                QuizScreen(
                    enrollmentId = destination.enrollmentId,
                    activityId = destination.activityId,
                    onBackClick = {
                        popBackStackSafely(backStack)
                    },
                    onQuizSubmitted = {
                        popBackStackSafely(backStack)
                        val currentDestination = backStack.lastOrNull()
                        if (
                            currentDestination !is Screen.EnrollmentProgress ||
                            currentDestination.enrollmentId != destination.enrollmentId
                        ) {
                            backStack.add(
                                Screen.EnrollmentProgress(destination.enrollmentId)
                            )
                        }
                    }
                )
            }

            entry<Screen.SubmissionList> {
                SubmissionListScreen(
                    onBackClick = {
                        popBackStackSafely(backStack)
                    },
                    onSubmissionClick = { submissionId ->
                        backStack.add(Screen.SubmissionDetail(submissionId))
                    }
                )
            }

            entry<Screen.SubmissionDetail> { destination ->
                SubmissionDetailScreen(
                    submissionId = destination.submissionId,
                    onBackClick = {
                        popBackStackSafely(backStack)
                    },
                    onEditClick = { submissionId, enrollmentId, activityId ->
                        backStack.add(
                            Screen.SubmissionForm(
                                enrollmentId = enrollmentId,
                                activityId = activityId,
                                submissionId = submissionId
                            )
                        )
                    },
                )
            }

            entry<Screen.SubmissionForm> { destination ->
                SubmissionFormScreen(
                    enrollmentId = destination.enrollmentId,
                    activityId = destination.activityId,
                    submissionId = destination.submissionId,
                    onBackClick = {
                        popBackStackSafely(backStack)
                    },
                    onSaved = { submissionId ->
                        popBackStackSafely(backStack)
                        val currentDestination = backStack.lastOrNull()
                        if (
                            currentDestination !is Screen.SubmissionDetail ||
                            currentDestination.submissionId != submissionId
                        ) {
                            backStack.add(Screen.SubmissionDetail(submissionId))
                        }
                    }
                )
            }

            entry<Screen.PeerReviewQueue> {
                PeerReviewQueueScreen(
                    onSubmissionClick = { submissionId ->
                        backStack.add(Screen.PeerReviewDetail(submissionId))
                    },
                    onHistoryClick = {
                        backStack.add(Screen.PeerReviewHistory)
                    }
                )
            }

            entry<Screen.PeerReviewDetail> { destination ->
                PeerReviewDetailScreen(
                    submissionId = destination.submissionId,
                    onBackClick = {
                        popBackStackSafely(backStack)
                    },
                    onReviewCompleted = {
                        replaceRoot(backStack, Screen.PeerReviewQueue)
                    }
                )
            }

            entry<Screen.PeerReviewHistory> {
                PeerReviewHistoryScreen(
                    onBackClick = {
                        popBackStackSafely(backStack)
                    }
                )
            }

            entry<Screen.Profile> {
                ProfileScreen(
                    onBackClick = {
                        if (!popBackStackSafely(backStack)) {
                            replaceRoot(backStack, Screen.Home)
                        }
                    },
                    onSignOutCompleted = {
                        replaceRoot(backStack, Screen.Login)
                    },
                    onAdminDashboardClick = {
                        backStack.add(Screen.AdminDashboard)
                    }
                )
            }

            entry<Screen.CourseMembers> { destination ->
                CourseMembersScreen(
                    courseId = destination.courseId,
                    onBackClick = {
                        popBackStackSafely(backStack)
                    }
                )
            }

            entry<Screen.CourseLeaderboard> { destination ->
                LeaderboardScreen(
                    courseId = destination.courseId,
                    onBackClick = {
                        popBackStackSafely(backStack)
                    }
                )
            }

            entry<Screen.AdminDashboard> {
                AdminDashboardScreen(
                    onCoursesClick = {
                        backStack.add(Screen.AdminCourseList)
                    },
                    onStudentsClick = {
                        backStack.add(Screen.AdminStudentList)
                    },
                    onPeerReviewsClick = {
                        backStack.add(Screen.AdminPeerReviewList)
                    },
                    onEscalatedSubmissionsClick = {
                        backStack.add(Screen.AdminSubmissionList)
                    },
                    onAnalyticsClick = {
                        backStack.add(Screen.AdminAnalytics)
                    },
                    onSignOutCompleted = {
                        replaceRoot(backStack, Screen.Login)
                    },
                )
            }

            entry<Screen.AdminPeerReviewList> {
                AdminPeerReviewListScreen(
                    onBackClick = {
                        popBackStackSafely(backStack)
                    },
                    onPeerReviewClick = { peerReviewId ->
                        backStack.add(Screen.PeerReviewAudit(peerReviewId))
                    }
                )
            }

            entry<Screen.AdminAnalytics> {
                AnalyticsScreen(
                    onBackClick = {
                        popBackStackSafely(backStack)
                    }
                )
            }

            entry<Screen.AdminSubmissionList> {
                AdminSubmissionListScreen(
                    onBackClick = {
                        popBackStackSafely(backStack)
                    },
                    onSubmissionClick = { submissionId ->
                        backStack.add(Screen.AdminSubmissionAudit(submissionId))
                    }
                )
            }

            entry<Screen.AdminSubmissionAudit> { destination ->
                AdminSubmissionAuditScreen(
                    submissionId = destination.submissionId,
                    onBackClick = {
                        popBackStackSafely(backStack)
                    },
                    onDecisionCompleted = {
                        // Volver a la lista sin duplicarla
                        popBackStackSafely(backStack)
                    }
                )
            }

            entry<Screen.PeerReviewAudit> { destination ->
                PeerReviewAuditScreen(
                    peerReviewId = destination.peerReviewId,
                    onBackClick = {
                        popBackStackSafely(backStack)
                    }
                )
            }

            entry<Screen.AdminStudentList> {
                AdminStudentListScreen(
                    onBackClick = {
                        popBackStackSafely(backStack)
                    }
                )
            }

            entry<Screen.AdminCourseList> {
                AdminCourseListScreen(
                    onCreateCourseClick = {
                        backStack.add(Screen.AdminCourseForm(courseId = null))
                    },
                    onEditCourseClick = { courseId ->
                        backStack.add(Screen.AdminCourseForm(courseId))
                    },
                    onManageContentClick = { courseId -> backStack.add(Screen.ContentManagement(courseId)) },
                    onBackClick = {
                        popBackStackSafely(backStack)
                    }
                )
            }

            entry<Screen.AdminCourseForm> { destination ->
                CourseFormScreen(
                    courseId = destination.courseId,
                    onBackClick = {
                        popBackStackSafely(backStack)
                    },
                    onSaved = {
                        popBackStackSafely(backStack)
                        if (backStack.lastOrNull() !is Screen.AdminCourseList) {
                            backStack.add(Screen.AdminCourseList)
                        }
                    }
                )
            }

            entry<Screen.ContentManagement> { destination ->
                ContentManagementScreen(
                    courseId = destination.courseId,
                    onBackClick = {
                        popBackStackSafely(backStack)
                    },
                    onCreateUnitClick = { courseId ->
                        backStack.add(Screen.UnitForm(courseId, unitId = null))
                    },
                    onEditUnitClick = { courseId, unitId ->
                        backStack.add(Screen.UnitForm(courseId, unitId))
                    },
                    onCreateThemeClick = { unitId ->
                        backStack.add(Screen.ThemeForm(unitId, themeId = null))
                    },
                    onEditThemeClick = { unitId, themeId ->
                        backStack.add(Screen.ThemeForm(unitId, themeId))
                    },
                    onCreateActivityClick = { themeId ->
                        backStack.add(Screen.ActivityForm(themeId, activityId = null))
                    },
                    onEditActivityClick = { themeId, activityId ->
                        backStack.add(Screen.ActivityForm(themeId, activityId))
                    },
                    onManageQuizQuestionsClick = { activityId ->
                        backStack.add(Screen.AdminQuizQuestion(activityId))
                    },
                )
            }

            entry<Screen.UnitForm> { destination ->
                UnitFormScreen(
                    courseId = destination.courseId,
                    unitId = destination.unitId,
                    onBackClick = {
                        popBackStackSafely(backStack)
                    },
                    onSaved = {
                        popBackStackSafely(backStack)
                    },
                )
            }

            entry<Screen.ThemeForm> { destination ->
                ThemeFormScreen(
                    unitId = destination.unitId,
                    themeId = destination.themeId,
                    onBackClick = {
                        popBackStackSafely(backStack)
                    },
                    onSaved = {
                        popBackStackSafely(backStack)
                    },
                )
            }

            entry<Screen.ActivityForm> { destination ->
                ActivityFormScreen(
                    themeId = destination.themeId,
                    activityId = destination.activityId,
                    onBackClick = {
                        popBackStackSafely(backStack)
                    },
                    onSaved = {
                        popBackStackSafely(backStack)
                    },
                )
            }

            entry<Screen.AdminQuizQuestion> { destination ->
                AdminQuizQuestionScreen(
                    activityId = destination.activityId,
                    onBackClick = {
                        popBackStackSafely(backStack)
                    },
                    onQuestionCreated = {
                        // La pantalla conserva el formulario para crear otra pregunta.
                    },
                )
            }
        }
    )
}

private fun replaceRoot(
    backStack: NavBackStack<NavKey>,
    screen: Screen
) {
    backStack.clear()
    backStack.add(screen)
}

private fun popBackStackSafely(backStack: NavBackStack<NavKey>): Boolean {
    if (backStack.size <= 1) {
        return false
    }

    backStack.removeLastOrNull()
    return true
}
