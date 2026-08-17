package com.sagrd.mentorly.presentation.admin.content.unit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sagrd.mentorly.data.remote.Resource
import com.sagrd.mentorly.data.remote.dto.unit.CreateUnitDto
import com.sagrd.mentorly.data.remote.dto.unit.UpdateUnitDto
import com.sagrd.mentorly.domain.model.student.StudentRole
import com.sagrd.mentorly.domain.repository.session.SessionRepository
import com.sagrd.mentorly.domain.repository.unit.UnitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class UnitFormViewModel @Inject constructor(
    private val unitRepository: UnitRepository,
    private val sessionRepository: SessionRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(UnitFormUiState())
    val state: StateFlow<UnitFormUiState> = _state.asStateFlow()

    private var courseId = ""
    private var unitId: String? = null

    fun onEvent(event: UnitFormUiEvent) {
        when (event) {
            is UnitFormUiEvent.Load -> load(event.courseId, event.unitId)
            is UnitFormUiEvent.TitleChanged -> _state.update {
                it.copy(title = event.value, fieldErrors = emptyMap())
            }
            is UnitFormUiEvent.OrderChanged -> _state.update {
                it.copy(orderIndex = event.value, fieldErrors = emptyMap())
            }
            UnitFormUiEvent.Save -> save()
            UnitFormUiEvent.DeleteUnit -> deleteUnit()
            UnitFormUiEvent.ClearError -> _state.update { it.copy(errorMessage = null) }
            UnitFormUiEvent.SavedHandled -> _state.update { it.copy(isSaved = false) }
            UnitFormUiEvent.DeletedHandled -> _state.update { it.copy(isDeleted = false) }
        }
    }

    private fun load(newCourseId: String, newUnitId: String?) {
        viewModelScope.launch {
            val session = sessionRepository.session.first()

            if (session == null) {
                updateMissingSession()
            } else if (session.role != StudentRole.ADMIN) {
                updateMissingAdminAccess()
            } else if (courseId != newCourseId || unitId != newUnitId) {
                courseId = newCourseId
                unitId = newUnitId
                _state.update {
                    UnitFormUiState(
                        isLoading = newUnitId != null,
                        isEditMode = newUnitId != null,
                        hasSession = true,
                        hasAdminAccess = true,
                    )
                }

                if (newUnitId != null) {
                    unitRepository.getUnitsByCourseId(newCourseId).collect { resource ->
                        when (resource) {
                            is Resource.Loading -> Unit
                            is Resource.Success -> _state.update { state ->
                                resource.data
                                    ?.firstOrNull { it.id == newUnitId }
                                    ?.let { unit ->
                                        state.copy(
                                            isLoading = false,
                                            title = unit.title,
                                            orderIndex = unit.orderIndex.toString(),
                                        )
                                    }
                                    ?: state.copy(
                                        isLoading = false,
                                        errorMessage = "No se encontró la unidad.",
                                    )
                            }
                            is Resource.Error -> _state.update {
                                it.copy(
                                    isLoading = false,
                                    errorMessage = resource.message ?: "No se pudo cargar la unidad.",
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

                if (unitId == null) {
                    unitRepository
                        .createUnit(session.studentId, courseId, CreateUnitDto(form.title.trim(), order))
                        .collect(::handleSaveResult)
                } else {
                    unitRepository
                        .updateUnit(
                            session.studentId,
                            unitId.orEmpty(),
                            UpdateUnitDto(form.title.trim(), order),
                        ).collect(::handleSaveResult)
                }
            }
        }
    }

    private fun deleteUnit() {
        viewModelScope.launch {
            val session = sessionRepository.session.first()

            if (session == null) {
                updateMissingSession()
            } else if (session.role != StudentRole.ADMIN) {
                updateMissingAdminAccess()
            } else if (unitId != null && !_state.value.isDeleting) {
                _state.update { it.copy(isDeleting = true, errorMessage = null) }
                unitRepository.deleteUnit(session.studentId, unitId.orEmpty()).collect { resource ->
                    when (resource) {
                        is Resource.Loading -> Unit
                        is Resource.Success -> _state.update {
                            it.copy(isDeleting = false, isDeleted = true)
                        }
                        is Resource.Error -> _state.update {
                            it.copy(
                                isDeleting = false,
                                errorMessage = resource.message ?: "No se pudo eliminar la unidad.",
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
                    errorMessage = resource.message ?: "No se pudo guardar la unidad.",
                )
            }
        }
    }

    private fun validate(form: UnitFormUiState): Map<String, String> = buildMap {
        if (form.title.isBlank()) put("title", "El título es obligatorio.")
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
