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
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class CourseFormViewModel @Inject constructor(
    private val courseRepository: CourseRepository,
    private val sessionRepository: SessionRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(CourseFormUiState())
    val state: StateFlow<CourseFormUiState> = _state.asStateFlow()

    private var courseId: String? = null

    fun onEvent(event: CourseFormUiEvent) {
        when (event) {
            is CourseFormUiEvent.Load -> load(event.courseId)
            is CourseFormUiEvent.TitleChanged -> updateFields { copy(title = event.value) }
            is CourseFormUiEvent.DescriptionChanged -> updateFields { copy(description = event.value) }
            is CourseFormUiEvent.ImageUrlChanged -> updateFields { copy(imageUrl = event.value) }
            is CourseFormUiEvent.RequiredPeerReviewsChanged -> {
                updateFields { copy(requiredPeerReviews = event.value) }
            }
            CourseFormUiEvent.Save -> save()
            CourseFormUiEvent.ClearError -> _state.update { it.copy(errorMessage = null) }
            CourseFormUiEvent.SavedHandled -> _state.update { it.copy(savedCourseId = null) }
        }
    }

    private fun updateFields(transform: CourseFormUiState.() -> CourseFormUiState) {
        _state.update { it.transform().copy(fieldErrors = emptyMap()) }
    }

    private fun load(id: String?) {
        viewModelScope.launch {
            val session = sessionRepository.session.first()

            if (session == null) {
                updateMissingSession()
            } else if (session.role != StudentRole.ADMIN) {
                updateMissingAdminAccess()
            } else {
                courseId = id
                _state.update {
                    CourseFormUiState(
                        isLoading = id != null,
                        isEditMode = id != null,
                        hasSession = true,
                        hasAdminAccess = true,
                    )
                }

                if (id != null) {
                    courseRepository.getCourseById(id).collect { resource ->
                        when (resource) {
                            is Resource.Loading -> Unit
                            is Resource.Success -> _state.update { state ->
                                resource.data?.let { course ->
                                    state.copy(
                                        isLoading = false,
                                        title = course.title,
                                        description = course.description,
                                        imageUrl = course.imageUrl.orEmpty(),
                                        requiredPeerReviews = course.requiredPeerReviews.toString(),
                                        isPublished = course.isPublished,
                                    )
                                } ?: state.copy(
                                    isLoading = false,
                                    errorMessage = "No se encontró el curso.",
                                )
                            }
                            is Resource.Error -> _state.update {
                                it.copy(
                                    isLoading = false,
                                    errorMessage = resource.message ?: "No se pudo cargar el curso.",
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun save() {
        viewModelScope.launch {
            val session = sessionRepository.session.first()
            val errors = validate()

            if (session == null) {
                updateMissingSession()
            } else if (session.role != StudentRole.ADMIN) {
                updateMissingAdminAccess()
            } else if (_state.value.isSaving) {
                Unit
            } else if (errors.isNotEmpty()) {
                _state.update { it.copy(fieldErrors = errors) }
            } else {
                val form = _state.value
                val peerReviews = form.requiredPeerReviews.toInt()
                _state.update { it.copy(isSaving = true, errorMessage = null) }

                if (courseId == null) {
                    createCourse(session.studentId, form, peerReviews)
                } else {
                    updateCourse(session.studentId, courseId.orEmpty(), form, peerReviews)
                }
            }
        }
    }

    private suspend fun createCourse(adminId: String, form: CourseFormUiState, peerReviews: Int) {
        courseRepository.createCourse(
            adminId,
            CreateCourseDto(
                form.title.trim(),
                form.description.trim(),
                peerReviews,
                form.imageUrl.trim().ifBlank { null },
            ),
        ).collect { resource ->
            when (resource) {
                is Resource.Loading -> Unit
                is Resource.Success -> _state.update {
                    it.copy(isSaving = false, savedCourseId = resource.data?.id)
                }
                is Resource.Error -> _state.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = resource.message ?: "No se pudo guardar el curso.",
                    )
                }
            }
        }
    }

    private suspend fun updateCourse(
        adminId: String,
        id: String,
        form: CourseFormUiState,
        peerReviews: Int,
    ) {
        courseRepository.updateCourse(
            adminId,
            id,
            UpdateCourseDto(
                form.title.trim(),
                form.description.trim(),
                peerReviews,
                form.imageUrl.trim().ifBlank { null },
            ),
        ).collect { resource ->
            when (resource) {
                is Resource.Loading -> Unit
                is Resource.Success -> _state.update {
                    it.copy(isSaving = false, savedCourseId = id)
                }
                is Resource.Error -> _state.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = resource.message ?: "No se pudo guardar el curso.",
                    )
                }
            }
        }
    }

    private fun validate(): Map<String, String> = buildMap {
        val form = _state.value
        if (form.title.isBlank()) put("title", "El título es obligatorio.")
        if (form.description.isBlank()) put("description", "La descripción es obligatoria.")
        if (form.requiredPeerReviews.toIntOrNull()?.takeIf { it >= 0 } == null) {
            put("requiredPeerReviews", "Debe ser un entero mayor o igual que cero.")
        }
        if (
            form.imageUrl.isNotBlank() &&
                !form.imageUrl.startsWith("http://") &&
                !form.imageUrl.startsWith("https://")
        ) {
            put("imageUrl", "La URL de imagen debe comenzar con http:// o https://.")
        }
    }

    private fun updateMissingSession() {
        _state.update {
            it.copy(
                isLoading = false,
                isSaving = false,
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
                isSaving = false,
                hasSession = true,
                hasAdminAccess = false,
                errorMessage = "No tienes permisos para administrar cursos.",
            )
        }
    }
}
