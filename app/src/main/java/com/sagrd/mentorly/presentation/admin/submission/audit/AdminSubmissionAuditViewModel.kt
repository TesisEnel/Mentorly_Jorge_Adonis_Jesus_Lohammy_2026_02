package com.sagrd.mentorly.presentation.admin.submission.audit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sagrd.mentorly.data.remote.Resource
import com.sagrd.mentorly.data.remote.dto.submission.AdminSubmissionDecisionDto
import com.sagrd.mentorly.domain.model.student.StudentRole
import com.sagrd.mentorly.domain.repository.session.SessionRepository
import com.sagrd.mentorly.domain.repository.submission.SubmissionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminSubmissionAuditViewModel @Inject constructor(
    private val submissionRepository: SubmissionRepository,
    private val sessionRepository: SessionRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminSubmissionAuditUiState())
    val uiState: StateFlow<AdminSubmissionAuditUiState> = _uiState.asStateFlow()

    private var submissionId: String = ""

    fun setSubmissionId(id: String) {
        if (submissionId != id) {
            submissionId = id
            onEvent(AdminSubmissionAuditUiEvent.Load)
        }
    }

    fun onEvent(event: AdminSubmissionAuditUiEvent) {
        when (event) {
            AdminSubmissionAuditUiEvent.Load -> loadAudit()
            AdminSubmissionAuditUiEvent.Retry -> loadAudit()
            is AdminSubmissionAuditUiEvent.RequestDecision -> {
                _uiState.update { it.copy(pendingDecision = event.isApproved) }
            }
            AdminSubmissionAuditUiEvent.ConfirmDecision -> confirmDecision()
            AdminSubmissionAuditUiEvent.DismissDecisionDialog -> {
                _uiState.update { it.copy(pendingDecision = null) }
            }
            AdminSubmissionAuditUiEvent.ClearError -> {
                _uiState.update { it.copy(errorMessage = null) }
            }
            AdminSubmissionAuditUiEvent.ClearSuccessMessage -> {
                _uiState.update { it.copy(successMessage = null) }
            }
        }
    }

    private fun loadAudit() {
        if (submissionId.isBlank()) return

        viewModelScope.launch {
            val session = sessionRepository.session.firstOrNull()
            if (session == null) {
                _uiState.update { it.copy(hasSession = false, errorMessage = "No se encontró una sesión activa.") }
                return@launch
            }

            if (session.role != StudentRole.ADMIN) {
                _uiState.update { it.copy(hasAdminAccess = false, errorMessage = "No tienes permisos para consultar la auditoría.") }
                return@launch
            }

            val adminId = session.studentId

            submissionRepository.getEscalatedSubmissionAudit(adminId, submissionId).collect { result ->
                when (result) {
                    is Resource.Loading<*> -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                    is Resource.Success -> {
                        _uiState.update { it.copy(
                            isLoading = false,
                            audit = result.data,
                            errorMessage = null,
                        ) }
                    }
                    is Resource.Error -> {
                        _uiState.update { it.copy(
                            isLoading = false,
                            errorMessage = result.message ?: "No se pudo cargar la auditoría de la entrega."
                        ) }
                    }
                }
            }
        }
    }

    private fun confirmDecision() {
        val isApproved = _uiState.value.pendingDecision ?: return
        _uiState.update { it.copy(pendingDecision = null, isDeciding = true) }

        viewModelScope.launch {
            val session = sessionRepository.session.firstOrNull()
            val adminId = session?.studentId ?: return@launch

            val decision = AdminSubmissionDecisionDto(
                isApproved = isApproved,
                feedbackComment = if (isApproved) "Aprobada por administración." else "Rechazada por administración."
            )

            submissionRepository.decideSubmission(adminId, submissionId, decision).collect { result ->
                when (result) {
                    is Resource.Loading<*> -> { /* Handled by isDeciding */ }
                    is Resource.Success -> {
                        _uiState.update { it.copy(
                            isDeciding = false,
                            successMessage = if (isApproved) "La entrega fue aprobada." else "La entrega fue rechazada."
                        ) }
                        loadAudit()
                    }
                    is Resource.Error -> {
                        _uiState.update { it.copy(
                            isDeciding = false,
                            errorMessage = result.message ?: "No se pudo registrar la decisión administrativa."
                        ) }
                    }
                }
            }
        }
    }
}
