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

    private val _state = MutableStateFlow(PeerReviewAuditUiState())
    val state: StateFlow<PeerReviewAuditUiState> = _state.asStateFlow()

    private var peerReviewId: String = ""

    fun setPeerReviewId(id: String) {
        if (peerReviewId != id) {
            peerReviewId = id
            onEvent(PeerReviewAuditUiEvent.Load)
        }
    }

    fun onEvent(event: PeerReviewAuditUiEvent) {
        when (event) {
            PeerReviewAuditUiEvent.Load -> checkSessionAndLoad()
            PeerReviewAuditUiEvent.Retry -> checkSessionAndLoad()
            PeerReviewAuditUiEvent.ClearError -> {
                _state.update { it.copy(errorMessage = null) }
            }
        }
    }

    private fun checkSessionAndLoad() {
        viewModelScope.launch {
            val session = sessionRepository.session.firstOrNull()
            if (session != null) {
                if (session.role == StudentRole.ADMIN) {
                    _state.update { it.copy(hasSession = true, hasAdminAccess = true) }
                    loadAudit(session.studentId)
                } else {
                    _state.update { it.copy(hasAdminAccess = false, errorMessage = "No tienes permisos para consultar la auditoría.") }
                }
            } else {
                _state.update { it.copy(hasSession = false, errorMessage = "No se encontró una sesión activa.") }
            }
        }
    }

    private fun loadAudit(adminId: String) {
        if (peerReviewId.isNotBlank()) {
            viewModelScope.launch {
                peerReviewRepository.getAudit(adminId, peerReviewId).collect { result ->
                    when (result) {
                        is Resource.Loading<*> -> {
                            _state.update { it.copy(isLoading = true) }
                        }
                        is Resource.Success -> {
                            _state.update { it.copy(
                                isLoading = false,
                                audit = result.data,
                                errorMessage = null
                            ) }
                        }
                        is Resource.Error -> {
                            _state.update { it.copy(
                                isLoading = false,
                                errorMessage = result.message ?: "No se pudo cargar la auditoría de la revisión."
                            ) }
                        }
                    }
                }
            }
        }
    }
}
