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
import com.sagrd.mentorly.presentation.admin.dashboard.AdminDashboardScreen
import com.sagrd.mentorly.presentation.auth.LoginScreen
import com.sagrd.mentorly.presentation.course.detail.CourseDetailScreen
import com.sagrd.mentorly.presentation.course.list.CourseListScreen
import com.sagrd.mentorly.presentation.enrollment.detail.EnrollmentDetailScreen
import com.sagrd.mentorly.presentation.enrollment.list.EnrollmentListScreen
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
        currentDestination is Screen.Profile

    Scaffold(
        bottomBar = {
            if (showBottomNavigation) {
                MentorlyBottomNavigation(
                    currentSection = when (currentDestination) {
                        is Screen.Profile -> MentorlySection.PROFILE
                        is Screen.EnrollmentList -> MentorlySection.LEARNING
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
                    onCoursesClick = {},
                    onStudentsClick = {},
                    onPeerReviewsClick = {},
                    onEscalatedSubmissionsClick = {},
                    onAnalyticsClick = {}
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
