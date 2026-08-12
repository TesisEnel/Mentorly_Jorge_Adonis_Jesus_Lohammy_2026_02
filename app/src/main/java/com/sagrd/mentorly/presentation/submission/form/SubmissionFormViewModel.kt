package com.sagrd.mentorly.presentation.submission.form

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sagrd.mentorly.data.remote.Resource
import com.sagrd.mentorly.data.remote.dto.submission.CreateSubmissionDto
import com.sagrd.mentorly.data.remote.dto.submission.UpdateSubmissionDto
import com.sagrd.mentorly.domain.model.submission.Submission
import com.sagrd.mentorly.domain.repository.submission.SubmissionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SubmissionFormViewModel @Inject constructor(
    private val submissionRepository: SubmissionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SubmissionFormUiState())
    val uiState: StateFlow<SubmissionFormUiState> = _uiState.asStateFlow()

    private var enrollmentId: String = ""
    private var activityId: String = ""
    private var submissionId: String? = null

    fun onEvent(event: SubmissionFormUiEvent) {
        when (event) {
            is SubmissionFormUiEvent.Load -> load(event.enrollmentId, event.activityId, event.submissionId)
            is SubmissionFormUiEvent.EvidenceUrlChanged -> _uiState.update {
                it.copy(evidenceUrl = event.value, evidenceUrlError = null)
            }
            is SubmissionFormUiEvent.Save -> save()
            is SubmissionFormUiEvent.DismissError -> _uiState.update { it.copy(errorMessage = null) }
        }
    }

    private fun load(enrollmentId: String, activityId: String, submissionId: String?) {
        this.enrollmentId = enrollmentId
        this.activityId = activityId
        this.submissionId = submissionId

        if (submissionId == null) {
            _uiState.update {
                it.copy(
                    isEditing = false,
                    evidenceUrl = "",
                    savedSubmissionId = null
                )
            }
            return
        }

        _uiState.update {
            it.copy(
                isEditing = true,
                isLoading = true,
                savedSubmissionId = null
            )
        }

        viewModelScope.launch {
            submissionRepository.getSubmissionById(submissionId).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _uiState.update { it.copy(isLoading = true) }
                    is Resource.Success -> _uiState.update {
                        it.copy(isLoading = false, evidenceUrl = resource.data?.evidenceUrl ?: "")
                    }
                    is Resource.Error -> _uiState.update {
                        it.copy(isLoading = false, errorMessage = resource.message)
                    }
                }
            }
        }
    }

    private fun save() {
        val state = _uiState.value
        val validationError = validateEvidenceUrl(state.evidenceUrl)

        if (validationError != null) {
            _uiState.update { it.copy(evidenceUrlError = validationError) }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSaving = true,
                    errorMessage = null,
                    savedSubmissionId = null
                )
            }

            val currentSubmissionId = submissionId

            if (currentSubmissionId == null) {
                submissionRepository.createSubmission(
                    enrollmentId,
                    activityId,
                    CreateSubmissionDto(evidenceUrl = state.evidenceUrl)
                ).collect(::handleCreateResult)
            } else {
                submissionRepository.updateSubmission(
                    currentSubmissionId,
                    UpdateSubmissionDto(evidenceUrl = state.evidenceUrl)
                ).collect { resource ->
                    handleUpdateResult(resource, currentSubmissionId)
                }
            }
        }
    }

    private fun handleCreateResult(resource: Resource<Submission>) {
        when (resource) {
            is Resource.Success -> {
                val createdSubmissionId = resource.data?.id
                _uiState.update {
                    if (createdSubmissionId == null) {
                        it.copy(
                            isSaving = false,
                            errorMessage = "No se pudo obtener la entrega guardada."
                        )
                    } else {
                        it.copy(
                            isSaving = false,
                            savedSubmissionId = createdSubmissionId
                        )
                    }
                }
            }
            is Resource.Error -> _uiState.update { it.copy(isSaving = false, errorMessage = resource.message) }
            is Resource.Loading -> Unit
        }
    }

    private fun handleUpdateResult(
        resource: Resource<Unit>,
        updatedSubmissionId: String
    ) {
        when (resource) {
            is Resource.Success -> _uiState.update {
                it.copy(
                    isSaving = false,
                    savedSubmissionId = updatedSubmissionId
                )
            }
            is Resource.Error -> _uiState.update { it.copy(isSaving = false, errorMessage = resource.message) }
            is Resource.Loading -> Unit
        }
    }

    private fun validateEvidenceUrl(url: String): String? {
        val trimmed = url.trim()
        if (trimmed.isEmpty()) {
            return "El enlace de evidencia es obligatorio."
        }
        val isAbsoluteHttp = trimmed.startsWith("http://") || trimmed.startsWith("https://")
        if (!isAbsoluteHttp) {
            return "El enlace debe ser una URL absoluta http o https."
        }
        return null
    }
}
