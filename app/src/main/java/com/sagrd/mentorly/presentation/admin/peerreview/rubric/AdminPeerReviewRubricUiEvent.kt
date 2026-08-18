package com.sagrd.mentorly.presentation.admin.peerreview.rubric

sealed interface AdminPeerReviewRubricUiEvent {
    data class Load(val activityId: String) : AdminPeerReviewRubricUiEvent
    data class TitleChanged(val value: String) : AdminPeerReviewRubricUiEvent
    data class DescriptionChanged(val value: String) : AdminPeerReviewRubricUiEvent
    data class MaxScoreChanged(val value: String) : AdminPeerReviewRubricUiEvent
    data class OrderChanged(val value: String) : AdminPeerReviewRubricUiEvent
    data class EditCriterion(val criterionId: String) : AdminPeerReviewRubricUiEvent
    data object SaveCriterion : AdminPeerReviewRubricUiEvent
    data object CancelEdit : AdminPeerReviewRubricUiEvent
    data object DeleteCriterion : AdminPeerReviewRubricUiEvent
    data object ClearError : AdminPeerReviewRubricUiEvent
    data object CriterionSavedHandled : AdminPeerReviewRubricUiEvent
}
