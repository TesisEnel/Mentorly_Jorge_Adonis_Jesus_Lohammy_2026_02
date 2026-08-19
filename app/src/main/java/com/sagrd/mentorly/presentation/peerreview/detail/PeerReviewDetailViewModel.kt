package com.sagrd.mentorly.presentation.peerreview.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sagrd.mentorly.data.remote.Resource
import com.sagrd.mentorly.data.remote.dto.peerreview.CreatePeerReviewRequestDto
import com.sagrd.mentorly.data.remote.dto.peerreview.PeerReviewCriterionScoreDto
import com.sagrd.mentorly.domain.repository.peerreview.PeerReviewRepository
import com.sagrd.mentorly.domain.repository.session.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PeerReviewDetailViewModel @Inject constructor(
    private val peerReviewRepository: PeerReviewRepository,
    private val sessionRepository: SessionRepository
) : ViewModel() {

    private val _state = MutableStateFlow(PeerReviewDetailUiState())
    val state: StateFlow<PeerReviewDetailUiState> = _state.asStateFlow()

    private var submissionId: String? = null

    fun initialize(id: String) {
        if (submissionId == id && (_state.value.submission != null || _state.value.isLoading)) return
        submissionId = id
        loadSubmission()
    }

    fun onEvent(event: PeerReviewDetailUiEvent) {
        when (event) {
            is PeerReviewDetailUiEvent.CriterionScoreChanged -> _state.update { state ->
                val newScores = state.criterionScores + (event.criterionId to event.score)
                val newErrors = state.criterionErrors - event.criterionId
                state.copy(criterionScores = newScores, criterionErrors = newErrors)
            }

            is PeerReviewDetailUiEvent.DecisionChanged -> _state.update {
                it.copy(isApproved = event.isApproved, decisionError = null)
            }

            is PeerReviewDetailUiEvent.FeedbackChanged -> _state.update {
                it.copy(feedbackComment = event.value, feedbackError = null)
            }

            PeerReviewDetailUiEvent.Submit -> submitReview()
            PeerReviewDetailUiEvent.Retry -> loadSubmission()
            PeerReviewDetailUiEvent.ClearError -> _state.update { it.copy(errorMessage = null) }
        }
    }

    private fun loadSubmission() {
        val id = submissionId ?: return
        if (_state.value.isLoading) return

        viewModelScope.launch {
            val session = sessionRepository.session.first()
            if (session == null) {
                _state.update {
                    it.copy(
                        hasSession = false,
                        errorMessage = "No se encontró una sesión activa."
                    )
                }
            } else {
                peerReviewRepository.getAnonymousSubmission(session.studentId, id).collect { resource ->
                    when (resource) {
                        is Resource.Loading -> _state.update {
                            it.copy(isLoading = true, errorMessage = null, hasSession = true)
                        }

                        is Resource.Success -> {
                            val submission = resource.data
                            if (submission != null) {
                                loadRubricAndSetState(submission.activityId, submission)
                            } else {
                                _state.update {
                                    it.copy(
                                        isLoading = false,
                                        errorMessage = "No se encontraron datos de la entrega."
                                    )
                                }
                            }
                        }

                        is Resource.Error -> _state.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = resource.message ?: "No se pudo cargar la entrega anónima."
                            )
                        }
                    }
                }
            }
        }
    }

    private suspend fun loadRubricAndSetState(
        activityId: String,
        submission: com.sagrd.mentorly.domain.model.submission.AnonymousSubmission
    ) {
        peerReviewRepository.getRubric(activityId).collect { rubricResource ->
            val criteria = when (rubricResource) {
                is Resource.Success -> rubricResource.data.orEmpty()
                else -> emptyList()
            }

            _state.update {
                it.copy(
                    isLoading = false,
                    submission = submission,
                    criteria = criteria,
                    errorMessage = null
                )
            }
        }
    }

    private fun submitReview() {
        val id = submissionId ?: return
        val state = _state.value
        if (state.isSubmitting || state.result != null) return

        val missingCriterionIds = state.criteria
            .filter { !state.criterionScores.containsKey(it.id) }
            .map { it.id }
            .toSet()

        val approved = state.isApproved
        val feedback = state.feedbackComment.trim()
        val decisionError = if (approved == null) "Debes seleccionar una decisión." else null
        val feedbackError = if (feedback.isBlank()) "Este campo es obligatorio para enviar la revisión." else null

        if (missingCriterionIds.isNotEmpty() || decisionError != null || feedbackError != null) {
            _state.update {
                it.copy(
                    criterionErrors = missingCriterionIds,
                    decisionError = decisionError,
                    feedbackError = feedbackError
                )
            }
            return
        }

        val selectedDecision = approved ?: return
        val scoresList = if (state.criteria.isEmpty()) {
            emptyList()
        } else {
            state.criterionScores.map { (criterionId, score) ->
                PeerReviewCriterionScoreDto(
                    rubricCriterionId = criterionId,
                    score = score
                )
            }
        }

        viewModelScope.launch {
            val session = sessionRepository.session.first()
            if (session == null) {
                _state.update { it.copy(errorMessage = "No se encontró una sesión activa.") }
            } else {
                peerReviewRepository.submitReview(
                    studentId = session.studentId,
                    dto = CreatePeerReviewRequestDto(
                        submissionId = id,
                        isApproved = selectedDecision,
                        feedbackComment = feedback,
                        criterionScores = scoresList
                    )
                ).collect { resource ->
                    when (resource) {
                        is Resource.Loading -> _state.update {
                            it.copy(isSubmitting = true, errorMessage = null)
                        }

                        is Resource.Success -> _state.update {
                            it.copy(
                                isSubmitting = false,
                                result = resource.data,
                                errorMessage = null
                            )
                        }

                        is Resource.Error -> _state.update {
                            it.copy(
                                isSubmitting = false,
                                errorMessage = resource.message ?: "No se pudo enviar la revisión."
                            )
                        }
                    }
                }
            }
        }
    }
}
