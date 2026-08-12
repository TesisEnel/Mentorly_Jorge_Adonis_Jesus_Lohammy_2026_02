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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UnitFormViewModel @Inject constructor(
    private val repo: UnitRepository,
    private val session: SessionRepository
) : ViewModel() {
    private val _state = MutableStateFlow(UnitFormUiState())
    val state: StateFlow<UnitFormUiState> = _state.asStateFlow()

    private var courseId = ""
    private var unitId: String? = null

    fun onEvent(event: UnitFormUiEvent) {
        when (event) {
            is UnitFormUiEvent.Load -> load(event.courseId, event.unitId)
            is UnitFormUiEvent.TitleChanged -> _state.update { it.copy(title = event.value, fieldErrors = emptyMap()) }
            is UnitFormUiEvent.OrderChanged -> _state.update { it.copy(orderIndex = event.value) }
            UnitFormUiEvent.Save -> save()
            UnitFormUiEvent.ClearError -> _state.update { it.copy(errorMessage = null) }
        }
    }

    private fun load(course: String, id: String?) = viewModelScope.launch {
        if (admin() != true) return@launch
        courseId = course
        unitId = id
        _state.update { it.copy(isEditMode = id != null, isLoading = id != null) }
        if (id != null) repo.getUnitsByCourseId(course).collect { resource ->
            when (resource) {
                is Resource.Success -> resource.data?.firstOrNull { it.id == id }?.let { unit ->
                    _state.update { it.copy(isLoading = false, title = unit.title, orderIndex = unit.orderIndex.toString()) }
                }
                is Resource.Error -> _state.update { it.copy(isLoading = false, errorMessage = resource.message ?: "No se pudo cargar la unidad.") }
                else -> Unit
            }
        }
    }

    private fun save() = viewModelScope.launch {
        if (_state.value.isSaving || admin() != true) return@launch
        val current = _state.value
        val order = current.orderIndex.toIntOrNull()
        if (current.title.isBlank() || order == null) {
            _state.update { it.copy(fieldErrors = buildMap { if (current.title.isBlank()) put("title", "El título es obligatorio."); if (order == null) put("order", "El orden debe ser un entero.") }) }
            return@launch
        }
        val adminId = session.session.first()!!.studentId
        _state.update { it.copy(isSaving = true) }
        val request = if (unitId == null) repo.createUnit(adminId, courseId, CreateUnitDto(current.title.trim(), order)) else repo.updateUnit(adminId, unitId!!, UpdateUnitDto(current.title.trim(), order))
        request.collect { resource ->
            when (resource) {
                is Resource.Success -> _state.update { it.copy(isSaving = false, isSaved = true) }
                is Resource.Error -> _state.update { it.copy(isSaving = false, errorMessage = resource.message ?: "No se pudo guardar la unidad.") }
                else -> Unit
            }
        }
    }

    private suspend fun admin() = (session.session.first()?.takeIf { it.role == StudentRole.ADMIN } != null).also { allowed ->
        if (!allowed) _state.update { it.copy(hasAdminAccess = false, errorMessage = "No tienes permisos para administrar el contenido del curso.") }
    }
}
