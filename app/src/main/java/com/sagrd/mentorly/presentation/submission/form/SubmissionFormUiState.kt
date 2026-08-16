package com.sagrd.mentorly.presentation.submission.form

import com.sagrd.mentorly.domain.model.submission.EvidenceType

data class SubmissionFormUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isEditing: Boolean = false,
    val evidenceType: EvidenceType = EvidenceType.URL,
    val evidenceContent: String = "",
    val evidenceContentError: String? = null,
    val errorMessage: String? = null,
    val savedSubmissionId: String? = null
)
