package com.sagrd.mentorly.presentation.admin.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sagrd.mentorly.data.remote.Resource
import com.sagrd.mentorly.domain.model.student.StudentRole
import com.sagrd.mentorly.domain.repository.analytics.AnalyticsRepository
import com.sagrd.mentorly.domain.repository.auth.AuthRepository
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
class AdminDashboardViewModel @Inject constructor(
    private val analyticsRepository: AnalyticsRepository,
    private val authRepository: AuthRepository,
    private val sessionRepository: SessionRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AdminDashboardUiState())
    val state: StateFlow<AdminDashboardUiState> = _state.asStateFlow()

    init {
        onEvent(AdminDashboardUiEvent.Load)
    }

    fun onEvent(event: AdminDashboardUiEvent) {
        when (event) {
            AdminDashboardUiEvent.Load -> loadOverview(isRefresh = false)
            AdminDashboardUiEvent.Refresh -> loadOverview(isRefresh = true)
            AdminDashboardUiEvent.ClearError -> _state.update { it.copy(errorMessage = null) }
            AdminDashboardUiEvent.ShowSignOutDialog -> _state.update {
                it.copy(isSignOutDialogVisible = true)
            }
            AdminDashboardUiEvent.DismissSignOutDialog -> _state.update {
                it.copy(isSignOutDialogVisible = false)
            }
            AdminDashboardUiEvent.ConfirmSignOut -> signOut()
            AdminDashboardUiEvent.SignOutHandled -> _state.update { it.copy(isSignedOut = false) }
        }
    }

    private fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
            sessionRepository.clearSession()
            _state.update {
                it.copy(isSignOutDialogVisible = false, isSignedOut = true)
            }
        }
    }

    private fun loadOverview(isRefresh: Boolean) {
        viewModelScope.launch {
            val session = sessionRepository.session.first()

            if (session == null) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        hasSession = false,
                        hasAdminAccess = false,
                        errorMessage = "No se encontró una sesión activa."
                    )
                }
            } else if (session.role != StudentRole.ADMIN) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        adminName = session.displayName,
                        hasSession = true,
                        hasAdminAccess = false,
                        errorMessage = "No tienes permisos para acceder al panel administrativo."
                    )
                }
            } else {
                _state.update {
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
                        is Resource.Success -> _state.update {
                            it.copy(
                                isLoading = false,
                                isRefreshing = false,
                                overview = resource.data,
                                errorMessage = null
                            )
                        }
                        is Resource.Error -> _state.update {
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
}
