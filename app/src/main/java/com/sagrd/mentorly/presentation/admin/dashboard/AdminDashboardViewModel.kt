package com.sagrd.mentorly.presentation.admin.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sagrd.mentorly.data.remote.Resource
import com.sagrd.mentorly.domain.model.student.StudentRole
import com.sagrd.mentorly.domain.repository.analytics.AnalyticsRepository
import com.sagrd.mentorly.domain.repository.session.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminDashboardViewModel @Inject constructor(
    private val analyticsRepository: AnalyticsRepository,
    private val sessionRepository: SessionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminDashboardUiState())
    val uiState: StateFlow<AdminDashboardUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    init {
        onEvent(AdminDashboardUiEvent.Load)
    }

    fun onEvent(event: AdminDashboardUiEvent) {
        when (event) {
            AdminDashboardUiEvent.Load -> loadOverview(isRefresh = false)
            AdminDashboardUiEvent.Refresh -> loadOverview(isRefresh = true)
            AdminDashboardUiEvent.ClearError -> _uiState.update { it.copy(errorMessage = null) }
        }
    }

    private fun loadOverview(isRefresh: Boolean) {
        if (loadJob?.isActive == true) return

        loadJob = viewModelScope.launch {
            val session = sessionRepository.session.first()

            if (session == null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        hasSession = false,
                        hasAdminAccess = false,
                        errorMessage = "No se encontró una sesión activa."
                    )
                }
                return@launch
            }

            if (session.role != StudentRole.ADMIN) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        adminName = session.displayName,
                        hasSession = true,
                        hasAdminAccess = false,
                        errorMessage = "No tienes permisos para acceder al panel administrativo."
                    )
                }
                return@launch
            }

            _uiState.update {
                it.copy(
                    isLoading = !isRefresh && it.overview == null,
                    isRefreshing = isRefresh,
                    adminName = session.displayName,
                    hasSession = true,
                    hasAdminAccess = true,
                    errorMessage = null
                )
            }

            analyticsRepository.getOverview(session.studentId).collect { resource ->
                when (resource) {
                    is Resource.Loading -> Unit
                    is Resource.Success -> _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            overview = resource.data,
                            errorMessage = null
                        )
                    }
                    is Resource.Error -> _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            errorMessage = resource.message ?: if (isRefresh) {
                                "No se pudieron actualizar los datos."
                            } else {
                                "No se pudo cargar el resumen administrativo."
                            }
                        )
                    }
                }
            }
        }
    }
}
