package com.sagrd.mentorly.presentation.admin.content

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sagrd.mentorly.data.remote.Resource
import com.sagrd.mentorly.data.remote.dto.common.ReorderItemsDto
import com.sagrd.mentorly.domain.model.student.StudentRole
import com.sagrd.mentorly.domain.repository.activity.ActivityRepository
import com.sagrd.mentorly.domain.repository.course.CourseRepository
import com.sagrd.mentorly.domain.repository.session.SessionRepository
import com.sagrd.mentorly.domain.repository.theme.ThemeRepository
import com.sagrd.mentorly.domain.repository.unit.UnitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@HiltViewModel
class ContentManagementViewModel
@Inject
constructor(
    private val courseRepository: CourseRepository,
    private val unitRepository: UnitRepository,
    private val themeRepository: ThemeRepository,
    private val activityRepository: ActivityRepository,
    private val sessionRepository: SessionRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ContentManagementUiState())
    val uiState: StateFlow<ContentManagementUiState> = _uiState.asStateFlow()
    private var courseId = ""

    fun onEvent(e: ContentManagementUiEvent) {
        when (e) {
            ContentManagementUiEvent.Load -> load(false)
            ContentManagementUiEvent.Refresh -> load(true)
            is ContentManagementUiEvent.DeleteUnit ->
                write(e.id) { a -> unitRepository.deleteUnit(a, e.id) }
            is ContentManagementUiEvent.DeleteTheme ->
                write(e.id) { a -> themeRepository.deleteTheme(a, e.id) }
            is ContentManagementUiEvent.DeleteActivity ->
                write(e.id) { a -> activityRepository.deleteActivity(a, e.id) }
            is ContentManagementUiEvent.MoveUnit -> moveUnit(e)
            is ContentManagementUiEvent.MoveTheme -> moveTheme(e)
            is ContentManagementUiEvent.MoveActivity -> moveActivity(e)
            ContentManagementUiEvent.ClearError -> _uiState.update { it.copy(errorMessage = null) }
        }
    }

    fun setCourseId(id: String) {
        if (courseId != id) {
            courseId = id
            load(false)
        }
    }

    private fun load(refresh: Boolean) =
        viewModelScope.launch {
            if (admin() == null) return@launch
            _uiState.update {
                it.copy(
                    isLoading = !refresh && it.courseContent == null,
                    isRefreshing = refresh,
                    errorMessage = null,
                )
            }
            courseRepository.getCourseContent(courseId).collect { r ->
                when (r) {
                    is Resource.Loading -> Unit
                    is Resource.Success ->
                        _uiState.update {
                            it.copy(isLoading = false, isRefreshing = false, courseContent = r.data)
                        }
                    is Resource.Error ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isRefreshing = false,
                                errorMessage =
                                    r.message ?: "No se pudo cargar el contenido del curso.",
                            )
                        }
                }
            }
        }

    private fun write(id: String, block: (String) -> Flow<Resource<Unit>>) =
        viewModelScope.launch {
            val a = admin() ?: return@launch
            _uiState.update { it.copy(deletingItemId = id) }
            block(a).collect { r ->
                when (r) {
                    is Resource.Success -> {
                        _uiState.update { it.copy(deletingItemId = null) }
                        load(true)
                    }
                    is Resource.Error ->
                        _uiState.update {
                            it.copy(
                                deletingItemId = null,
                                errorMessage = r.message ?: "No se pudo eliminar el elemento.",
                            )
                        }
                    is Resource.Loading -> Unit
                }
            }
        }

    private fun moveUnit(e: ContentManagementUiEvent.MoveUnit) {
        val list = _uiState.value.courseContent?.units.orEmpty()
        move(list.map { it.id }, e.id, e.up) { ids, a ->
            unitRepository.reorderUnits(a, courseId, ReorderItemsDto(ids))
        }
    }

    private fun moveTheme(e: ContentManagementUiEvent.MoveTheme) {
        val list =
            _uiState.value.courseContent?.units?.firstOrNull { it.id == e.unitId }?.themes.orEmpty()
        move(list.map { it.id }, e.id, e.up) { ids, a ->
            themeRepository.reorderThemes(a, e.unitId, ReorderItemsDto(ids))
        }
    }

    private fun moveActivity(e: ContentManagementUiEvent.MoveActivity) {
        val list =
            _uiState.value.courseContent
                ?.units
                .orEmpty()
                .flatMap { it.themes }
                .firstOrNull { it.id == e.themeId }
                ?.activities
                .orEmpty()
        move(list.map { it.id }, e.id, e.up) { ids, a ->
            activityRepository.reorderActivities(a, e.themeId, ReorderItemsDto(ids))
        }
    }

    private fun move(
        ids: List<String>,
        id: String,
        up: Boolean,
        block: (List<String>, String) -> Flow<Resource<Unit>>,
    ) =
        viewModelScope.launch {
            val index = ids.indexOf(id)
            val target = if (up) index - 1 else index + 1
            if (index < 0 || target !in ids.indices) return@launch
            val a = admin() ?: return@launch
            val ordered = ids.toMutableList().also { java.util.Collections.swap(it, index, target) }
            _uiState.update { it.copy(reorderingItemId = id) }
            block(ordered, a).collect { r ->
                when (r) {
                    is Resource.Success -> {
                        _uiState.update { it.copy(reorderingItemId = null) }
                        load(true)
                    }
                    is Resource.Error ->
                        _uiState.update {
                            it.copy(
                                reorderingItemId = null,
                                errorMessage = r.message ?: "No se pudo cambiar el orden.",
                            )
                        }
                    is Resource.Loading -> Unit
                }
            }
        }

    private suspend fun admin(): String? =
        sessionRepository.session
            .first()
            ?.takeIf { it.role == StudentRole.ADMIN }
            ?.studentId
            .also {
                if (it == null)
                    _uiState.update { s ->
                        s.copy(
                            isLoading = false,
                            hasAdminAccess = false,
                            errorMessage =
                                "No tienes permisos para administrar el contenido del curso.",
                        )
                    }
            }
}
