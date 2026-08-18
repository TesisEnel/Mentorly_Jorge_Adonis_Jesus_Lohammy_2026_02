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

    private val _state = MutableStateFlow(AdminSubmissionAuditUiState())
    val state: StateFlow<AdminSubmissionAuditUiState> = _state.asStateFlow()

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
                _state.update { it.copy(pendingDecision = event.isApproved) }
            }
            AdminSubmissionAuditUiEvent.ConfirmDecision -> confirmDecision()
            AdminSubmissionAuditUiEvent.DismissDecisionDialog -> {
                _state.update { it.copy(pendingDecision = null) }
            }
            AdminSubmissionAuditUiEvent.ClearError -> {
                _state.update { it.copy(errorMessage = null) }
            }
            AdminSubmissionAuditUiEvent.ClearSuccessMessage -> {
                _state.update { it.copy(successMessage = null) }
            }
        }
    }

    private fun loadAudit() {
        if (submissionId.isNotBlank()) {
            viewModelScope.launch {
                val session = sessionRepository.session.firstOrNull()
                if (session != null) {
                    if (session.role == StudentRole.ADMIN) {
                        val adminId = session.studentId
                        submissionRepository.getEscalatedSubmissionAudit(adminId, submissionId).collect { result ->
                            when (result) {
                                is Resource.Loading<*> -> {
                                    _state.update { it.copy(isLoading = true) }
                                }
                                is Resource.Success -> {
                                    _state.update { it.copy(
                                        isLoading = false,
                                        audit = result.data,
                                        errorMessage = null,
                                    ) }
                                }
                                is Resource.Error -> {
                                    _state.update { it.copy(
                                        isLoading = false,
                                        errorMessage = result.message ?: "No se pudo cargar la auditoría de la entrega."
                                    ) }
                                }
                            }
                        }
                    } else {
                        _state.update { it.copy(hasAdminAccess = false, errorMessage = "No tienes permisos para administrar entregas escaladas.") }
                    }
                } else {
                    _state.update { it.copy(hasSession = false, errorMessage = "No se encontró una sesión activa.") }
                }
            }
        }
    }

    private fun confirmDecision() {
        val isApproved = _state.value.pendingDecision
        if (isApproved != null) {
            _state.update { it.copy(pendingDecision = null, isDeciding = true) }

            viewModelScope.launch {
                val session = sessionRepository.session.firstOrNull()
                if (session != null) {
                    val adminId = session.studentId
                    val decision = AdminSubmissionDecisionDto(
                        isApproved = isApproved,
                        feedbackComment = if (isApproved) "Aprobada por administración." else "Rechazada por administración."
                    )

                    submissionRepository.decideSubmission(adminId, submissionId, decision).collect { result ->
                        when (result) {
                            is Resource.Loading<*> -> { /* Handled by isDeciding */ }
                            is Resource.Success -> {
                                _state.update { it.copy(
                                    isDeciding = false,
                                    successMessage = if (isApproved) "La entrega fue aprobada." else "La entrega fue rechazada."
                                ) }
                                loadAudit()
                            }
                            is Resource.Error -> {
                                _state.update { it.copy(
                                    isDeciding = false,
                                    errorMessage = result.message ?: "No se pudo registrar la decisión administrativa."
                                ) }
                            }
                        }
                    }
                }
            }
        }
    }
}
