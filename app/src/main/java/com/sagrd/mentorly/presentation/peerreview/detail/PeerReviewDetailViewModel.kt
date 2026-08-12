package com.sagrd.mentorly.presentation.peerreview.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sagrd.mentorly.data.remote.Resource
import com.sagrd.mentorly.data.remote.dto.peerreview.CreatePeerReviewRequestDto
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

    private val _uiState = MutableStateFlow(PeerReviewDetailUiState())
    val uiState: StateFlow<PeerReviewDetailUiState> = _uiState.asStateFlow()

    private var submissionId: String? = null

    fun initialize(id: String) {
        if (submissionId == id && (_uiState.value.submission != null || _uiState.value.isLoading)) return
        submissionId = id
        loadSubmission()
    }

    fun onEvent(event: PeerReviewDetailUiEvent) {
        when (event) {
            is PeerReviewDetailUiEvent.DecisionChanged -> _uiState.update {
                it.copy(isApproved = event.isApproved, decisionError = null)
            }

            is PeerReviewDetailUiEvent.FeedbackChanged -> _uiState.update {
                it.copy(feedbackComment = event.value, feedbackError = null)
            }

            PeerReviewDetailUiEvent.Submit -> submitReview()
            PeerReviewDetailUiEvent.Retry -> loadSubmission()
            PeerReviewDetailUiEvent.ClearError -> _uiState.update { it.copy(errorMessage = null) }
        }
    }

    private fun loadSubmission() {
        val id = submissionId ?: return
        if (_uiState.value.isLoading) return

        viewModelScope.launch {
            val session = sessionRepository.session.first()
            if (session == null) {
                _uiState.update {
                    it.copy(
                        hasSession = false,
                        errorMessage = "No se encontró una sesión activa."
                    )
                }
                return@launch
            }

            peerReviewRepository.getAnonymousSubmission(session.studentId, id).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _uiState.update {
                        it.copy(isLoading = true, errorMessage = null, hasSession = true)
                    }

                    is Resource.Success -> _uiState.update {
                        it.copy(isLoading = false, submission = resource.data, errorMessage = null)
                    }

                    is Resource.Error -> _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = resource.message ?: "No se pudo cargar la entrega anónima."
                        )
                    }
                }
            }
        }
    }

    private fun submitReview() {
        val id = submissionId ?: return
        val state = _uiState.value
        if (state.isSubmitting || state.result != null) return

        val approved = state.isApproved
        val feedback = state.feedbackComment.trim()
        val decisionError = if (approved == null) "Selecciona una decisión." else null
        val feedbackError = if (feedback.isBlank()) "El comentario es obligatorio." else null

        if (decisionError != null || feedbackError != null) {
            _uiState.update {
                it.copy(decisionError = decisionError, feedbackError = feedbackError)
            }
            return
        }

        val selectedDecision = approved ?: return

        viewModelScope.launch {
            val session = sessionRepository.session.first()
            if (session == null) {
                _uiState.update { it.copy(errorMessage = "No se encontró una sesión activa.") }
                return@launch
            }

            peerReviewRepository.submitReview(
                studentId = session.studentId,
                dto = CreatePeerReviewRequestDto(
                    submissionId = id,
                    isApproved = selectedDecision,
                    feedbackComment = feedback
                )
            ).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _uiState.update {
                        it.copy(isSubmitting = true, errorMessage = null)
                    }

                    is Resource.Success -> _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            result = resource.data,
                            errorMessage = null
                        )
                    }

                    is Resource.Error -> _uiState.update {
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
