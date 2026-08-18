package com.sagrd.mentorly.presentation.admin.peerreview.rubric

import com.sagrd.mentorly.domain.model.peerreview.PeerReviewRubricCriterion

data class AdminPeerReviewRubricUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isDeleting: Boolean = false,
    val isCriterionSaved: Boolean = false,
    val hasSession: Boolean = true,
    val hasAdminAccess: Boolean = true,
    val criteria: List<PeerReviewRubricCriterion> = emptyList(),
    val editingCriterionId: String? = null,
    val title: String = "",
    val description: String = "",
    val maxScore: String = "5",
    val orderIndex: String = "0",
    val titleError: String? = null,
    val descriptionError: String? = null,
    val maxScoreError: String? = null,
    val orderError: String? = null,
    val errorMessage: String? = null,
)
