package com.sagrd.mentorly.presentation.admin.student.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sagrd.mentorly.data.remote.Resource
import com.sagrd.mentorly.domain.model.student.Student
import com.sagrd.mentorly.domain.model.student.StudentRole
import com.sagrd.mentorly.domain.repository.session.SessionRepository
import com.sagrd.mentorly.domain.repository.student.StudentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminStudentListViewModel @Inject constructor(
    private val studentRepository: StudentRepository,
    private val sessionRepository: SessionRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AdminStudentListUiState())
    val state: StateFlow<AdminStudentListUiState> = _state.asStateFlow()

    init {
        checkSessionAndLoad()
    }

    private fun checkSessionAndLoad() {
        viewModelScope.launch {
            sessionRepository.session.firstOrNull()?.let { session ->
                if (session.role == StudentRole.ADMIN) {
                    _state.update { it.copy(
                        hasSession = true,
                        hasAdminAccess = true,
                        currentAdminId = session.studentId
                    ) }
                    loadStudents()
                } else {
                    _state.update { it.copy(
                        hasAdminAccess = false,
                        errorMessage = "No tienes permisos para administrar estudiantes."
                    ) }
                }
            } ?: run {
                _state.update { it.copy(
                    hasSession = false,
                    errorMessage = "No se encontró una sesión activa."
                ) }
            }
        }
    }

    fun onEvent(event: AdminStudentListUiEvent) {
        when (event) {
            AdminStudentListUiEvent.Load -> loadStudents()
            AdminStudentListUiEvent.Refresh -> loadStudents(isRefreshing = true)
            is AdminStudentListUiEvent.SearchChanged -> {
                _state.update { it.copy(searchQuery = event.value) }
            }
            is AdminStudentListUiEvent.RequestPromotion -> {
                val student = _state.value.students.find { it.id == event.studentId }
                if (student != null) {
                    if (student.role == StudentRole.ADMIN) {
                        _state.update { it.copy(errorMessage = "El estudiante ya es administrador.") }
                    } else if (student.id == _state.value.currentAdminId) {
                        _state.update { it.copy(errorMessage = "No puedes promover tu propia cuenta.") }
                    } else {
                        _state.update { it.copy(studentPendingPromotion = student) }
                    }
                }
            }
            AdminStudentListUiEvent.ConfirmPromotion -> confirmPromotion()
            AdminStudentListUiEvent.DismissPromotionDialog -> {
                _state.update { it.copy(studentPendingPromotion = null) }
            }
            AdminStudentListUiEvent.ClearError -> {
                _state.update { it.copy(errorMessage = null) }
            }
            AdminStudentListUiEvent.ClearSuccessMessage -> {
                _state.update { it.copy(successMessage = null) }
            }
        }
    }

    private fun loadStudents(isRefreshing: Boolean = false) {
        if (!_state.value.hasAdminAccess) return

        viewModelScope.launch {
            studentRepository.getStudents().collect { result ->
                when (result) {
                    is Resource.Loading<*> -> {
                        _state.update { 
                            if (isRefreshing) it.copy(isRefreshing = true) 
                            else it.copy(isLoading = true) 
                        }
                    }
                    is Resource.Success -> {
                        _state.update { it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            students = result.data ?: emptyList(),
                            errorMessage = null
                        ) }
                    }
                    is Resource.Error -> {
                        _state.update { it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            errorMessage = result.message ?: "No se pudo cargar la lista de estudiantes."
                        ) }
                    }
                }
            }
        }
    }

    private fun confirmPromotion() {
        val student = _state.value.studentPendingPromotion ?: return
        val adminId = _state.value.currentAdminId ?: return

        _state.update { it.copy(studentPendingPromotion = null, promotingStudentId = student.id) }

        viewModelScope.launch {
            studentRepository.promoteToAdmin(adminId, student.id).collect { result ->
                when (result) {
                    is Resource.Loading -> { /* Handled by promotingStudentId */ }
                    is Resource.Success -> {
                        _state.update { it.copy(
                            promotingStudentId = null,
                            successMessage = "Estudiante promovido a administrador."
                        ) }
                        loadStudents()
                    }
                    is Resource.Error -> {
                        _state.update { it.copy(
                            promotingStudentId = null,
                            errorMessage = result.message ?: "No se pudo promover al estudiante."
                        ) }
                    }
                }
            }
        }
    }

    val filteredStudents = state.map { state ->
        if (state.searchQuery.isBlank()) {
            state.students
        } else {
            state.students.filter { 
                it.displayName.contains(state.searchQuery, ignoreCase = true) 
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
