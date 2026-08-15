package com.sagrd.mentorly.presentation.admin.peerreview.audit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sagrd.mentorly.data.remote.Resource
import com.sagrd.mentorly.domain.model.student.StudentRole
import com.sagrd.mentorly.domain.repository.peerreview.PeerReviewRepository
import com.sagrd.mentorly.domain.repository.session.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PeerReviewAuditViewModel @Inject constructor(
    private val peerReviewRepository: PeerReviewRepository,
    private val sessionRepository: SessionRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PeerReviewAuditUiState())
    val uiState: StateFlow<PeerReviewAuditUiState> = _uiState.asStateFlow()

    private var peerReviewId: String = ""

    fun setPeerReviewId(id: String) {
        if (peerReviewId != id) {
            peerReviewId = id
            onEvent(PeerReviewAuditUiEvent.Load)
        }
    }

    fun onEvent(event: PeerReviewAuditUiEvent) {
        when (event) {
            PeerReviewAuditUiEvent.Load -> loadAudit()
            PeerReviewAuditUiEvent.Retry -> loadAudit()
            PeerReviewAuditUiEvent.ClearError -> {
                _uiState.update { it.copy(errorMessage = null) }
            }
        }
    }

    private fun loadAudit() {
        if (peerReviewId.isBlank()) return

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

            peerReviewRepository.getAudit(adminId, peerReviewId).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                    is Resource.Success -> {
                        _uiState.update { it.copy(
                            isLoading = false,
                            audit = result.data,
                            errorMessage = null
                        ) }
                    }
                    is Resource.Error -> {
                        _uiState.update { it.copy(
                            isLoading = false,
                            errorMessage = result.message ?: "No se pudo cargar la auditoría de la revisión."
                        ) }
                    }
                }
            }
        }
    }
}
