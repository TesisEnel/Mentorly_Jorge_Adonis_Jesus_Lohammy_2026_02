package com.sagrd.mentorly.presentation.startup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sagrd.mentorly.data.remote.Resource
import com.sagrd.mentorly.data.remote.dto.student.ProvisionStudentDto
import com.sagrd.mentorly.domain.model.auth.AuthUser
import com.sagrd.mentorly.domain.model.session.AppSession
import com.sagrd.mentorly.domain.model.student.Student
import com.sagrd.mentorly.domain.model.student.StudentRole
import com.sagrd.mentorly.domain.repository.auth.AuthRepository
import com.sagrd.mentorly.domain.repository.session.SessionRepository
import com.sagrd.mentorly.domain.repository.student.StudentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StartupViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val studentRepository: StudentRepository,
    private val sessionRepository: SessionRepository
) : ViewModel() {

    private val _state = MutableStateFlow(StartupUiState())
    val state: StateFlow<StartupUiState> = _state.asStateFlow()

    init {
        restoreSession()
    }

    fun onEvent(event: StartupUiEvent) {
        when (event) {
            StartupUiEvent.Retry -> restoreSession()
            StartupUiEvent.SignOut -> signOut()
        }
    }

    private fun restoreSession() {
        val authUser = authRepository.getCurrentUser()

        if (authUser == null) {
            viewModelScope.launch {
                sessionRepository.clearSession()
                navigateTo(StartupDestination.LOGIN)
            }
            return
        }

        viewModelScope.launch {
            provisionStudent(authUser)
        }
    }

    private suspend fun provisionStudent(authUser: AuthUser) {
        val email = authUser.email
        val displayName = authUser.displayName

        if (email.isNullOrBlank() || displayName.isNullOrBlank()) {
            authRepository.signOut()
            sessionRepository.clearSession()

            _state.update {
                it.copy(
                    isLoading = false,
                    errorMessage = "Tu cuenta de Google debe tener correo y nombre visibles."
                )
            }
            return
        }

        _state.update {
            it.copy(
                isLoading = true,
                destination = null,
                errorMessage = null
            )
        }

        studentRepository.provisionStudent(
            ProvisionStudentDto(
                googleUserId = authUser.uid,
                email = email,
                displayName = displayName
            )
        ).collect { resource ->
            when (resource) {
                is Resource.Loading -> Unit

                is Resource.Success -> {
                    val student = resource.data ?: return@collect

                    sessionRepository.saveSession(student.toSession(authUser.uid))
                    navigateTo(
                        if (student.role == StudentRole.ADMIN) {
                            StartupDestination.ADMIN_DASHBOARD
                        } else {
                            StartupDestination.COURSE_LIST
                        }
                    )
                }

                is Resource.Error -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = resource.message
                                ?: "No se pudo sincronizar tu perfil."
                        )
                    }
                }
            }
        }
    }

    private fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
            sessionRepository.clearSession()
            navigateTo(StartupDestination.LOGIN)
        }
    }

    private fun navigateTo(destination: StartupDestination) {
        _state.value = StartupUiState(
            isLoading = false,
            destination = destination
        )
    }

    private fun Student.toSession(firebaseUserId: String): AppSession {
        return AppSession(
            studentId = id,
            firebaseUserId = firebaseUserId,
            displayName = displayName,
            email = email,
            role = role,
            isLeaderboardPublic = isLeaderboardPublic
        )
    }
}
