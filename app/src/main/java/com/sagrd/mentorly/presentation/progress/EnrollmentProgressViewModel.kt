package com.sagrd.mentorly.presentation.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sagrd.mentorly.data.remote.Resource
import com.sagrd.mentorly.domain.repository.course.CourseRepository
import com.sagrd.mentorly.domain.repository.enrollment.EnrollmentRepository
import com.sagrd.mentorly.domain.repository.progress.EnrollmentProgressRepository
import com.sagrd.mentorly.domain.repository.session.SessionRepository
import com.sagrd.mentorly.domain.repository.submission.SubmissionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class EnrollmentProgressViewModel @Inject constructor(
    private val enrollmentProgressRepository: EnrollmentProgressRepository,
    private val enrollmentRepository: EnrollmentRepository,
    private val courseRepository: CourseRepository,
    private val submissionRepository: SubmissionRepository,
    private val sessionRepository: SessionRepository
) : ViewModel() {

    private val _state = MutableStateFlow(EnrollmentProgressUiState())
    val state: StateFlow<EnrollmentProgressUiState> = _state.asStateFlow()

    private var enrollmentId: String? = null

    fun initialize(id: String) {
        if (enrollmentId != id) {
            enrollmentId = id
            _state.update {
                EnrollmentProgressUiState(
                    isLoading = true,
                    expandedUnitIds = it.expandedUnitIds
                )
            }
        }
        loadProgress(isRefresh = false)
        loadEnrollmentDetails(id)
        loadSubmissions(id)
        loadSession()
    }

    private fun loadSession() {
        viewModelScope.launch {
            sessionRepository.session.firstOrNull()?.let { session ->
                _state.update { it.copy(studentName = session.displayName) }
            }
        }
    }

    fun onEvent(event: EnrollmentProgressUiEvent) {
        when (event) {
            EnrollmentProgressUiEvent.Refresh -> {
                val id = enrollmentId
                if (id != null) {
                    loadProgress(isRefresh = true)
                    loadEnrollmentDetails(id)
                    loadSubmissions(id)
                    loadSession()
                }
            }

            is EnrollmentProgressUiEvent.CompleteTheme -> completeTheme(event.themeId)
            is EnrollmentProgressUiEvent.ToggleUnitExpansion -> {
                _state.update { current ->
                    val expanded = current.expandedUnitIds
                    val updated = if (event.unitId in expanded) {
                        expanded - event.unitId
                    } else {
                        expanded + event.unitId
                    }
                    current.copy(expandedUnitIds = updated)
                }
            }

            EnrollmentProgressUiEvent.ClearError -> _state.update { it.copy(errorMessage = null) }
            EnrollmentProgressUiEvent.ShowCertificateDialog -> {
                val id = enrollmentId
                _state.update { it.copy(isCertificateDialogVisible = true) }
                if (id != null && _state.value.certificate == null) {
                    loadCertificate(id)
                }
            }
            EnrollmentProgressUiEvent.DismissCertificateDialog -> {
                _state.update { it.copy(isCertificateDialogVisible = false) }
            }
        }
    }

    private fun loadCertificate(enrollmentId: String) {
        viewModelScope.launch {
            enrollmentRepository.getCertificate(enrollmentId).collect { resource ->
                if (resource is Resource.Success && resource.data != null) {
                    _state.update { it.copy(certificate = resource.data) }
                }
            }
        }
    }

    private fun completeTheme(themeId: String) {
        val id = enrollmentId ?: return
        if (themeId in _state.value.completingThemeIds) return

        viewModelScope.launch {
            enrollmentProgressRepository.completeTheme(id, themeId).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _state.update {
                        it.copy(
                            completingThemeIds = it.completingThemeIds + themeId,
                            errorMessage = null
                        )
                    }

                    is Resource.Success -> {
                        _state.update {
                            it.copy(
                                progress = resource.data,
                                completingThemeIds = it.completingThemeIds - themeId,
                                errorMessage = null
                            )
                        }
                        loadSubmissions(id)
                    }

                    is Resource.Error -> _state.update {
                        it.copy(
                            completingThemeIds = it.completingThemeIds - themeId,
                            errorMessage = resource.message ?: "No se pudo completar el tema."
                        )
                    }
                }
            }
        }
    }

    private fun loadProgress(isRefresh: Boolean = false) {
        val id = enrollmentId ?: return

        viewModelScope.launch {
            enrollmentProgressRepository.getEnrollmentProgress(id).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _state.update {
                        it.copy(
                            isLoading = !isRefresh && it.progress == null,
                            isRefreshing = isRefresh,
                            errorMessage = null
                        )
                    }

                    is Resource.Success -> {
                        val progressData = resource.data
                        _state.update { current ->
                            val defaultExpanded = if (current.expandedUnitIds.isEmpty() && progressData != null) {
                                val activeUnit = progressData.units.firstOrNull { unit ->
                                    val isUnitCompleted = unit.totalThemes > 0 &&
                                        unit.completedThemes == unit.totalThemes &&
                                        (unit.totalMandatoryActivities == 0 || unit.approvedMandatoryActivities >= unit.totalMandatoryActivities)
                                    !isUnitCompleted
                                } ?: progressData.units.lastOrNull()

                                if (activeUnit != null) setOf(activeUnit.unitId) else emptySet()
                            } else {
                                current.expandedUnitIds
                            }

                            current.copy(
                                isLoading = false,
                                isRefreshing = false,
                                progress = progressData,
                                expandedUnitIds = defaultExpanded,
                                errorMessage = null
                            )
                        }
                    }

                    is Resource.Error -> _state.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            errorMessage = resource.message ?: "No se pudo cargar el progreso."
                        )
                    }
                }
            }
        }
    }

    private fun loadSubmissions(enrollmentId: String) {
        viewModelScope.launch {
            val session = sessionRepository.session.firstOrNull()
            val studentId = session?.studentId
            if (studentId != null) {
                submissionRepository.getSubmissionsByStudentId(studentId).collect { resource ->
                    if (resource is Resource.Success && resource.data != null) {
                        val map = resource.data
                            .filter { it.enrollmentId == enrollmentId }
                            .associateBy { it.activityId }

                        _state.update { it.copy(submissionsByActivityId = map) }
                    }
                }
            }
        }
    }

    private fun loadEnrollmentDetails(id: String) {
        viewModelScope.launch {
            enrollmentRepository.getEnrollmentById(id).collect { resource ->
                if (resource is Resource.Success && resource.data != null) {
                    val enrollment = resource.data
                    _state.update {
                        it.copy(enrollment = enrollment)
                    }
                    loadCourseImage(enrollment.courseId)
                }
            }
        }
    }

    private fun loadCourseImage(courseId: String) {
        viewModelScope.launch {
            courseRepository.getCourseById(courseId).collect { resource ->
                if (resource is Resource.Success && resource.data != null) {
                    _state.update {
                        it.copy(courseImageUrl = resource.data.imageUrl)
                    }
                }
            }
        }
    }
}
