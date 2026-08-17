package com.sagrd.mentorly.presentation.submission.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sagrd.mentorly.data.remote.Resource
import com.sagrd.mentorly.domain.model.content.ApprovalStrategy
import com.sagrd.mentorly.domain.model.submission.Submission
import com.sagrd.mentorly.domain.model.submission.SubmissionStatus
import com.sagrd.mentorly.domain.repository.course.CourseRepository
import com.sagrd.mentorly.domain.repository.enrollment.EnrollmentRepository
import com.sagrd.mentorly.domain.repository.session.SessionRepository
import com.sagrd.mentorly.domain.repository.submission.SubmissionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class SubmissionDetailViewModel @Inject constructor(
    private val submissionRepository: SubmissionRepository,
    private val sessionRepository: SessionRepository,
    private val enrollmentRepository: EnrollmentRepository,
    private val courseRepository: CourseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SubmissionDetailUiState())
    val uiState: StateFlow<SubmissionDetailUiState> = _uiState.asStateFlow()

    private var currentSubmissionId: String? = null

    fun onEvent(event: SubmissionDetailUiEvent) {
        when (event) {
            is SubmissionDetailUiEvent.Load -> {
                currentSubmissionId = event.submissionId
                load(event.submissionId)
            }
            is SubmissionDetailUiEvent.Refresh -> {
                currentSubmissionId?.let { load(it) }
            }
            is SubmissionDetailUiEvent.Escalate -> escalate()
            is SubmissionDetailUiEvent.DismissError -> _uiState.update { it.copy(errorMessage = null) }
        }
    }

    private fun load(submissionId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val session = sessionRepository.session.first()
            val studentId = session?.studentId

            if (studentId == null) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Sesión no encontrada") }
            } else {
                submissionRepository.getSubmissionById(submissionId).collect { resource ->
                    when (resource) {
                        is Resource.Loading -> _uiState.update { it.copy(isLoading = true) }
                        is Resource.Success -> {
                            val submission = resource.data
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    submission = submission,
                                    canEscalate = calculateCanEscalate(submission, it.approvalStrategy)
                                )
                            }
                            if (submission != null) {
                                loadCourseMetadata(submission.enrollmentId, submission.activityId)
                                loadReviews(studentId, submissionId)
                            }
                        }
                        is Resource.Error -> _uiState.update {
                            it.copy(isLoading = false, errorMessage = resource.message)
                        }
                    }
                }
            }
        }
    }

    private fun loadCourseMetadata(enrollmentId: String, activityId: String) {
        viewModelScope.launch {
            enrollmentRepository.getEnrollmentById(enrollmentId).collect { enrollmentRes ->
                if (enrollmentRes is Resource.Success && enrollmentRes.data != null) {
                    val courseId = enrollmentRes.data.courseId
                    courseRepository.getCourseContent(courseId).collect { courseRes ->
                        if (courseRes is Resource.Success && courseRes.data != null) {
                            val course = courseRes.data
                            val foundActivity = course.units.flatMap { it.themes }.flatMap { it.activities }
                                .firstOrNull { it.id == activityId }

                            val strategy = foundActivity?.approvalStrategy
                            val strategyText = when (strategy) {
                                ApprovalStrategy.PEER_REVIEW -> "Revisión entre pares"
                                ApprovalStrategy.AUTO -> "Aprobación automática"
                                ApprovalStrategy.ADMIN -> "Revisión del instructor"
                                null -> "Cargando información..."
                            }

                            _uiState.update {
                                it.copy(
                                    requiredReviewsCount = course.requiredPeerReviews,
                                    approvalStrategy = strategy,
                                    approvalStrategyText = strategyText,
                                    canEscalate = calculateCanEscalate(it.submission, strategy)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun loadReviews(studentId: String, submissionId: String) {
        viewModelScope.launch {
            submissionRepository.getSubmissionReviews(studentId, submissionId).collect { resource ->
                if (resource is Resource.Success) {
                    _uiState.update { it.copy(reviews = resource.data ?: emptyList()) }
                }
            }
        }
    }

    private fun escalate() {
        val submissionId = currentSubmissionId ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isEscalating = true, errorMessage = null) }

            val session = sessionRepository.session.first()
            val studentId = session?.studentId

            if (studentId == null) {
                _uiState.update { it.copy(isEscalating = false, errorMessage = "Sesión no encontrada") }
            } else {
                submissionRepository.escalateSubmission(studentId, submissionId).collect { resource ->
                    when (resource) {
                        is Resource.Success -> {
                            _uiState.update {
                                it.copy(
                                    isEscalating = false,
                                    submission = it.submission?.copy(status = SubmissionStatus.ESCALATED),
                                    canEscalate = false
                                )
                            }
                        }
                        is Resource.Error -> _uiState.update {
                            it.copy(isEscalating = false, errorMessage = resource.message)
                        }
                        is Resource.Loading -> Unit
                    }
                }
            }
        }
    }

    private fun calculateCanEscalate(
        submission: Submission?,
        strategy: ApprovalStrategy?
    ): Boolean {
        if (submission == null) return false
        if (strategy != ApprovalStrategy.PEER_REVIEW) return false

        return when (submission.status) {
            SubmissionStatus.REJECTED -> true
            SubmissionStatus.PENDING -> isOver72Hours(submission.submittedAt)
            SubmissionStatus.APPROVED,
            SubmissionStatus.ESCALATED,
            SubmissionStatus.UNKNOWN -> false
        }
    }

    private fun isOver72Hours(submittedAt: String): Boolean {
        if (submittedAt.isBlank()) return false
        return runCatching {
            val submittedInstant = if ('T' in submittedAt) {
                runCatching { OffsetDateTime.parse(submittedAt).toInstant() }
                    .getOrElse { LocalDateTime.parse(submittedAt).toInstant(ZoneOffset.UTC) }
            } else {
                LocalDate.parse(submittedAt).atStartOfDay().toInstant(ZoneOffset.UTC)
            }
            val duration = Duration.between(submittedInstant, Instant.now())
            duration.toHours() >= 72
        }.getOrDefault(false)
    }
}