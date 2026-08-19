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
import com.sagrd.mentorly.domain.repository.progress.EnrollmentProgressRepository
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
    private val enrollmentRepository: EnrollmentRepository,
    private val enrollmentProgressRepository: EnrollmentProgressRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileUiState())
    val state: StateFlow<ProfileUiState> = _state.asStateFlow()

    init {
        onEvent(ProfileUiEvent.Load)
    }

    fun onEvent(event: ProfileUiEvent) {
        when (event) {
            ProfileUiEvent.Load -> load()
            ProfileUiEvent.ShowEditDialog -> showEditDialog()
            ProfileUiEvent.DismissEditDialog -> _state.update { it.copy(isEditDialogVisible = false) }
            is ProfileUiEvent.DisplayNameChanged -> _state.update { it.copy(editedDisplayName = event.value) }
            is ProfileUiEvent.EmailChanged -> _state.update { it.copy(editedEmail = event.value) }
            ProfileUiEvent.SaveProfile -> saveProfile()
            is ProfileUiEvent.PrivacyChanged -> changePrivacy(event.isPublic)
            ProfileUiEvent.ShowCertificatesListDialog -> _state.update {
                it.copy(isCertificatesListDialogVisible = true)
            }
            ProfileUiEvent.DismissCertificatesListDialog -> _state.update {
                it.copy(isCertificatesListDialogVisible = false)
            }
            is ProfileUiEvent.SelectCertificateEnrollment -> selectCertificateEnrollment(event.enrollment)
            ProfileUiEvent.DismissCertificateDialog -> _state.update {
                it.copy(selectedCertificateEnrollment = null, selectedCertificate = null)
            }
            ProfileUiEvent.ShowSignOutDialog -> _state.update { it.copy(isSignOutDialogVisible = true) }
            ProfileUiEvent.DismissSignOutDialog -> _state.update { it.copy(isSignOutDialogVisible = false) }
            ProfileUiEvent.ConfirmSignOut -> signOut()
            ProfileUiEvent.SignOutHandled -> _state.update { it.copy(isSignedOut = false) }
            ProfileUiEvent.DismissError -> _state.update { it.copy(errorMessage = null) }
        }
    }

    private fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            val session = sessionRepository.session.first()
            if (session == null) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "No se encontró una sesión activa."
                    )
                }
            } else {
                val studentId = session.studentId
                val authUser = authRepository.getCurrentUser()

                _state.update {
                    it.copy(userPhotoUrl = authUser?.photoUrl)
                }

                studentRepository.getStudentById(studentId).collect { resource ->
                    when (resource) {
                        is Resource.Loading -> _state.update { it.copy(isLoading = true) }
                        is Resource.Success -> {
                            val student = resource.data
                            _state.update {
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
                        is Resource.Error -> _state.update {
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
                    _state.update { it.copy(statistics = resource.data) }
                }
            }
        }
    }

    private fun loadPeerReviewsCount(studentId: String) {
        viewModelScope.launch {
            peerReviewRepository.getMyReviews(studentId).collect { resource ->
                if (resource is Resource.Success && resource.data != null) {
                    _state.update { it.copy(peerReviewsCount = resource.data.size) }
                }
            }
        }
    }

    private fun loadCertificatesCount(studentId: String) {
        viewModelScope.launch {
            enrollmentRepository.getEnrollments(studentId).collect { resource ->
                if (resource is Resource.Success && resource.data != null) {
                    val enrollments = resource.data
                    val explicitlyCompleted = enrollments.filter {
                        it.status == EnrollmentStatus.COMPLETED || it.completedAt != null
                    }
                    _state.update {
                        it.copy(
                            certificatesCount = explicitlyCompleted.size,
                            completedEnrollments = explicitlyCompleted
                        )
                    }

                    // Check progress for any other enrollments in case status in DB hasn't flipped
                    val pendingStatusEnrollments = enrollments.filter {
                        it.status != EnrollmentStatus.COMPLETED && it.completedAt == null
                    }

                    pendingStatusEnrollments.forEach { enrollment ->
                        launch {
                            enrollmentProgressRepository.getEnrollmentProgress(enrollment.id).collect { progressResource ->
                                if (progressResource is Resource.Success && progressResource.data?.percentage == 100) {
                                    _state.update { current ->
                                        if (current.completedEnrollments.none { it.id == enrollment.id }) {
                                            val updated = current.completedEnrollments + enrollment
                                            current.copy(
                                                certificatesCount = updated.size,
                                                completedEnrollments = updated
                                            )
                                        } else {
                                            current
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun selectCertificateEnrollment(enrollment: com.sagrd.mentorly.domain.model.enrollment.Enrollment) {
        _state.update {
            it.copy(
                selectedCertificateEnrollment = enrollment,
                selectedCertificate = null
            )
        }
        viewModelScope.launch {
            enrollmentRepository.getCertificate(enrollment.id).collect { resource ->
                if (resource is Resource.Success && resource.data != null) {
                    _state.update { it.copy(selectedCertificate = resource.data) }
                }
            }
        }
    }

    private fun showEditDialog() {
        val student = _state.value.student
        _state.update {
            it.copy(
                isEditDialogVisible = true,
                editedDisplayName = student?.displayName ?: "",
                editedEmail = student?.email ?: ""
            )
        }
    }

    private fun saveProfile() {
        val state = _state.value
        val student = state.student
        if (student == null) return

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }

            val dto = UpdateStudentDto(
                displayName = state.editedDisplayName,
                email = state.editedEmail
            )

            studentRepository.updateStudent(student.id, dto).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        _state.update {
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
                    is Resource.Error -> _state.update {
                        it.copy(isSaving = false, errorMessage = resource.message)
                    }
                    is Resource.Loading -> Unit
                }
            }
        }
    }

    private fun changePrivacy(isPublic: Boolean) {
        val student = _state.value.student
        if (student == null) return

        viewModelScope.launch {
            val dto = UpdateLeaderboardPrivacyDto(isLeaderboardPublic = isPublic)

            studentRepository.updateLeaderboardPrivacy(student.id, dto).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        _state.update {
                            it.copy(student = it.student?.copy(isLeaderboardPublic = isPublic))
                        }
                        updateSessionCache()
                    }
                    is Resource.Error -> _state.update { it.copy(errorMessage = resource.message) }
                    is Resource.Loading -> Unit
                }
            }
        }
    }

    private suspend fun updateSessionCache() {
        val student = _state.value.student
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
            _state.update { it.copy(isSignOutDialogVisible = false, isSignedOut = true) }
        }
    }
}
