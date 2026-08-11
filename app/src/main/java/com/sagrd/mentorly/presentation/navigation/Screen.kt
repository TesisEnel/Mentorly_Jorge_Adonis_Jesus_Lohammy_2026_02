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
}
