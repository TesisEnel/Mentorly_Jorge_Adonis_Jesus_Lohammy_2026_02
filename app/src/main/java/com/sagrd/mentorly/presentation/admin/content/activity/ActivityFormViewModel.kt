package com.sagrd.mentorly.presentation.admin.content.activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sagrd.mentorly.data.remote.Resource
import com.sagrd.mentorly.data.remote.dto.activity.CreateActivityDto
import com.sagrd.mentorly.data.remote.dto.activity.UpdateActivityDto
import com.sagrd.mentorly.domain.model.content.ActivityType
import com.sagrd.mentorly.domain.model.content.ApprovalStrategy
import com.sagrd.mentorly.domain.model.student.StudentRole
import com.sagrd.mentorly.domain.repository.activity.ActivityRepository
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
class ActivityFormViewModel @Inject constructor(
    private val activityRepository: ActivityRepository,
    private val sessionRepository: SessionRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ActivityFormUiState())
    val state: StateFlow<ActivityFormUiState> = _state.asStateFlow()

    private var themeId = ""
    private var activityId: String? = null

    fun onEvent(event: ActivityFormUiEvent) {
        when (event) {
            is ActivityFormUiEvent.Load -> load(event.themeId, event.activityId)
            is ActivityFormUiEvent.TitleChanged -> _state.update {
                it.copy(title = event.value, fieldErrors = emptyMap())
            }
            is ActivityFormUiEvent.TypeChanged -> _state.update {
                it.copy(
                    type = event.value,
                    approvalStrategy = if (event.value == ActivityType.QUIZ) ApprovalStrategy.AUTO else it.approvalStrategy,
                )
            }
            is ActivityFormUiEvent.MandatoryChanged -> _state.update { it.copy(isMandatory = event.value) }
            is ActivityFormUiEvent.StrategyChanged -> _state.update {
                if (it.type == ActivityType.QUIZ) it else it.copy(approvalStrategy = event.value)
            }
            is ActivityFormUiEvent.OrderChanged -> _state.update {
                it.copy(orderIndex = event.value, fieldErrors = emptyMap())
            }
            ActivityFormUiEvent.Save -> save()
            ActivityFormUiEvent.DeleteActivity -> deleteActivity()
            ActivityFormUiEvent.ClearError -> _state.update { it.copy(errorMessage = null) }
            ActivityFormUiEvent.SavedHandled -> _state.update { it.copy(isSaved = false) }
            ActivityFormUiEvent.DeletedHandled -> _state.update { it.copy(isDeleted = false) }
        }
    }

    private fun load(newThemeId: String, newActivityId: String?) {
        viewModelScope.launch {
            val session = sessionRepository.session.first()

            if (session == null) {
                updateMissingSession()
            } else if (session.role != StudentRole.ADMIN) {
                updateMissingAdminAccess()
            } else {
                themeId = newThemeId
                activityId = newActivityId
                _state.update {
                    ActivityFormUiState(
                        isLoading = newActivityId != null,
                        isEditMode = newActivityId != null,
                        hasSession = true,
                        hasAdminAccess = true,
                    )
                }

                if (newActivityId != null) {
                    activityRepository.getActivities(newThemeId).collect { resource ->
                        when (resource) {
                            is Resource.Loading -> Unit
                            is Resource.Success -> _state.update { state ->
                                resource.data
                                    ?.firstOrNull { it.id == newActivityId }
                                    ?.let { activity ->
                                        state.copy(
                                            isLoading = false,
                                            title = activity.title,
                                            type = activity.type,
                                            isMandatory = activity.isMandatory,
                                            approvalStrategy = activity.approvalStrategy,
                                            orderIndex = activity.orderIndex.toString(),
                                        )
                                    }
                                    ?: state.copy(
                                        isLoading = false,
                                        errorMessage = "No se encontró la actividad.",
                                    )
                            }
                            is Resource.Error -> _state.update {
                                it.copy(
                                    isLoading = false,
                                    errorMessage = resource.message ?: "No se pudo cargar la actividad.",
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
            } else if (form.isSaving || form.isDeleting) {
                Unit
            } else if (errors.isNotEmpty()) {
                _state.update { it.copy(fieldErrors = errors) }
            } else {
                _state.update { it.copy(isSaving = true, errorMessage = null) }
                val dto = form.toCreateDto()

                if (activityId == null) {
                    activityRepository
                        .createActivity(session.studentId, themeId, dto)
                        .collect(::handleSaveResult)
                } else {
                    activityRepository
                        .updateActivity(session.studentId, activityId.orEmpty(), dto.toUpdateDto())
                        .collect(::handleSaveResult)
                }
            }
        }
    }

    private fun deleteActivity() {
        viewModelScope.launch {
            val session = sessionRepository.session.first()

            if (session == null) {
                updateMissingSession()
            } else if (session.role != StudentRole.ADMIN) {
                updateMissingAdminAccess()
            } else if (activityId != null && !_state.value.isDeleting) {
                _state.update { it.copy(isDeleting = true, errorMessage = null) }
                activityRepository.deleteActivity(session.studentId, activityId.orEmpty()).collect { resource ->
                    when (resource) {
                        is Resource.Loading -> Unit
                        is Resource.Success -> _state.update {
                            it.copy(isDeleting = false, isDeleted = true)
                        }
                        is Resource.Error -> _state.update {
                            it.copy(
                                isDeleting = false,
                                errorMessage = resource.message ?: "No se pudo eliminar la actividad.",
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
                    errorMessage = resource.message ?: "No se pudo guardar la actividad.",
                )
            }
        }
    }

    private fun validate(form: ActivityFormUiState): Map<String, String> = buildMap {
        if (form.title.isBlank()) put("title", "El título es obligatorio.")
        if (form.orderIndex.toIntOrNull() == null) put("order", "El orden debe ser un entero.")
    }

    private fun ActivityFormUiState.toCreateDto() = CreateActivityDto(
        title = title.trim(),
        type = if (type == ActivityType.EXERCISE) 1 else 2,
        isMandatory = isMandatory,
        approvalStrategy = approvalStrategy.toApiValue(),
        orderIndex = orderIndex.toInt(),
    )

    private fun CreateActivityDto.toUpdateDto() = UpdateActivityDto(
        title = title,
        type = type,
        isMandatory = isMandatory,
        approvalStrategy = approvalStrategy,
        orderIndex = orderIndex,
    )

    private fun ApprovalStrategy.toApiValue() = when (this) {
        ApprovalStrategy.AUTO -> 1
        ApprovalStrategy.PEER_REVIEW -> 2
        ApprovalStrategy.ADMIN -> 3
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
