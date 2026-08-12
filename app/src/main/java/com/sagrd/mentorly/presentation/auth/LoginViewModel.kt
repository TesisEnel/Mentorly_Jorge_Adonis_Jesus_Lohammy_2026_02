package com.sagrd.mentorly.presentation.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sagrd.mentorly.data.remote.Resource
import com.sagrd.mentorly.data.remote.dto.student.ProvisionStudentDto
import com.sagrd.mentorly.domain.model.auth.AuthUser
import com.sagrd.mentorly.domain.model.session.AppSession
import com.sagrd.mentorly.domain.model.student.Student
import com.sagrd.mentorly.domain.repository.auth.AuthRepository
import com.sagrd.mentorly.domain.repository.session.SessionRepository
import com.sagrd.mentorly.domain.repository.student.StudentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val studentRepository: StudentRepository,
    private val sessionRepository: SessionRepository
) : ViewModel() {

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    fun onEvent(event: LoginUiEvent) {
        when (event) {
            is LoginUiEvent.SignInWithGoogle -> signInWithGoogle(event.context)
            LoginUiEvent.SignOut -> signOut()
        }
    }

    private fun signInWithGoogle(context: Context) {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null
                )
            }

            authRepository.signInWithGoogle(context).fold(
                onSuccess = { authUser ->
                    provisionStudent(authUser)
                },
                onFailure = { exception ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = exception.message
                                ?: "No se pudo iniciar sesión con Google."
                        )
                    }
                }
            )
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
                is Resource.Loading -> {
                    _state.update {
                        it.copy(isLoading = true)
                    }
                }

                is Resource.Success -> {
                    val student = resource.data ?: return@collect
                    sessionRepository.saveSession(student.toSession(authUser.uid))

                    _state.update {
                        it.copy(
                            isLoading = false,
                            student = student,
                            errorMessage = null
                        )
                    }
                }

                is Resource.Error -> {
                    authRepository.signOut()
                    sessionRepository.clearSession()

                    _state.update {
                        it.copy(
                            isLoading = false,
                            student = null,
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

            _state.value = LoginUiState()
        }
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
