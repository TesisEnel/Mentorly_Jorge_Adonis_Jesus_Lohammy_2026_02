package com.sagrd.mentorly.presentation.admin.course.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sagrd.mentorly.data.remote.Resource
import com.sagrd.mentorly.data.remote.dto.course.UpdateCoursePublicationDto
import com.sagrd.mentorly.domain.model.student.StudentRole
import com.sagrd.mentorly.domain.repository.course.CourseRepository
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
class AdminCourseListViewModel @Inject constructor(
    private val courseRepository: CourseRepository,
    private val sessionRepository: SessionRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(AdminCourseListUiState())
    val state: StateFlow<AdminCourseListUiState> = _state.asStateFlow()

    init {
        onEvent(AdminCourseListUiEvent.Load)
    }

    fun onEvent(event: AdminCourseListUiEvent) {
        when (event) {
            AdminCourseListUiEvent.Load -> loadCourses(isRefresh = false)
            AdminCourseListUiEvent.Refresh -> loadCourses(isRefresh = true)
            is AdminCourseListUiEvent.SearchQueryChanged -> {
                _state.update { it.copy(searchQuery = event.query) }
            }
            is AdminCourseListUiEvent.TogglePublication -> togglePublication(event.courseId)
            is AdminCourseListUiEvent.DeleteCourse -> deleteCourse(event.courseId)
            AdminCourseListUiEvent.ClearError -> _state.update { it.copy(errorMessage = null) }
        }
    }

    private fun loadCourses(isRefresh: Boolean) {
        viewModelScope.launch {
            val session = sessionRepository.session.first()

            if (session == null) {
                updateMissingSession()
            } else if (session.role != StudentRole.ADMIN) {
                updateMissingAdminAccess()
            } else {
                _state.update {
                    it.copy(
                        isLoading = !isRefresh && it.courses.isEmpty(),
                        isRefreshing = isRefresh,
                        hasSession = true,
                        hasAdminAccess = true,
                        errorMessage = null,
                    )
                }

                courseRepository.getCourses().collect { resource ->
                    when (resource) {
                        is Resource.Loading -> Unit
                        is Resource.Success -> _state.update {
                            it.copy(
                                isLoading = false,
                                isRefreshing = false,
                                courses = resource.data.orEmpty(),
                            )
                        }
                        is Resource.Error -> _state.update {
                            it.copy(
                                isLoading = false,
                                isRefreshing = false,
                                errorMessage = resource.message ?: "No se pudo cargar la lista de cursos.",
                            )
                        }
                    }
                }
            }
        }
    }

    private fun togglePublication(courseId: String) {
        viewModelScope.launch {
            val session = sessionRepository.session.first()
            val course = _state.value.courses.firstOrNull { it.id == courseId }

            if (session == null) {
                updateMissingSession()
            } else if (session.role != StudentRole.ADMIN) {
                updateMissingAdminAccess()
            } else if (course != null && _state.value.publishingCourseId == null) {
                _state.update { it.copy(publishingCourseId = courseId, errorMessage = null) }
                courseRepository
                    .updateCoursePublication(
                        session.studentId,
                        courseId,
                        UpdateCoursePublicationDto(!course.isPublished),
                    ).collect { resource ->
                        when (resource) {
                            is Resource.Loading -> Unit
                            is Resource.Success -> _state.update { state ->
                                state.copy(
                                    publishingCourseId = null,
                                    courses = state.courses.map {
                                        if (it.id == courseId) it.copy(isPublished = !it.isPublished) else it
                                    },
                                )
                            }
                            is Resource.Error -> _state.update {
                                it.copy(
                                    publishingCourseId = null,
                                    errorMessage =
                                        resource.message ?: "No se pudo cambiar el estado de publicación.",
                                )
                            }
                        }
                    }
            }
        }
    }

    private fun deleteCourse(courseId: String) {
        viewModelScope.launch {
            val session = sessionRepository.session.first()

            if (session == null) {
                updateMissingSession()
            } else if (session.role != StudentRole.ADMIN) {
                updateMissingAdminAccess()
            } else if (_state.value.deletingCourseId == null) {
                _state.update { it.copy(deletingCourseId = courseId, errorMessage = null) }
                courseRepository.deleteCourse(session.studentId, courseId).collect { resource ->
                    when (resource) {
                        is Resource.Loading -> Unit
                        is Resource.Success -> _state.update { state ->
                            state.copy(
                                deletingCourseId = null,
                                courses = state.courses.filterNot { it.id == courseId },
                            )
                        }
                        is Resource.Error -> _state.update {
                            it.copy(
                                deletingCourseId = null,
                                errorMessage = resource.message ?: "No se pudo eliminar el curso.",
                            )
                        }
                    }
                }
            }
        }
    }

    private fun updateMissingSession() {
        _state.update {
            it.copy(
                isLoading = false,
                isRefreshing = false,
                hasSession = false,
                hasAdminAccess = false,
                errorMessage = "No se encontró una sesión activa.",
            )
        }
    }

    private fun updateMissingAdminAccess() {
        _state.update {
            it.copy(
                isLoading = false,
                isRefreshing = false,
                hasSession = true,
                hasAdminAccess = false,
                errorMessage = "No tienes permisos para administrar cursos.",
            )
        }
    }
}
