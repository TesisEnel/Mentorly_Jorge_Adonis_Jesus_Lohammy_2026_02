package com.sagrd.mentorly.presentation.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sagrd.mentorly.data.remote.Resource
import com.sagrd.mentorly.domain.repository.course.CourseRepository
import com.sagrd.mentorly.domain.repository.enrollment.EnrollmentRepository
import com.sagrd.mentorly.domain.repository.progress.EnrollmentProgressRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class EnrollmentProgressViewModel @Inject constructor(
    private val enrollmentProgressRepository: EnrollmentProgressRepository,
    private val enrollmentRepository: EnrollmentRepository,
    private val courseRepository: CourseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EnrollmentProgressUiState())
    val uiState: StateFlow<EnrollmentProgressUiState> = _uiState.asStateFlow()

    private var enrollmentId: String? = null

    fun initialize(id: String) {
        if (enrollmentId == id && (_uiState.value.progress != null || _uiState.value.isLoading)) return

        enrollmentId = id
        loadProgress()
        loadEnrollmentDetails(id)
    }

    fun onEvent(event: EnrollmentProgressUiEvent) {
        when (event) {
            EnrollmentProgressUiEvent.Refresh -> {
                val id = enrollmentId
                if (id != null) {
                    loadProgress(isRefresh = true)
                    loadEnrollmentDetails(id)
                }
            }

            is EnrollmentProgressUiEvent.CompleteTheme -> completeTheme(event.themeId)
            is EnrollmentProgressUiEvent.ToggleUnitExpansion -> {
                _uiState.update { current ->
                    val expanded = current.expandedUnitIds
                    val updated = if (event.unitId in expanded) {
                        expanded - event.unitId
                    } else {
                        expanded + event.unitId
                    }
                    current.copy(expandedUnitIds = updated)
                }
            }

            EnrollmentProgressUiEvent.ClearError -> _uiState.update { it.copy(errorMessage = null) }
        }
    }

    private fun completeTheme(themeId: String) {
        val id = enrollmentId ?: return
        if (themeId in _uiState.value.completingThemeIds) return

        viewModelScope.launch {
            enrollmentProgressRepository.completeTheme(id, themeId).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _uiState.update {
                        it.copy(
                            completingThemeIds = it.completingThemeIds + themeId,
                            errorMessage = null
                        )
                    }

                    is Resource.Success -> _uiState.update {
                        it.copy(
                            progress = resource.data,
                            completingThemeIds = it.completingThemeIds - themeId,
                            errorMessage = null
                        )
                    }

                    is Resource.Error -> _uiState.update {
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
        if (_uiState.value.isLoading || _uiState.value.isRefreshing) return

        viewModelScope.launch {
            enrollmentProgressRepository.getEnrollmentProgress(id).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _uiState.update {
                        it.copy(
                            isLoading = !isRefresh,
                            isRefreshing = isRefresh,
                            errorMessage = null
                        )
                    }

                    is Resource.Success -> {
                        val progressData = resource.data
                        _uiState.update { current ->
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

                    is Resource.Error -> _uiState.update {
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

    private fun loadEnrollmentDetails(id: String) {
        viewModelScope.launch {
            enrollmentRepository.getEnrollmentById(id).collect { resource ->
                if (resource is Resource.Success && resource.data != null) {
                    val enrollment = resource.data
                    val days = calculateDaysRemaining(enrollment.expiresAt)
                    _uiState.update {
                        it.copy(
                            enrollment = enrollment,
                            daysRemaining = days
                        )
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
                    _uiState.update {
                        it.copy(courseImageUrl = resource.data.imageUrl)
                    }
                }
            }
        }
    }

    private fun calculateDaysRemaining(expiresAt: String?): Long? {
        if (expiresAt.isNullOrBlank()) return null
        val parsedDate = if ('T' in expiresAt) {
            runCatching { OffsetDateTime.parse(expiresAt).toLocalDate() }.getOrNull()
        } else {
            runCatching { LocalDate.parse(expiresAt) }.getOrNull()
        }
        val today = LocalDate.now()
        return if (parsedDate != null) {
            ChronoUnit.DAYS.between(today, parsedDate)
        } else {
            null
        }
    }
}
