package com.sagrd.mentorly.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sagrd.mentorly.data.remote.Resource
import com.sagrd.mentorly.data.remote.dto.student.UpdateLeaderboardPrivacyDto
import com.sagrd.mentorly.data.remote.dto.student.UpdateStudentDto
import com.sagrd.mentorly.domain.model.enrollment.EnrollmentStatus
import com.sagrd.mentorly.domain.repository.auth.AuthRepository
import com.sagrd.mentorly.domain.repository.enrollment.EnrollmentRepository
import com.sagrd.mentorly.domain.repository.peerreview.PeerReviewRepository
import com.sagrd.mentorly.domain.repository.session.SessionRepository
import com.sagrd.mentorly.domain.repository.student.StudentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val studentRepository: StudentRepository,
    private val authRepository: AuthRepository,
    private val sessionRepository: SessionRepository,
    private val peerReviewRepository: PeerReviewRepository,
    private val enrollmentRepository: EnrollmentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        onEvent(ProfileUiEvent.Load)
    }

    fun onEvent(event: ProfileUiEvent) {
        when (event) {
            ProfileUiEvent.Load -> load()
            ProfileUiEvent.ShowEditDialog -> showEditDialog()
            ProfileUiEvent.DismissEditDialog -> _uiState.update { it.copy(isEditDialogVisible = false) }
            is ProfileUiEvent.DisplayNameChanged -> _uiState.update { it.copy(editedDisplayName = event.value) }
            is ProfileUiEvent.EmailChanged -> _uiState.update { it.copy(editedEmail = event.value) }
            ProfileUiEvent.SaveProfile -> saveProfile()
            is ProfileUiEvent.PrivacyChanged -> changePrivacy(event.isPublic)
            ProfileUiEvent.ShowSignOutDialog -> _uiState.update { it.copy(isSignOutDialogVisible = true) }
            ProfileUiEvent.DismissSignOutDialog -> _uiState.update { it.copy(isSignOutDialogVisible = false) }
            ProfileUiEvent.ConfirmSignOut -> signOut()
            ProfileUiEvent.SignOutHandled -> _uiState.update { it.copy(isSignedOut = false) }
            ProfileUiEvent.DismissError -> _uiState.update { it.copy(errorMessage = null) }
        }
    }

    private fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val session = sessionRepository.session.first()
            if (session == null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "No se encontró una sesión activa."
                    )
                }
            } else {
                val studentId = session.studentId
                val authUser = authRepository.getCurrentUser()

                _uiState.update {
                    it.copy(userPhotoUrl = authUser?.photoUrl)
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
                            loadPeerReviewsCount(studentId)
                            loadCertificatesCount(studentId)
                        }
                        is Resource.Error -> _uiState.update {
                            it.copy(isLoading = false, errorMessage = resource.message)
                        }
                    }
                }
            }
        }
    }

    private fun loadStatistics(studentId: String) {
        viewModelScope.launch {
            studentRepository.getStudentStatistics(studentId).collect { resource ->
                if (resource is Resource.Success && resource.data != null) {
                    _uiState.update { it.copy(statistics = resource.data) }
                }
            }
        }
    }

    private fun loadPeerReviewsCount(studentId: String) {
        viewModelScope.launch {
            peerReviewRepository.getMyReviews(studentId).collect { resource ->
                if (resource is Resource.Success && resource.data != null) {
                    _uiState.update { it.copy(peerReviewsCount = resource.data.size) }
                }
            }
        }
    }

    private fun loadCertificatesCount(studentId: String) {
        viewModelScope.launch {
            enrollmentRepository.getEnrollments(studentId).collect { resource ->
                if (resource is Resource.Success && resource.data != null) {
                    val completed = resource.data.count { it.status == EnrollmentStatus.COMPLETED }
                    _uiState.update { it.copy(certificatesCount = completed) }
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
        val student = state.student
        if (student == null) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }

            val dto = UpdateStudentDto(
                displayName = state.editedDisplayName,
                email = state.editedEmail
            )

            studentRepository.updateStudent(student.id, dto).collect { resource ->
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
        val student = _uiState.value.student
        if (student == null) return

        viewModelScope.launch {
            val dto = UpdateLeaderboardPrivacyDto(isLeaderboardPublic = isPublic)

            studentRepository.updateLeaderboardPrivacy(student.id, dto).collect { resource ->
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
        val student = _uiState.value.student
        if (student != null) {
            val currentSession = sessionRepository.session.first()
            if (currentSession != null) {
                sessionRepository.saveSession(
                    currentSession.copy(
                        displayName = student.displayName,
                        email = student.email,
                        isLeaderboardPublic = student.isLeaderboardPublic
                    )
                )
            }
        }
    }

    private fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
            sessionRepository.clearSession()
            _uiState.update { it.copy(isSignOutDialogVisible = false, isSignedOut = true) }
        }
    }
}
