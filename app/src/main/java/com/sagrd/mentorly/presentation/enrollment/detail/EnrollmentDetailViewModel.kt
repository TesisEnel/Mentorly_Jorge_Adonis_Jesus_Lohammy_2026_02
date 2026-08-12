package com.sagrd.mentorly.presentation.enrollment.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sagrd.mentorly.data.remote.Resource
import com.sagrd.mentorly.domain.model.enrollment.EnrollmentStatus
import com.sagrd.mentorly.domain.repository.enrollment.EnrollmentRepository
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
class EnrollmentDetailViewModel @Inject constructor(
    private val enrollmentRepository: EnrollmentRepository,
    private val sessionRepository: SessionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EnrollmentDetailUiState())
    val uiState: StateFlow<EnrollmentDetailUiState> = _uiState.asStateFlow()

    private var enrollmentId: String? = null

    fun initialize(id: String) {
        if (enrollmentId == id && (_uiState.value.enrollment != null || _uiState.value.isLoading)) return
        enrollmentId = id
        loadEnrollment()
    }

    fun onEvent(event: EnrollmentDetailUiEvent) {
        when (event) {
            EnrollmentDetailUiEvent.Refresh -> loadEnrollment()
            EnrollmentDetailUiEvent.LoadCertificate -> loadCertificate()
            EnrollmentDetailUiEvent.ShowRestartConfirmation -> _uiState.update { it.copy(isRestartConfirmationVisible = true) }
            EnrollmentDetailUiEvent.DismissRestartConfirmation -> _uiState.update { it.copy(isRestartConfirmationVisible = false) }
            EnrollmentDetailUiEvent.ConfirmRestart -> restartEnrollment()
            EnrollmentDetailUiEvent.ClearError -> _uiState.update { it.copy(errorMessage = null) }
        }
    }

    private fun loadEnrollment() {
        val id = enrollmentId ?: return
        if (_uiState.value.isLoading) return

        viewModelScope.launch {
            if (sessionRepository.session.first() == null) {
                _uiState.update { it.copy(hasSession = false, errorMessage = "No se encontró una sesión activa.") }
                return@launch
            }

            enrollmentRepository.getEnrollmentById(id).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _uiState.update { it.copy(isLoading = true, errorMessage = null, hasSession = true) }
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                enrollment = resource.data,
                                currentStatus = resource.data?.status,
                                errorMessage = null
                            )
                        }
                        loadStatus(id)
                    }
                    is Resource.Error -> _uiState.update {
                        it.copy(isLoading = false, errorMessage = resource.message ?: "No se pudo cargar la inscripción.")
                    }
                }
            }
        }
    }

    private fun loadStatus(id: String) {
        viewModelScope.launch {
            enrollmentRepository.getEnrollmentStatus(id).collect { resource ->
                if (resource is Resource.Success) {
                    _uiState.update { it.copy(currentStatus = resource.data ?: it.currentStatus) }
                }
            }
        }
    }

    private fun loadCertificate() {
        val id = enrollmentId ?: return
        if (_uiState.value.isLoadingCertificate) return

        viewModelScope.launch {
            enrollmentRepository.getCertificate(id).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _uiState.update { it.copy(isLoadingCertificate = true, errorMessage = null) }
                    is Resource.Success -> _uiState.update { it.copy(isLoadingCertificate = false, certificate = resource.data) }
                    is Resource.Error -> _uiState.update {
                        it.copy(isLoadingCertificate = false, errorMessage = resource.message ?: "No se pudo cargar el certificado.")
                    }
                }
            }
        }
    }

    private fun restartEnrollment() {
        val enrollment = _uiState.value.enrollment ?: return
        if (_uiState.value.isRestarting || _uiState.value.currentStatus != EnrollmentStatus.EXPIRED) return

        viewModelScope.launch {
            val session = sessionRepository.session.first()
            if (session == null) {
                _uiState.update { it.copy(errorMessage = "No se encontró una sesión activa.") }
                return@launch
            }

            enrollmentRepository.restartEnrollment(session.studentId, enrollment.courseId).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _uiState.update { it.copy(isRestarting = true, errorMessage = null) }
                    is Resource.Success -> _uiState.update {
                        it.copy(
                            isRestarting = false,
                            isRestartConfirmationVisible = false,
                            restartedEnrollmentId = resource.data?.enrollmentId
                        )
                    }
                    is Resource.Error -> _uiState.update {
                        it.copy(isRestarting = false, isRestartConfirmationVisible = false, errorMessage = resource.message ?: "No se pudo reiniciar la inscripción.")
                    }
                }
            }
        }
    }
}
