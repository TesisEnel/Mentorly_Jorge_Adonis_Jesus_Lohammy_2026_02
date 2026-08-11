package com.sagrd.mentorly.presentation.startup

data class StartupUiState(
    val isLoading: Boolean = true,
    val destination: StartupDestination? = null,
    val errorMessage: String? = null
)

enum class StartupDestination {
    LOGIN,
    COURSE_LIST
}
