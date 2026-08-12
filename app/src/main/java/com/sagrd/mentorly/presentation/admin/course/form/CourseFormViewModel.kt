package com.sagrd.mentorly.presentation.admin.course.form

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sagrd.mentorly.data.remote.Resource
import com.sagrd.mentorly.data.remote.dto.course.CreateCourseDto
import com.sagrd.mentorly.data.remote.dto.course.UpdateCourseDto
import com.sagrd.mentorly.domain.model.student.StudentRole
import com.sagrd.mentorly.domain.repository.course.CourseRepository
import com.sagrd.mentorly.domain.repository.session.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CourseFormViewModel @Inject constructor(
    private val courseRepository: CourseRepository,
    private val sessionRepository: SessionRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(CourseFormUiState())
    val uiState: StateFlow<CourseFormUiState> = _uiState.asStateFlow()
    private var courseId: String? = null

    fun onEvent(event: CourseFormUiEvent) = when (event) {
        is CourseFormUiEvent.Load -> load(event.courseId)
        is CourseFormUiEvent.TitleChanged -> change { copy(title = event.value) }
        is CourseFormUiEvent.DescriptionChanged -> change { copy(description = event.value) }
        is CourseFormUiEvent.ImageUrlChanged -> change { copy(imageUrl = event.value) }
        is CourseFormUiEvent.RequiredPeerReviewsChanged -> change { copy(requiredPeerReviews = event.value) }
        CourseFormUiEvent.Save -> save()
        CourseFormUiEvent.ClearError -> _uiState.update { it.copy(errorMessage = null) }
    }

    private fun change(transform: CourseFormUiState.() -> CourseFormUiState) = _uiState.update { it.transform().copy(fieldErrors = emptyMap()) }

    private fun load(id: String?) = viewModelScope.launch {
        if (!isAdmin()) return@launch
        if (courseId == id && (id == null || _uiState.value.title.isNotBlank())) return@launch
        courseId = id
        _uiState.update { it.copy(isEditMode = id != null, isLoading = id != null, errorMessage = null) }
        if (id == null) return@launch
        courseRepository.getCourseById(id).collect { resource -> when (resource) {
            is Resource.Loading -> Unit
            is Resource.Success -> resource.data?.let { course -> _uiState.update { it.copy(isLoading = false, title = course.title, description = course.description, imageUrl = course.imageUrl.orEmpty(), requiredPeerReviews = course.requiredPeerReviews.toString(), isPublished = course.isPublished) } }
            is Resource.Error -> _uiState.update { it.copy(isLoading = false, errorMessage = resource.message ?: "No se pudo cargar el curso.") }
        } }
    }

    private fun save() = viewModelScope.launch {
        if (_uiState.value.isSaving || !isAdmin()) return@launch
        val errors = validate()
        if (errors.isNotEmpty()) { _uiState.update { it.copy(fieldErrors = errors) }; return@launch }
        val state = _uiState.value
        val session = sessionRepository.session.first() ?: return@launch
        _uiState.update { it.copy(isSaving = true, errorMessage = null) }
        val peers = state.requiredPeerReviews.toInt()
        if (courseId == null) {
            courseRepository.createCourse(session.studentId, CreateCourseDto(state.title.trim(), state.description.trim(), peers, state.imageUrl.trim().ifBlank { null })).collect { resource -> when (resource) {
                is Resource.Loading -> Unit
                is Resource.Success -> _uiState.update { it.copy(isSaving = false, savedCourseId = resource.data?.id) }
                is Resource.Error -> _uiState.update { it.copy(isSaving = false, errorMessage = resource.message ?: "No se pudo guardar el curso.") }
            } }
        } else {
            courseRepository.updateCourse(session.studentId, courseId!!, UpdateCourseDto(state.title.trim(), state.description.trim(), peers, state.imageUrl.trim().ifBlank { null })).collect { resource -> when (resource) {
                is Resource.Loading -> Unit
                is Resource.Success -> _uiState.update { it.copy(isSaving = false, savedCourseId = courseId) }
                is Resource.Error -> _uiState.update { it.copy(isSaving = false, errorMessage = resource.message ?: "No se pudo guardar el curso.") }
            } }
        }
    }

    private fun validate(): Map<String, String> = buildMap {
        val state = _uiState.value
        if (state.title.isBlank()) put("title", "El título es obligatorio.")
        if (state.description.isBlank()) put("description", "La descripción es obligatoria.")
        if (state.requiredPeerReviews.toIntOrNull()?.takeIf { it >= 0 } == null) put("requiredPeerReviews", "Debe ser un entero mayor o igual que cero.")
        if (state.imageUrl.isNotBlank() && !state.imageUrl.startsWith("http://") && !state.imageUrl.startsWith("https://")) put("imageUrl", "La URL de imagen debe comenzar con http:// o https://.")
    }

    private suspend fun isAdmin(): Boolean = sessionRepository.session.first()?.takeIf { it.role == StudentRole.ADMIN }?.let { true } ?: false.also {
        _uiState.update { state -> state.copy(isLoading = false, isSaving = false, hasAdminAccess = false, errorMessage = "No tienes permisos para administrar cursos.") }
    }
}
