package com.sagrd.mentorly.presentation.progress

import com.sagrd.mentorly.domain.model.enrollment.Enrollment
import com.sagrd.mentorly.domain.model.progress.EnrollmentProgress
import com.sagrd.mentorly.domain.model.submission.Submission

data class EnrollmentProgressUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val progress: EnrollmentProgress? = null,
    val enrollment: Enrollment? = null,
    val courseImageUrl: String? = null,
    val expandedUnitIds: Set<String> = emptySet(),
    val completingThemeIds: Set<String> = emptySet(),
    val submissionsByActivityId: Map<String, Submission> = emptyMap(),
    val errorMessage: String? = null
)
