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
import com.sagrd.mentorly.presentation.admin.course.form.CourseFormScreen
import com.sagrd.mentorly.presentation.admin.course.list.AdminCourseListScreen
import com.sagrd.mentorly.presentation.admin.content.ContentManagementScreen
import com.sagrd.mentorly.presentation.admin.content.activity.ActivityFormScreen
import com.sagrd.mentorly.presentation.admin.content.theme.ThemeFormScreen
import com.sagrd.mentorly.presentation.admin.content.unit.UnitFormScreen
import com.sagrd.mentorly.presentation.admin.dashboard.AdminDashboardScreen
import com.sagrd.mentorly.presentation.auth.LoginScreen
import com.sagrd.mentorly.presentation.course.detail.CourseDetailScreen
import com.sagrd.mentorly.presentation.course.list.CourseListScreen
import com.sagrd.mentorly.presentation.enrollment.detail.EnrollmentDetailScreen
import com.sagrd.mentorly.presentation.enrollment.list.EnrollmentListScreen
import com.sagrd.mentorly.presentation.peerreview.detail.PeerReviewDetailScreen
import com.sagrd.mentorly.presentation.peerreview.history.PeerReviewHistoryScreen
import com.sagrd.mentorly.presentation.peerreview.queue.PeerReviewQueueScreen
import com.sagrd.mentorly.presentation.progress.EnrollmentProgressScreen
import com.sagrd.mentorly.presentation.profile.ProfileScreen
import com.sagrd.mentorly.presentation.startup.StartupScreen
import com.sagrd.mentorly.domain.model.student.StudentRole

@Composable
fun AppNavigation() {
    val backStack = rememberNavBackStack(Screen.Startup)

    AppNavigationDisplay(backStack)
}

@Composable
private fun AppNavigationDisplay(
    backStack: NavBackStack<NavKey>
) {
    val currentDestination = backStack.lastOrNull()
    val showBottomNavigation = currentDestination is Screen.CourseList ||
        currentDestination is Screen.EnrollmentList ||
        currentDestination is Screen.PeerReviewQueue ||
        currentDestination is Screen.Profile

    Scaffold(
        bottomBar = {
            if (showBottomNavigation) {
                MentorlyBottomNavigation(
                    currentSection = when (currentDestination) {
                        is Screen.Profile -> MentorlySection.PROFILE
                        is Screen.EnrollmentList -> MentorlySection.LEARNING
                        is Screen.PeerReviewQueue -> MentorlySection.REVIEWS
                        else -> MentorlySection.COURSES
                    },
                    onSectionSelected = { section ->
                        when (section) {
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
                            MentorlySection.LEARNING -> {
                                if (currentDestination !is Screen.EnrollmentList) {
                                    replaceRoot(backStack, Screen.EnrollmentList)
                                }
                            }
                            MentorlySection.REVIEWS -> {
                                if (currentDestination !is Screen.PeerReviewQueue) {
                                    replaceRoot(backStack, Screen.PeerReviewQueue)
                                }
                            }
                            else -> {}
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
            if (backStack.size > 1) {
                backStack.removeLastOrNull()
            }
        },
        entryProvider = entryProvider {
            entry<Screen.Startup> {
                StartupScreen(
                    onNavigateToLogin = {
                        replaceRoot(backStack, Screen.Login)
                    },
                    onNavigateToCourseList = {
                        replaceRoot(backStack, Screen.CourseList)
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
                                Screen.CourseList
                            }
                        )
                    }
                )
            }

            entry<Screen.CourseList> {
                CourseListScreen(
                    onCourseClick = { courseId ->
                        backStack.add(Screen.CourseDetail(courseId))
                    }
                )
            }

            entry<Screen.CourseDetail> { destination ->
                CourseDetailScreen(
                    courseId = destination.courseId,
                    onBackClick = {
                        backStack.removeLastOrNull()
                    },
                    onEnrollmentCreated = { enrollmentId ->
                        backStack.removeLastOrNull()
                        backStack.add(Screen.EnrollmentDetail(enrollmentId))
                    },
                    onActiveEnrollmentClick = { enrollmentId ->
                        backStack.add(Screen.EnrollmentDetail(enrollmentId))
                    }
                )
            }

            entry<Screen.EnrollmentList> {
                EnrollmentListScreen(
                    onEnrollmentClick = { enrollmentId ->
                        backStack.add(Screen.EnrollmentDetail(enrollmentId))
                    }
                )
            }

            entry<Screen.EnrollmentDetail> { destination ->
                EnrollmentDetailScreen(
                    enrollmentId = destination.enrollmentId,
                    onBackClick = {
                        backStack.removeLastOrNull()
                    },
                    onRestarted = { enrollmentId ->
                        backStack.removeLastOrNull()
                        backStack.add(Screen.EnrollmentDetail(enrollmentId))
                    },
                    onProgressClick = { enrollmentId ->
                        backStack.add(Screen.EnrollmentProgress(enrollmentId))
                    }
                )
            }

            entry<Screen.EnrollmentProgress> { destination ->
                EnrollmentProgressScreen(
                    enrollmentId = destination.enrollmentId,
                    onBackClick = {
                        backStack.removeLastOrNull()
                    },
                    onActivityClick = {
                        // Las rutas de quizzes y entregas se conectarán en sus respectivas ramas.
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
                        backStack.removeLastOrNull()
                    },
                    onReviewCompleted = {
                        replaceRoot(backStack, Screen.PeerReviewQueue)
                    }
                )
            }

            entry<Screen.PeerReviewHistory> {
                PeerReviewHistoryScreen(
                    onBackClick = {
                        backStack.removeLastOrNull()
                    }
                )
            }

            entry<Screen.Profile> {
                ProfileScreen(
                    onSignOutCompleted = {
                        replaceRoot(backStack, Screen.Login)
                    },
                    onAdminDashboardClick = {
                        backStack.add(Screen.AdminDashboard)
                    }
                )
            }

            entry<Screen.AdminDashboard> {
                AdminDashboardScreen(
                    onCoursesClick = {
                        backStack.add(Screen.AdminCourseList)
                    },
                    onStudentsClick = {},
                    onPeerReviewsClick = {},
                    onEscalatedSubmissionsClick = {},
                    onAnalyticsClick = {}
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
                        backStack.removeLastOrNull()
                    }
                )
            }

            entry<Screen.AdminCourseForm> { destination ->
                CourseFormScreen(
                    courseId = destination.courseId,
                    onBackClick = {
                        backStack.removeLastOrNull()
                    },
                    onSaved = {
                        backStack.removeLastOrNull()
                        if (backStack.lastOrNull() !is Screen.AdminCourseList) {
                            backStack.add(Screen.AdminCourseList)
                        }
                    }
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
