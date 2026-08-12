package com.sagrd.mentorly.presentation.enrollment.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sagrd.mentorly.data.remote.Resource
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
class EnrollmentListViewModel @Inject constructor(
    private val enrollmentRepository: EnrollmentRepository,
    private val sessionRepository: SessionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EnrollmentListUiState())
    val uiState: StateFlow<EnrollmentListUiState> = _uiState.asStateFlow()

    init {
        loadEnrollments()
    }

    fun onEvent(event: EnrollmentListUiEvent) {
        when (event) {
            EnrollmentListUiEvent.Refresh -> loadEnrollments(isRefresh = true)
            EnrollmentListUiEvent.ClearError -> _uiState.update { it.copy(errorMessage = null) }
        }
    }

    private fun loadEnrollments(isRefresh: Boolean = false) {
        if (_uiState.value.isLoading || _uiState.value.isRefreshing) return

        viewModelScope.launch {
            val session = sessionRepository.session.first()
            if (session == null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        hasSession = false,
                        errorMessage = "No se encontró una sesión activa."
                    )
                }
                return@launch
            }

            enrollmentRepository.getEnrollments(session.studentId).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _uiState.update {
                        it.copy(
                            isLoading = !isRefresh,
                            isRefreshing = isRefresh,
                            errorMessage = null,
                            hasSession = true
                        )
                    }

                    is Resource.Success -> _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            enrollments = resource.data.orEmpty(),
                            errorMessage = null,
                            hasSession = true
                        )
                    }

                    is Resource.Error -> _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            errorMessage = resource.message
                                ?: "No se pudieron cargar las inscripciones."
                        )
                    }
                }
            }
        }
    }
}
