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
import com.sagrd.mentorly.presentation.auth.LoginScreen
import com.sagrd.mentorly.presentation.course.detail.CourseDetailScreen
import com.sagrd.mentorly.presentation.course.list.CourseListScreen
import com.sagrd.mentorly.presentation.profile.ProfileScreen
import com.sagrd.mentorly.presentation.startup.StartupScreen

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
    val showBottomNavigation = currentDestination is Screen.CourseList || currentDestination is Screen.Profile

    Scaffold(
        bottomBar = {
            if (showBottomNavigation) {
                MentorlyBottomNavigation(
                    currentSection = when (currentDestination) {
                        is Screen.Profile -> MentorlySection.PROFILE
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
                    }
                )
            }

            entry<Screen.Login> {
                LoginScreen(
                    onLoginCompleted = {
                        replaceRoot(backStack, Screen.CourseList)
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
                    }
                )
            }

            entry<Screen.Profile> {
                ProfileScreen(
                    onSignOutCompleted = {
                        replaceRoot(backStack, Screen.Login)
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