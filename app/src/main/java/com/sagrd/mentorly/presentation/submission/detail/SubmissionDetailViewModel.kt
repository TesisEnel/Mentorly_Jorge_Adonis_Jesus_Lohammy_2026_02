package com.sagrd.mentorly.presentation.submission.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sagrd.mentorly.data.remote.Resource
import com.sagrd.mentorly.domain.model.submission.SubmissionStatus
import com.sagrd.mentorly.domain.repository.session.SessionRepository
import com.sagrd.mentorly.domain.repository.submission.SubmissionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SubmissionDetailViewModel @Inject constructor(
    private val submissionRepository: SubmissionRepository,
    private val sessionRepository: SessionRepository
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
            is SubmissionDetailUiEvent.EditClicked -> Unit
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
                return@launch
            }

            submissionRepository.getSubmissionById(submissionId).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _uiState.update { it.copy(isLoading = true) }
                    is Resource.Success -> {
                        _uiState.update { it.copy(isLoading = false, submission = resource.data) }
                        loadReviews(studentId, submissionId)
                    }
                    is Resource.Error -> _uiState.update {
                        it.copy(isLoading = false, errorMessage = resource.message)
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
                return@launch
            }

            submissionRepository.escalateSubmission(studentId, submissionId).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                isEscalating = false,
                                submission = it.submission?.copy(status = SubmissionStatus.ESCALATED)
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