package com.sagrd.mentorly.presentation.course.list

sealed interface CourseListUiEvent {
    data object Refresh : CourseListUiEvent
}
