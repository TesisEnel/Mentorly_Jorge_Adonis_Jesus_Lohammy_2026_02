package com.sagrd.mentorly.presentation.admin.student.list

sealed interface AdminStudentListUiEvent {
    data object Load : AdminStudentListUiEvent
    data object Refresh : AdminStudentListUiEvent
    data class SearchChanged(val value: String) : AdminStudentListUiEvent
    data class RequestPromotion(val studentId: String) : AdminStudentListUiEvent
    data object ConfirmPromotion : AdminStudentListUiEvent
    data object DismissPromotionDialog : AdminStudentListUiEvent
    data object ClearError : AdminStudentListUiEvent
    data object ClearSuccessMessage : AdminStudentListUiEvent
}
