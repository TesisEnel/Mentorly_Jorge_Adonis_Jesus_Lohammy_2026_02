package com.sagrd.mentorly.presentation.submission.list

import com.sagrd.mentorly.domain.model.submission.Submission

data class SubmissionListUiState(
    val isLoading: Boolean = true,
    val submissions: List<Submission> = emptyList(),
    val errorMessage: String? = null
)