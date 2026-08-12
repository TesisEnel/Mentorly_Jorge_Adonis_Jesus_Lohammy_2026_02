package com.sagrd.mentorly.presentation.admin.content.activity

import androidx.lifecycle.*
import com.sagrd.mentorly.data.remote.*
import com.sagrd.mentorly.data.remote.dto.activity.*
import com.sagrd.mentorly.domain.model.content.*
import com.sagrd.mentorly.domain.model.student.StudentRole
import com.sagrd.mentorly.domain.repository.activity.ActivityRepository
import com.sagrd.mentorly.domain.repository.session.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@HiltViewModel
class ActivityFormViewModel
@Inject
constructor(private val repo: ActivityRepository, private val session: SessionRepository) :
    ViewModel() {
    private val _state = MutableStateFlow(ActivityFormUiState())
    val state: StateFlow<ActivityFormUiState> = _state.asStateFlow()
    private var themeId = ""
    private var activityId: String? = null

    fun onEvent(e: ActivityFormUiEvent) {
        when (e) {
            is ActivityFormUiEvent.Load -> load(e.themeId, e.activityId)
            is ActivityFormUiEvent.TitleChanged ->
                _state.update { it.copy(title = e.value, fieldErrors = emptyMap()) }
            is ActivityFormUiEvent.TypeChanged ->
                _state.update {
                    it.copy(
                        type = e.value,
                        approvalStrategy =
                            if (e.value == ActivityType.QUIZ) ApprovalStrategy.AUTO
                            else it.approvalStrategy,
                    )
                }
            is ActivityFormUiEvent.MandatoryChanged ->
                _state.update { it.copy(isMandatory = e.value) }
            is ActivityFormUiEvent.StrategyChanged ->
                _state.update {
                    if (it.type == ActivityType.QUIZ) it else it.copy(approvalStrategy = e.value)
                }
            is ActivityFormUiEvent.OrderChanged -> _state.update { it.copy(orderIndex = e.value) }
            ActivityFormUiEvent.Save -> save()
            ActivityFormUiEvent.ClearError -> _state.update { it.copy(errorMessage = null) }
        }
    }

    private fun load(theme: String, id: String?) =
        viewModelScope.launch {
            if (admin() != true) return@launch
            themeId = theme
            activityId = id
            _state.update { it.copy(isEditMode = id != null, isLoading = id != null) }
            if (id != null)
                repo.getActivities(theme).collect { r ->
                    when (r) {
                        is Resource.Success ->
                            r.data
                                ?.firstOrNull { it.id == id }
                                ?.let { a ->
                                    _state.update { s ->
                                        s.copy(
                                            isLoading = false,
                                            title = a.title,
                                            type = a.type,
                                            isMandatory = a.isMandatory,
                                            approvalStrategy = a.approvalStrategy,
                                            orderIndex = a.orderIndex.toString(),
                                        )
                                    }
                                }
                        is Resource.Error ->
                            _state.update {
                                it.copy(
                                    isLoading = false,
                                    errorMessage = r.message ?: "No se pudo cargar la actividad.",
                                )
                            }
                        else -> Unit
                    }
                }
        }

    private fun save() =
        viewModelScope.launch {
            if (_state.value.isSaving || admin() != true) return@launch
            val s = _state.value
            val order = s.orderIndex.toIntOrNull()
            if (s.title.isBlank() || order == null) {
                _state.update {
                    it.copy(
                        fieldErrors =
                            buildMap {
                                if (s.title.isBlank()) put("title", "El título es obligatorio.")
                                if (order == null) put("order", "El orden debe ser un entero.")
                            }
                    )
                }
                return@launch
            }
            val a = session.session.first()!!.studentId
            val type = if (s.type == ActivityType.EXERCISE) 1 else 2
            val strategy =
                when (
                    if (s.type == ActivityType.QUIZ) ApprovalStrategy.AUTO else s.approvalStrategy
                ) {
                    ApprovalStrategy.AUTO -> 1
                    ApprovalStrategy.PEER_REVIEW -> 2
                    ApprovalStrategy.ADMIN -> 3
                }
            _state.update { it.copy(isSaving = true) }
            val flow =
                if (activityId == null)
                    repo.createActivity(
                        a,
                        themeId,
                        CreateActivityDto(s.title.trim(), type, s.isMandatory, strategy, order),
                    )
                else
                    repo.updateActivity(
                        a,
                        activityId!!,
                        UpdateActivityDto(s.title.trim(), type, s.isMandatory, strategy, order),
                    )
            flow.collect { r ->
                when (r) {
                    is Resource.Success ->
                        _state.update { it.copy(isSaving = false, isSaved = true) }
                    is Resource.Error ->
                        _state.update {
                            it.copy(
                                isSaving = false,
                                errorMessage = r.message ?: "No se pudo guardar la actividad.",
                            )
                        }
                    else -> Unit
                }
            }
        }

    private suspend fun admin() =
        (session.session.first()?.takeIf { it.role == StudentRole.ADMIN } != null).also {
            if (!it)
                _state.update { s ->
                    s.copy(
                        hasAdminAccess = false,
                        errorMessage = "No tienes permisos para administrar el contenido del curso.",
                    )
                }
        }
}
