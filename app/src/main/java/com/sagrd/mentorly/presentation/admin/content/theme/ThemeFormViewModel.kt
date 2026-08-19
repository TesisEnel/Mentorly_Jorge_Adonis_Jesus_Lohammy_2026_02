package com.sagrd.mentorly.presentation.admin.content.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sagrd.mentorly.data.remote.Resource
import com.sagrd.mentorly.data.remote.dto.theme.CreateThemeDto
import com.sagrd.mentorly.data.remote.dto.theme.UpdateThemeDto
import com.sagrd.mentorly.domain.model.student.StudentRole
import com.sagrd.mentorly.domain.repository.session.SessionRepository
import com.sagrd.mentorly.domain.repository.theme.ThemeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ThemeFormViewModel @Inject constructor(
    private val themeRepository: ThemeRepository,
    private val sessionRepository: SessionRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ThemeFormUiState())
    val state: StateFlow<ThemeFormUiState> = _state.asStateFlow()

    private var unitId = ""
    private var themeId: String? = null

    fun onEvent(event: ThemeFormUiEvent) {
        when (event) {
            is ThemeFormUiEvent.Load -> load(event.unitId, event.themeId)
            is ThemeFormUiEvent.TitleChanged -> _state.update {
                it.copy(title = event.value, fieldErrors = emptyMap())
            }
            is ThemeFormUiEvent.ContentChanged -> _state.update {
                it.copy(contentText = event.value, fieldErrors = emptyMap())
            }
            is ThemeFormUiEvent.OrderChanged -> _state.update {
                it.copy(orderIndex = event.value, fieldErrors = emptyMap())
            }
            ThemeFormUiEvent.Save -> save()
            ThemeFormUiEvent.DeleteTheme -> deleteTheme()
            ThemeFormUiEvent.ClearError -> _state.update { it.copy(errorMessage = null) }
            ThemeFormUiEvent.SavedHandled -> _state.update { it.copy(isSaved = false) }
            ThemeFormUiEvent.DeletedHandled -> _state.update { it.copy(isDeleted = false) }
        }
    }

    private fun load(newUnitId: String, newThemeId: String?) {
        viewModelScope.launch {
            val session = sessionRepository.session.first()

            if (session == null) {
                updateMissingSession()
            } else if (session.role != StudentRole.ADMIN) {
                updateMissingAdminAccess()
            } else {
                unitId = newUnitId
                themeId = newThemeId
                _state.update {
                    ThemeFormUiState(
                        isLoading = newThemeId != null,
                        isEditMode = newThemeId != null,
                        hasSession = true,
                        hasAdminAccess = true,
                    )
                }

                if (newThemeId != null) {
                    themeRepository.getThemesByUnit(newUnitId).collect { resource ->
                        when (resource) {
                            is Resource.Loading -> Unit
                            is Resource.Success -> _state.update { state ->
                                resource.data
                                    ?.firstOrNull { it.id == newThemeId }
                                    ?.let { theme ->
                                        state.copy(
                                            isLoading = false,
                                            title = theme.title,
                                            contentText = theme.contentText,
                                            orderIndex = theme.orderIndex.toString(),
                                        )
                                    }
                                    ?: state.copy(
                                        isLoading = false,
                                        errorMessage = "No se encontró el tema.",
                                    )
                            }
                            is Resource.Error -> _state.update {
                                it.copy(
                                    isLoading = false,
                                    errorMessage = resource.message ?: "No se pudo cargar el tema.",
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
            val form = _state.value
            val errors = validate(form)

            if (session == null) {
                updateMissingSession()
            } else if (session.role != StudentRole.ADMIN) {
                updateMissingAdminAccess()
            } else if (form.isSaving) {
                Unit
            } else if (errors.isNotEmpty()) {
                _state.update { it.copy(fieldErrors = errors) }
            } else {
                val order = form.orderIndex.toInt()
                _state.update { it.copy(isSaving = true, errorMessage = null) }

                if (themeId == null) {
                    themeRepository
                        .createTheme(
                            session.studentId,
                            unitId,
                            CreateThemeDto(form.title.trim(), form.contentText.trim(), order),
                        ).collect(::handleSaveResult)
                } else {
                    themeRepository
                        .updateTheme(
                            session.studentId,
                            themeId.orEmpty(),
                            UpdateThemeDto(form.title.trim(), form.contentText.trim(), order),
                        ).collect(::handleSaveResult)
                }
            }
        }
    }

    private fun deleteTheme() {
        viewModelScope.launch {
            val session = sessionRepository.session.first()

            if (session == null) {
                updateMissingSession()
            } else if (session.role != StudentRole.ADMIN) {
                updateMissingAdminAccess()
            } else if (themeId != null && !_state.value.isDeleting) {
                _state.update { it.copy(isDeleting = true, errorMessage = null) }
                themeRepository.deleteTheme(session.studentId, themeId.orEmpty()).collect { resource ->
                    when (resource) {
                        is Resource.Loading -> Unit
                        is Resource.Success -> _state.update {
                            it.copy(isDeleting = false, isDeleted = true)
                        }
                        is Resource.Error -> _state.update {
                            it.copy(
                                isDeleting = false,
                                errorMessage = resource.message ?: "No se pudo eliminar el tema.",
                            )
                        }
                    }
                }
            }
        }
    }

    private fun handleSaveResult(resource: Resource<*>) {
        when (resource) {
            is Resource.Loading -> Unit
            is Resource.Success -> _state.update { it.copy(isSaving = false, isSaved = true) }
            is Resource.Error -> _state.update {
                it.copy(
                    isSaving = false,
                    errorMessage = resource.message ?: "No se pudo guardar el tema.",
                )
            }
        }
    }

    private fun validate(form: ThemeFormUiState): Map<String, String> = buildMap {
        if (form.title.isBlank()) put("title", "El título es obligatorio.")
        if (form.contentText.isBlank()) put("content", "El contenido del tema es obligatorio.")
        if (form.orderIndex.toIntOrNull() == null) put("order", "El orden debe ser un entero.")
    }

    private fun updateMissingSession() {
        _state.update {
            it.copy(
                isLoading = false,
                isSaving = false,
                isDeleting = false,
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
                isDeleting = false,
                hasSession = true,
                hasAdminAccess = false,
                errorMessage = "No tienes permisos para administrar el contenido del curso.",
            )
        }
    }
}
