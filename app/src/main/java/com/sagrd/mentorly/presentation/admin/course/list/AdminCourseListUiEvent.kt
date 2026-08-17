package com.sagrd.mentorly.presentation.admin.course.list

sealed interface AdminCourseListUiEvent {
    data object Load : AdminCourseListUiEvent
    data object Refresh : AdminCourseListUiEvent
    data class SearchQueryChanged(val query: String) : AdminCourseListUiEvent
    data class TogglePublication(val courseId: String) : AdminCourseListUiEvent
    data class DeleteCourse(val courseId: String) : AdminCourseListUiEvent
    data object ClearError : AdminCourseListUiEvent
}
