package com.sagrd.mentorly.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sagrd.mentorly.data.remote.Resource
import com.sagrd.mentorly.domain.model.enrollment.EnrollmentStatus
import com.sagrd.mentorly.domain.repository.auth.AuthRepository
import com.sagrd.mentorly.domain.repository.course.CourseRepository
import com.sagrd.mentorly.domain.repository.enrollment.EnrollmentRepository
import com.sagrd.mentorly.domain.repository.progress.EnrollmentProgressRepository
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
    private val enrollmentProgressRepository: EnrollmentProgressRepository,
    private val authRepository: AuthRepository,
    private val sessionRepository: SessionRepository
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    init {
        onEvent(HomeUiEvent.Load)
    }

    fun onEvent(event: HomeUiEvent) {
        when (event) {
            HomeUiEvent.Load -> loadHome()
            HomeUiEvent.Refresh -> loadHome(isRefresh = true)
            HomeUiEvent.ClearError -> _state.update { it.copy(errorMessage = null) }
        }
    }

    private fun loadHome(isRefresh: Boolean = false) {
        if (_state.value.isLoading || _state.value.isRefreshing) return

        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = !isRefresh,
                    isRefreshing = isRefresh,
                    errorMessage = null
                )
            }

            val session = sessionRepository.session.first()
            if (session == null) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        studentName = "",
                        userPhotoUrl = null,
                        activeEnrollments = emptyList(),
                        enrollmentProgressMap = emptyMap(),
                        publishedCourses = emptyList(),
                        errorMessage = "No se encontró una sesión activa.",
                        hasSession = false
                    )
                }
            } else {
                val authUser = authRepository.getCurrentUser()
                _state.update {
                    it.copy(
                        studentName = session.displayName,
                        userPhotoUrl = authUser?.photoUrl,
                        hasSession = true
                    )
                }

                var hasError = false

                enrollmentRepository.getEnrollments(session.studentId).collect { resource ->
                    when (resource) {
                        is Resource.Loading -> Unit
                        is Resource.Success -> {
                            val activeList = resource.data.orEmpty().filter { enrollment ->
                                enrollment.status == EnrollmentStatus.ACTIVE
                            }
                            _state.update {
                                it.copy(activeEnrollments = activeList)
                            }
                            activeList.forEach { enrollment ->
                                loadProgressForEnrollment(enrollment.id)
                            }
                        }
                        is Resource.Error -> hasError = true
                    }
                }

                courseRepository.getCourses().collect { resource ->
                    when (resource) {
                        is Resource.Loading -> Unit
                        is Resource.Success -> _state.update {
                            it.copy(
                                publishedCourses = resource.data.orEmpty().filter { course ->
                                    course.isPublished
                                }
                            )
                        }
                        is Resource.Error -> hasError = true
                    }
                }

                _state.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        errorMessage = when {
                            !hasError -> null
                            isRefresh -> "No se pudieron actualizar los datos."
                            else -> "No se pudo cargar el inicio. Inténtalo nuevamente."
                        }
                    )
                }
            }
        }
    }

    private fun loadProgressForEnrollment(enrollmentId: String) {
        viewModelScope.launch {
            enrollmentProgressRepository.getEnrollmentProgress(enrollmentId).collect { resource ->
                if (resource is Resource.Success && resource.data != null) {
                    _state.update { current ->
                        val updatedMap = current.enrollmentProgressMap.toMutableMap()
                        updatedMap[enrollmentId] = resource.data.percentage
                        current.copy(enrollmentProgressMap = updatedMap)
                    }
                }
            }
        }
    }
}
