package com.sagrd.mentorly.presentation.community.members

sealed interface CourseMembersUiEvent {
    data object Load : CourseMembersUiEvent
    data object Refresh : CourseMembersUiEvent
    data class SearchChanged(val value: String) : CourseMembersUiEvent
    data object ClearError : CourseMembersUiEvent
}
