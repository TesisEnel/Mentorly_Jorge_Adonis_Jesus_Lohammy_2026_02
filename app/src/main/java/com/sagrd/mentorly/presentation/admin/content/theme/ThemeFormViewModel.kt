package com.sagrd.mentorly.presentation.admin.content.theme

import androidx.lifecycle.*
import com.sagrd.mentorly.data.remote.*
import com.sagrd.mentorly.data.remote.dto.theme.*
import com.sagrd.mentorly.domain.model.student.StudentRole
import com.sagrd.mentorly.domain.repository.session.SessionRepository
import com.sagrd.mentorly.domain.repository.theme.ThemeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@HiltViewModel
class ThemeFormViewModel
@Inject
constructor(private val repo: ThemeRepository, private val session: SessionRepository) :
    ViewModel() {
    private val _state = MutableStateFlow(ThemeFormUiState())
    val state: StateFlow<ThemeFormUiState> = _state.asStateFlow()
    private var unitId = ""
    private var themeId: String? = null

    fun onEvent(e: ThemeFormUiEvent) {
        when (e) {
            is ThemeFormUiEvent.Load -> load(e.unitId, e.themeId)
            is ThemeFormUiEvent.TitleChanged ->
                _state.update { it.copy(title = e.value, fieldErrors = emptyMap()) }
            is ThemeFormUiEvent.ContentChanged -> _state.update { it.copy(contentText = e.value) }
            is ThemeFormUiEvent.OrderChanged -> _state.update { it.copy(orderIndex = e.value) }
            ThemeFormUiEvent.Save -> save()
            ThemeFormUiEvent.ClearError -> _state.update { it.copy(errorMessage = null) }
        }
    }

    private fun load(unit: String, id: String?) =
        viewModelScope.launch {
            if (admin() != true) return@launch
            unitId = unit
            themeId = id
            _state.update { it.copy(isEditMode = id != null, isLoading = id != null) }
            if (id != null)
                repo.getThemesByUnit(unit).collect { r ->
                    when (r) {
                        is Resource.Success ->
                            r.data
                                ?.firstOrNull { it.id == id }
                                ?.let { t ->
                                    _state.update { s ->
                                        s.copy(
                                            isLoading = false,
                                            title = t.title,
                                            contentText = t.contentText,
                                            orderIndex = t.orderIndex.toString(),
                                        )
                                    }
                                }
                        is Resource.Error ->
                            _state.update {
                                it.copy(
                                    isLoading = false,
                                    errorMessage = r.message ?: "No se pudo cargar el tema.",
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
                            mapOf(
                                    "title" to
                                        if (s.title.isBlank()) "El título es obligatorio." else "",
                                    "order" to
                                        if (order == null) "El orden debe ser un entero." else "",
                                )
                                .filterValues { it.isNotBlank() }
                    )
                }
                return@launch
            }
            val a = session.session.first()!!.studentId
            _state.update { it.copy(isSaving = true) }
            val flow =
                if (themeId == null)
                    repo.createTheme(
                        a,
                        unitId,
                        CreateThemeDto(s.title.trim(), s.contentText, order),
                    )
                else
                    repo.updateTheme(
                        a,
                        themeId!!,
                        UpdateThemeDto(s.title.trim(), s.contentText, order),
                    )
            flow.collect { r ->
                when (r) {
                    is Resource.Success ->
                        _state.update { it.copy(isSaving = false, isSaved = true) }
                    is Resource.Error ->
                        _state.update {
                            it.copy(
                                isSaving = false,
                                errorMessage = r.message ?: "No se pudo guardar el tema.",
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
