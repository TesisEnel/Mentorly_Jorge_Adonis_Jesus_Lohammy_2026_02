package com.sagrd.mentorly.presentation.course.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sagrd.mentorly.data.remote.Resource
import com.sagrd.mentorly.data.remote.dto.enrollment.CreateEnrollmentDto
import com.sagrd.mentorly.domain.model.enrollment.EnrollmentStatus
import com.sagrd.mentorly.domain.repository.course.CourseRepository
import com.sagrd.mentorly.domain.repository.enrollment.EnrollmentRepository
import com.sagrd.mentorly.domain.repository.progress.EnrollmentProgressRepository
import com.sagrd.mentorly.domain.repository.session.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class CourseDetailViewModel @Inject constructor(
    private val courseRepository: CourseRepository,
    private val enrollmentRepository: EnrollmentRepository,
    private val enrollmentProgressRepository: EnrollmentProgressRepository,
    private val sessionRepository: SessionRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CourseDetailUiState())
    val state: StateFlow<CourseDetailUiState> = _state.asStateFlow()

    private var courseId: String? = null
    private var loadJob: Job? = null
    private var activeEnrollmentJob: Job? = null
    private var progressJob: Job? = null

    fun onEvent(event: CourseDetailUiEvent) {
        when (event) {
            is CourseDetailUiEvent.LoadCourseContent -> {
                courseId = event.courseId
                loadCourseContent(event.courseId)
                checkActiveEnrollment(event.courseId)
            }

            CourseDetailUiEvent.Retry -> {
                val currentId = courseId
                if (currentId != null) {
                    loadCourseContent(currentId)
                    checkActiveEnrollment(currentId)
                }
            }

            CourseDetailUiEvent.Enroll -> enroll()
            CourseDetailUiEvent.ClearEnrollmentError -> _state.update {
                it.copy(enrollmentErrorMessage = null)
            }
        }
    }

    private fun loadCourseContent(courseId: String) {
        loadJob?.cancel()

        _state.update {
            it.copy(
                activeEnrollmentId = null,
                createdEnrollmentId = null,
                progressPercentage = 0
            )
        }

        loadJob = viewModelScope.launch {
            courseRepository.getCourseContent(courseId).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _state.update {
                            it.copy(
                                isLoading = true,
                                errorMessage = null
                            )
                        }
                    }

                    is Resource.Success -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                course = resource.data,
                                errorMessage = null
                            )
                        }
                    }

                    is Resource.Error -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = resource.message
                                    ?: "No se pudo cargar el contenido del curso."
                            )
                        }
                    }
                }
            }
        }
    }

    private fun enroll() {
        val currentCourseId = courseId ?: return
        if (_state.value.isEnrolling) return

        viewModelScope.launch {
            val session = sessionRepository.session.first()
            if (session == null) {
                _state.update {
                    it.copy(enrollmentErrorMessage = "No se encontró una sesión activa.")
                }
            } else {
                enrollmentRepository.createEnrollment(
                    studentId = session.studentId,
                    enrollment = CreateEnrollmentDto(courseId = currentCourseId)
                ).collect { resource ->
                    when (resource) {
                        is Resource.Loading -> _state.update {
                            it.copy(isEnrolling = true, enrollmentErrorMessage = null)
                        }

                        is Resource.Success -> {
                            val newEnrollmentId = resource.data?.enrollmentId
                            _state.update {
                                it.copy(
                                    isEnrolling = false,
                                    createdEnrollmentId = newEnrollmentId,
                                    activeEnrollmentId = newEnrollmentId,
                                    enrollmentErrorMessage = null
                                )
                            }
                            if (newEnrollmentId != null) {
                                loadProgress(newEnrollmentId)
                            }
                        }

                        is Resource.Error -> _state.update {
                            it.copy(
                                isEnrolling = false,
                                enrollmentErrorMessage = resource.message
                                    ?: "No se pudo crear la inscripción."
                            )
                        }
                    }
                }
            }
        }
    }

    private fun checkActiveEnrollment(courseId: String) {
        activeEnrollmentJob?.cancel()

        activeEnrollmentJob = viewModelScope.launch {
            val session = sessionRepository.session.first()
            if (session == null) {
                _state.update {
                    it.copy(
                        isCheckingActiveEnrollment = false,
                        activeEnrollmentId = null,
                        progressPercentage = 0
                    )
                }
            } else {
                enrollmentRepository.getEnrollments(session.studentId).collect { resource ->
                    when (resource) {
                        is Resource.Loading -> _state.update {
                            it.copy(
                                isCheckingActiveEnrollment = true,
                                activeEnrollmentId = null
                            )
                        }

                        is Resource.Success -> {
                            val activeEnrollment = resource.data
                                .orEmpty()
                                .firstOrNull { enrollment ->
                                    enrollment.courseId == courseId &&
                                        enrollment.status == EnrollmentStatus.ACTIVE
                                }

                            _state.update {
                                it.copy(
                                    isCheckingActiveEnrollment = false,
                                    activeEnrollmentId = activeEnrollment?.id
                                )
                            }

                            if (activeEnrollment != null) {
                                loadProgress(activeEnrollment.id)
                            }
                        }

                        is Resource.Error -> _state.update {
                            it.copy(isCheckingActiveEnrollment = false)
                        }
                    }
                }
            }
        }
    }

    private fun loadProgress(enrollmentId: String) {
        progressJob?.cancel()
        progressJob = viewModelScope.launch {
            enrollmentProgressRepository.getEnrollmentProgress(enrollmentId).collect { resource ->
                if (resource is Resource.Success && resource.data != null) {
                    _state.update {
                        it.copy(progressPercentage = resource.data.percentage)
                    }
                }
            }
        }
    }
}
