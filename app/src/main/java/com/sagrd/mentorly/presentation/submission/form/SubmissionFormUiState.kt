package com.sagrd.mentorly.presentation.submission.form

import com.sagrd.mentorly.domain.model.content.ApprovalStrategy
import com.sagrd.mentorly.domain.model.submission.EvidenceType

data class SubmissionFormUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isEditing: Boolean = false,
    val activityTitle: String = "Ejercicio",
    val activityDescription: String = "",
    val isMandatory: Boolean = true,
    val submissionStatus: String = "Pendiente de envío",
    val approvalStrategy: ApprovalStrategy = ApprovalStrategy.PEER_REVIEW,
    val requiredPeerReviews: Int = 3,
    val evidenceType: EvidenceType = EvidenceType.URL,
    val urlContent: String = "",
    val commentsContent: String = "",
    val textContent: String = "",
    val evidenceContentError: String? = null,
    val errorMessage: String? = null,
    val savedSubmissionId: String? = null
)
