package com.sagrd.mentorly.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sagrd.mentorly.data.remote.Resource
import com.sagrd.mentorly.data.remote.dto.student.UpdateLeaderboardPrivacyDto
import com.sagrd.mentorly.data.remote.dto.student.UpdateStudentDto
import com.sagrd.mentorly.domain.model.session.AppSession
import com.sagrd.mentorly.domain.repository.auth.AuthRepository
import com.sagrd.mentorly.domain.repository.session.SessionRepository
import com.sagrd.mentorly.domain.repository.student.StudentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val studentRepository: StudentRepository,
    private val authRepository: AuthRepository,
    private val sessionRepository: SessionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        onEvent(ProfileUiEvent.Load)
    }

    fun onEvent(event: ProfileUiEvent) {
        when (event) {
            is ProfileUiEvent.Load -> load()
            is ProfileUiEvent.ShowEditDialog -> showEditDialog()
            is ProfileUiEvent.DismissEditDialog -> _uiState.update { it.copy(isEditDialogVisible = false) }
            is ProfileUiEvent.DisplayNameChanged -> _uiState.update { it.copy(editedDisplayName = event.value) }
            is ProfileUiEvent.EmailChanged -> _uiState.update { it.copy(editedEmail = event.value) }
            is ProfileUiEvent.SaveProfile -> saveProfile()
            is ProfileUiEvent.PrivacyChanged -> changePrivacy(event.isPublic)
            is ProfileUiEvent.ShowSignOutDialog -> _uiState.update { it.copy(isSignOutDialogVisible = true) }
            is ProfileUiEvent.DismissSignOutDialog -> _uiState.update { it.copy(isSignOutDialogVisible = false) }
            is ProfileUiEvent.ConfirmSignOut -> signOut()
            is ProfileUiEvent.DismissError -> _uiState.update { it.copy(errorMessage = null) }
        }
    }

    private fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val session = sessionRepository.session.first()
            val studentId = session?.studentId

            if (studentId == null) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Sesión no encontrada") }
                return@launch
            }

            studentRepository.getStudentById(studentId).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _uiState.update { it.copy(isLoading = true) }
                    is Resource.Success -> {
                        val student = resource.data
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                student = student,
                                editedDisplayName = student?.displayName ?: "",
                                editedEmail = student?.email ?: ""
                            )
                        }
                        loadStatistics(studentId)
                    }
                    is Resource.Error -> _uiState.update {
                        it.copy(isLoading = false, errorMessage = resource.message)
                    }
                }
            }
        }
    }

    private fun loadStatistics(studentId: String) {
        viewModelScope.launch {
            studentRepository.getStudentStatistics(studentId).collect { resource ->
                if (resource is Resource.Success) {
                    _uiState.update { it.copy(statistics = resource.data) }
                }
            }
        }
    }

    private fun showEditDialog() {
        val student = _uiState.value.student
        _uiState.update {
            it.copy(
                isEditDialogVisible = true,
                editedDisplayName = student?.displayName ?: "",
                editedEmail = student?.email ?: ""
            )
        }
    }

    private fun saveProfile() {
        val state = _uiState.value
        val studentId = state.student?.id ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }

            val dto = UpdateStudentDto(
                displayName = state.editedDisplayName,
                email = state.editedEmail
            )

            studentRepository.updateStudent(studentId, dto).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                isSaving = false,
                                isEditDialogVisible = false,
                                student = it.student?.copy(
                                    displayName = state.editedDisplayName,
                                    email = state.editedEmail
                                )
                            )
                        }
                        updateSessionCache()
                    }
                    is Resource.Error -> _uiState.update {
                        it.copy(isSaving = false, errorMessage = resource.message)
                    }
                    is Resource.Loading -> Unit
                }
            }
        }
    }

    private fun changePrivacy(isPublic: Boolean) {
        val studentId = _uiState.value.student?.id ?: return

        viewModelScope.launch {
            val dto = UpdateLeaderboardPrivacyDto(isLeaderboardPublic = isPublic)

            studentRepository.updateLeaderboardPrivacy(studentId, dto).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(student = it.student?.copy(isLeaderboardPublic = isPublic))
                        }
                        updateSessionCache()
                    }
                    is Resource.Error -> _uiState.update { it.copy(errorMessage = resource.message) }
                    is Resource.Loading -> Unit
                }
            }
        }
    }

    private suspend fun updateSessionCache() {
        val student = _uiState.value.student ?: return
        val currentSession = sessionRepository.session.first() ?: return

        sessionRepository.saveSession(
            currentSession.copy(
                displayName = student.displayName,
                email = student.email,
                isLeaderboardPublic = student.isLeaderboardPublic
            )
        )
    }

    private fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
            sessionRepository.clearSession()
            _uiState.update { it.copy(isSignOutDialogVisible = false, isSignedOut = true) }
        }
    }
}