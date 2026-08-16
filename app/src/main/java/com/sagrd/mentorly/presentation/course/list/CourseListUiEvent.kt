package com.sagrd.mentorly.presentation.course.list

sealed interface CourseListUiEvent {
    data object Refresh : CourseListUiEvent
    data class SearchQueryChanged(val query: String) : CourseListUiEvent
    data object ClearError : CourseListUiEvent
}
