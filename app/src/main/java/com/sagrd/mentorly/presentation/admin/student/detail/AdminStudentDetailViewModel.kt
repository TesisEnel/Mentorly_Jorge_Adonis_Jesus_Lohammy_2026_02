package com.sagrd.mentorly.presentation.admin.student.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sagrd.mentorly.data.remote.Resource
import com.sagrd.mentorly.domain.model.student.StudentRole
import com.sagrd.mentorly.domain.repository.enrollment.EnrollmentRepository
import com.sagrd.mentorly.domain.repository.progress.EnrollmentProgressRepository
import com.sagrd.mentorly.domain.repository.session.SessionRepository
import com.sagrd.mentorly.domain.repository.student.StudentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminStudentDetailViewModel @Inject constructor(
    private val studentRepository: StudentRepository,
    private val enrollmentRepository: EnrollmentRepository,
    private val progressRepository: EnrollmentProgressRepository,
    private val sessionRepository: SessionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminStudentDetailUiState())
    val uiState: StateFlow<AdminStudentDetailUiState> = _uiState.asStateFlow()

    private var studentId: String = ""

    fun setStudentId(id: String) {
        if (studentId != id) {
            studentId = id
            onEvent(AdminStudentDetailUiEvent.Load)
        }
    }

    fun onEvent(event: AdminStudentDetailUiEvent) {
        when (event) {
            AdminStudentDetailUiEvent.Load -> checkSessionAndLoad()
            AdminStudentDetailUiEvent.Refresh -> checkSessionAndLoad()
            is AdminStudentDetailUiEvent.ToggleEnrollmentExpansion -> toggleExpansion(event.enrollmentId)
            AdminStudentDetailUiEvent.ClearError -> _uiState.update { it.copy(errorMessage = null) }
        }
    }

    private fun checkSessionAndLoad() {
        viewModelScope.launch {
            sessionRepository.session.firstOrNull()?.let { session ->
                if (session.role == StudentRole.ADMIN) {
                    _uiState.update { it.copy(hasSession = true, hasAdminAccess = true) }
                    loadData(session.studentId)
                } else {
                    _uiState.update { it.copy(hasAdminAccess = false, errorMessage = "No tienes permisos para ver el detalle.") }
                }
            } ?: run {
                _uiState.update { it.copy(hasSession = false, errorMessage = "No se encontró una sesión activa.") }
            }
        }
    }

    private fun loadData(adminId: String) {
        if (studentId.isBlank()) return

        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            // Load Student Profile
            launch {
                studentRepository.getStudentById(studentId).collect { result ->
                    when (result) {
                        is Resource.Success -> _uiState.update { it.copy(student = result.data) }
                        is Resource.Error -> _uiState.update { it.copy(errorMessage = result.message) }
                        else -> {}
                    }
                }
            }

            // Load Enrollments
            launch {
                enrollmentRepository.getAdminStudentEnrollments(adminId, studentId).collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            _uiState.update { it.copy(
                                enrollments = result.data ?: emptyList(),
                                isLoading = false
                            ) }
                        }
                        is Resource.Error -> {
                            _uiState.update { it.copy(
                                errorMessage = result.message,
                                isLoading = false
                            ) }
                        }
                        else -> {}
                    }
                }
            }
        }
    }

    private fun toggleExpansion(enrollmentId: String) {
        val currentExpanded = _uiState.value.expandedEnrollmentIds
        if (currentExpanded.contains(enrollmentId)) {
            _uiState.update { it.copy(expandedEnrollmentIds = currentExpanded - enrollmentId) }
        } else {
            _uiState.update { it.copy(expandedEnrollmentIds = currentExpanded + enrollmentId) }
            loadProgressIfNeeded(enrollmentId)
        }
    }

    private fun loadProgressIfNeeded(enrollmentId: String) {
        if (_uiState.value.enrollmentProgress.containsKey(enrollmentId)) return

        viewModelScope.launch {
            val session = sessionRepository.session.firstOrNull()
            val adminId = session?.studentId ?: return@launch

            progressRepository.getAdminEnrollmentProgress(adminId, enrollmentId).collect { result ->
                if (result is Resource.Success && result.data != null) {
                    _uiState.update { state ->
                        state.copy(
                            enrollmentProgress = state.enrollmentProgress + (enrollmentId to result.data!!)
                        )
                    }
                }
            }
        }
    }
}
