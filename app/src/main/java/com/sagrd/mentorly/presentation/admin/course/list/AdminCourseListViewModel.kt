package com.sagrd.mentorly.presentation.admin.course.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sagrd.mentorly.data.remote.Resource
import com.sagrd.mentorly.data.remote.dto.course.UpdateCoursePublicationDto
import com.sagrd.mentorly.domain.model.student.StudentRole
import com.sagrd.mentorly.domain.repository.course.CourseRepository
import com.sagrd.mentorly.domain.repository.session.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminCourseListViewModel @Inject constructor(
    private val courseRepository: CourseRepository,
    private val sessionRepository: SessionRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(AdminCourseListUiState())
    val uiState: StateFlow<AdminCourseListUiState> = _uiState.asStateFlow()
    private var loadJob: Job? = null

    init { onEvent(AdminCourseListUiEvent.Load) }

    fun onEvent(event: AdminCourseListUiEvent) = when (event) {
        AdminCourseListUiEvent.Load -> loadCourses(false)
        AdminCourseListUiEvent.Refresh -> loadCourses(true)
        is AdminCourseListUiEvent.TogglePublication -> togglePublication(event.courseId)
        is AdminCourseListUiEvent.DeleteCourse -> deleteCourse(event.courseId)
        AdminCourseListUiEvent.ClearError -> _uiState.update { it.copy(errorMessage = null) }
    }

    private fun loadCourses(isRefresh: Boolean) {
        if (loadJob?.isActive == true) return
        loadJob = viewModelScope.launch {
            if (!ensureAdmin()) return@launch
            _uiState.update { it.copy(isLoading = !isRefresh && it.courses.isEmpty(), isRefreshing = isRefresh, errorMessage = null) }
            courseRepository.getCourses().collect { resource ->
                when (resource) {
                    is Resource.Loading -> Unit
                    is Resource.Success -> _uiState.update { it.copy(isLoading = false, isRefreshing = false, courses = resource.data.orEmpty()) }
                    is Resource.Error -> _uiState.update { it.copy(isLoading = false, isRefreshing = false, errorMessage = resource.message ?: "No se pudo cargar la lista de cursos.") }
                }
            }
        }
    }

    private fun togglePublication(courseId: String) = viewModelScope.launch {
        val session = adminSession() ?: return@launch
        val course = _uiState.value.courses.firstOrNull { it.id == courseId } ?: return@launch
        if (_uiState.value.publishingCourseId != null) return@launch
        _uiState.update { it.copy(publishingCourseId = courseId, errorMessage = null) }
        courseRepository.updateCoursePublication(session.studentId, courseId, UpdateCoursePublicationDto(!course.isPublished)).collect { resource ->
            when (resource) {
                is Resource.Loading -> Unit
                is Resource.Success -> _uiState.update { state -> state.copy(publishingCourseId = null, courses = state.courses.map { if (it.id == courseId) it.copy(isPublished = !it.isPublished) else it }) }
                is Resource.Error -> _uiState.update { it.copy(publishingCourseId = null, errorMessage = resource.message ?: "No se pudo cambiar el estado de publicación.") }
            }
        }
    }

    private fun deleteCourse(courseId: String) = viewModelScope.launch {
        val session = adminSession() ?: return@launch
        if (_uiState.value.deletingCourseId != null) return@launch
        _uiState.update { it.copy(deletingCourseId = courseId, errorMessage = null) }
        courseRepository.deleteCourse(session.studentId, courseId).collect { resource ->
            when (resource) {
                is Resource.Loading -> Unit
                is Resource.Success -> _uiState.update { state -> state.copy(deletingCourseId = null, courses = state.courses.filterNot { it.id == courseId }) }
                is Resource.Error -> _uiState.update { it.copy(deletingCourseId = null, errorMessage = resource.message ?: "No se pudo eliminar el curso.") }
            }
        }
    }

    private suspend fun ensureAdmin(): Boolean = adminSession() != null
    private suspend fun adminSession() = sessionRepository.session.first()?.takeIf { it.role == StudentRole.ADMIN }.also { session ->
        if (session == null) _uiState.update { it.copy(isLoading = false, isRefreshing = false, hasAdminAccess = false, errorMessage = "No tienes permisos para administrar cursos.") }
    }
}
