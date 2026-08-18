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

    private val _state = MutableStateFlow(AdminStudentDetailUiState())
    val state: StateFlow<AdminStudentDetailUiState> = _state.asStateFlow()

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
            AdminStudentDetailUiEvent.ClearError -> _state.update { it.copy(errorMessage = null) }
        }
    }

    private fun checkSessionAndLoad() {
        viewModelScope.launch {
            val session = sessionRepository.session.firstOrNull()
            if (session != null) {
                if (session.role == StudentRole.ADMIN) {
                    _state.update { it.copy(hasSession = true, hasAdminAccess = true) }
                    loadData(session.studentId)
                } else {
                    _state.update {
                        it.copy(
                            hasAdminAccess = false,
                            errorMessage = "No tienes permisos para ver el detalle."
                        )
                    }
                }
            } else {
                _state.update {
                    it.copy(
                        hasSession = false,
                        errorMessage = "No se encontró una sesión activa."
                    )
                }
            }
        }
    }

    private fun loadData(adminId: String) {
        if (studentId.isBlank()) return

        _state.update { it.copy(isLoading = true, isLoadingEnrollments = true) }

        viewModelScope.launch {
            // Load Student Profile
            launch {
                studentRepository.getStudentById(studentId).collect { result ->
                    when (result) {
                        is Resource.Success -> _state.update { it.copy(student = result.data, isLoading = false) }
                        is Resource.Error -> _state.update { it.copy(errorMessage = result.message, isLoading = false) }
                        else -> {}
                    }
                }
            }

            // Load Enrollments
            launch {
                enrollmentRepository.getAdminStudentEnrollments(adminId, studentId).collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            _state.update {
                                it.copy(
                                    enrollments = result.data ?: emptyList(),
                                    isLoadingEnrollments = false
                                )
                            }
                        }
                        is Resource.Error -> {
                            _state.update {
                                it.copy(
                                    errorMessage = result.message,
                                    isLoadingEnrollments = false
                                )
                            }
                        }
                        else -> {}
                    }
                }
            }
        }
    }

    private fun toggleExpansion(enrollmentId: String) {
        val currentExpanded = _state.value.expandedEnrollmentIds
        if (currentExpanded.contains(enrollmentId)) {
            _state.update { it.copy(expandedEnrollmentIds = currentExpanded - enrollmentId) }
        } else {
            _state.update { it.copy(expandedEnrollmentIds = currentExpanded + enrollmentId) }
            loadProgress(enrollmentId)
        }
    }

    private fun loadProgress(enrollmentId: String) {
        viewModelScope.launch {
            val session = sessionRepository.session.firstOrNull()
            if (session != null) {
                val adminId = session.studentId
                progressRepository.getAdminEnrollmentProgress(adminId, enrollmentId).collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            if (result.data != null) {
                                _state.update { state ->
                                    state.copy(
                                        enrollmentProgress = state.enrollmentProgress + (enrollmentId to result.data!!),
                                        enrollmentErrors = state.enrollmentErrors - enrollmentId
                                    )
                                }
                            }
                        }
                        is Resource.Error -> {
                            _state.update { state ->
                                state.copy(
                                    enrollmentErrors = state.enrollmentErrors + (enrollmentId to (result.message ?: "Error al cargar")),
                                    enrollmentProgress = state.enrollmentProgress - enrollmentId
                                )
                            }
                        }
                        else -> {}
                    }
                }
            }
        }
    }
}
