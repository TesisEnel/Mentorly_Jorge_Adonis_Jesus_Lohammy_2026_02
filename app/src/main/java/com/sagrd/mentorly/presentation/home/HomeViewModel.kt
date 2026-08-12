package com.sagrd.mentorly.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sagrd.mentorly.data.remote.Resource
import com.sagrd.mentorly.domain.model.enrollment.EnrollmentStatus
import com.sagrd.mentorly.domain.repository.course.CourseRepository
import com.sagrd.mentorly.domain.repository.enrollment.EnrollmentRepository
import com.sagrd.mentorly.domain.repository.session.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val courseRepository: CourseRepository,
    private val enrollmentRepository: EnrollmentRepository,
    private val sessionRepository: SessionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        onEvent(HomeUiEvent.Load)
    }

    fun onEvent(event: HomeUiEvent) {
        when (event) {
            HomeUiEvent.Load -> loadHome()
            HomeUiEvent.Refresh -> loadHome(isRefresh = true)
            HomeUiEvent.ClearError -> _uiState.update { it.copy(errorMessage = null) }
        }
    }

    private fun loadHome(isRefresh: Boolean = false) {
        if (_uiState.value.isLoading || _uiState.value.isRefreshing) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = !isRefresh,
                    isRefreshing = isRefresh,
                    errorMessage = null
                )
            }

            val session = sessionRepository.session.first()
            if (session == null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        studentName = "",
                        activeEnrollments = emptyList(),
                        publishedCourses = emptyList(),
                        errorMessage = "No se encontró una sesión activa.",
                        hasSession = false
                    )
                }
                return@launch
            }

            _uiState.update {
                it.copy(
                    studentName = session.displayName,
                    hasSession = true
                )
            }

            var enrollmentFailed = false
            var coursesFailed = false

            try {
                enrollmentRepository.getEnrollments(session.studentId).collect { resource ->
                    when (resource) {
                        is Resource.Loading -> Unit
                        is Resource.Success -> _uiState.update {
                            it.copy(
                                activeEnrollments = resource.data.orEmpty().filter { enrollment ->
                                    enrollment.status == EnrollmentStatus.ACTIVE
                                }
                            )
                        }
                        is Resource.Error -> enrollmentFailed = true
                    }
                }
            } catch (_: Exception) {
                enrollmentFailed = true
            }

            try {
                courseRepository.getCourses().collect { resource ->
                    when (resource) {
                        is Resource.Loading -> Unit
                        is Resource.Success -> _uiState.update {
                            it.copy(
                                publishedCourses = resource.data.orEmpty().filter { course ->
                                    course.isPublished
                                }
                            )
                        }
                        is Resource.Error -> coursesFailed = true
                    }
                }
            } catch (_: Exception) {
                coursesFailed = true
            }

            _uiState.update {
                it.copy(
                    isLoading = false,
                    isRefreshing = false,
                    errorMessage = when {
                        !enrollmentFailed && !coursesFailed -> null
                        isRefresh -> "No se pudieron actualizar los datos."
                        else -> "No se pudo cargar el inicio. Inténtalo nuevamente."
                    }
                )
            }
        }
    }
}
