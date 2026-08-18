package com.sagrd.mentorly.presentation.course.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sagrd.mentorly.data.remote.Resource
import com.sagrd.mentorly.domain.model.course.Course
import com.sagrd.mentorly.domain.model.enrollment.Enrollment
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
class CourseListViewModel @Inject constructor(
    private val courseRepository: CourseRepository,
    private val enrollmentRepository: EnrollmentRepository,
    private val enrollmentProgressRepository: EnrollmentProgressRepository,
    private val authRepository: AuthRepository,
    private val sessionRepository: SessionRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CourseListUiState())
    val state: StateFlow<CourseListUiState> = _state.asStateFlow()

    init {
        loadData()
    }

    fun onEvent(event: CourseListUiEvent) {
        when (event) {
            CourseListUiEvent.Refresh -> loadData(isRefresh = true)
            is CourseListUiEvent.SearchQueryChanged -> onSearchQueryChanged(event.query)
            CourseListUiEvent.ClearError -> _state.update { it.copy(errorMessage = null) }
        }
    }

    private fun onSearchQueryChanged(query: String) {
        val currentCourses = _state.value.courses
        val currentEnrollments = _state.value.activeEnrollments
        val filteredCourses = filterCourses(query, currentCourses)
        val filteredEnrollments = filterEnrollments(query, currentEnrollments)

        _state.update {
            it.copy(
                searchQuery = query,
                filteredCourses = filteredCourses,
                filteredEnrollments = filteredEnrollments
            )
        }
    }

    private fun loadData(isRefresh: Boolean = false) {
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
                        filteredEnrollments = emptyList(),
                        enrollmentProgressMap = emptyMap(),
                        courses = emptyList(),
                        filteredCourses = emptyList(),
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
                            val query = _state.value.searchQuery
                            _state.update {
                                it.copy(
                                    activeEnrollments = activeList,
                                    filteredEnrollments = filterEnrollments(query, activeList)
                                )
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
                        is Resource.Success -> {
                            val publishedCourses = resource.data.orEmpty().filter { it.isPublished }
                            val query = _state.value.searchQuery
                            _state.update {
                                it.copy(
                                    courses = publishedCourses,
                                    filteredCourses = filterCourses(query, publishedCourses)
                                )
                            }
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
                            isRefresh -> "No se pudieron actualizar los cursos."
                            else -> "No se pudieron cargar los cursos. Inténtalo nuevamente."
                        }
                    )
                }
            }
        }
    }

    private fun filterCourses(query: String, courses: List<Course>): List<Course> {
        return if (query.isBlank()) {
            courses
        } else {
            courses.filter {
                it.title.contains(query, ignoreCase = true) ||
                it.description.contains(query, ignoreCase = true)
            }
        }
    }

    private fun filterEnrollments(query: String, enrollments: List<Enrollment>): List<Enrollment> {
        return if (query.isBlank()) {
            enrollments
        } else {
            enrollments.filter {
                it.courseTitle.contains(query, ignoreCase = true)
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
