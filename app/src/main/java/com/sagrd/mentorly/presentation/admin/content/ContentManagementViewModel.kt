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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ContentManagementViewModel @Inject constructor(
    private val courseRepository: CourseRepository,
    private val unitRepository: UnitRepository,
    private val themeRepository: ThemeRepository,
    private val activityRepository: ActivityRepository,
    private val sessionRepository: SessionRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ContentManagementUiState())
    val state: StateFlow<ContentManagementUiState> = _state.asStateFlow()

    private var courseId = ""

    fun onEvent(event: ContentManagementUiEvent) {
        when (event) {
            ContentManagementUiEvent.Load -> load(isRefresh = false)
            ContentManagementUiEvent.Refresh -> load(isRefresh = true)
            is ContentManagementUiEvent.DeleteUnit -> {
                deleteItem(event.id) { adminId -> unitRepository.deleteUnit(adminId, event.id) }
            }
            is ContentManagementUiEvent.DeleteTheme -> {
                deleteItem(event.id) { adminId -> themeRepository.deleteTheme(adminId, event.id) }
            }
            is ContentManagementUiEvent.DeleteActivity -> {
                deleteItem(event.id) { adminId -> activityRepository.deleteActivity(adminId, event.id) }
            }
            is ContentManagementUiEvent.MoveUnit -> moveUnit(event)
            is ContentManagementUiEvent.MoveTheme -> moveTheme(event)
            is ContentManagementUiEvent.MoveActivity -> moveActivity(event)
            ContentManagementUiEvent.ClearError -> _state.update { it.copy(errorMessage = null) }
        }
    }

    fun setCourseId(id: String) {
        if (courseId != id) {
            courseId = id
            load(isRefresh = false)
        }
    }

    private fun load(isRefresh: Boolean) {
        viewModelScope.launch {
            val session = sessionRepository.session.first()

            if (session == null) {
                updateMissingSession()
            } else if (session.role != StudentRole.ADMIN) {
                updateMissingAdminAccess()
            } else {
                _state.update {
                    it.copy(
                        isLoading = !isRefresh && it.courseContent == null,
                        isRefreshing = isRefresh,
                        hasSession = true,
                        hasAdminAccess = true,
                        errorMessage = null,
                    )
                }

                courseRepository.getCourseContent(courseId).collect { resource ->
                    when (resource) {
                        is Resource.Loading -> Unit
                        is Resource.Success -> _state.update {
                            it.copy(
                                isLoading = false,
                                isRefreshing = false,
                                courseContent = resource.data,
                            )
                        }
                        is Resource.Error -> _state.update {
                            it.copy(
                                isLoading = false,
                                isRefreshing = false,
                                errorMessage =
                                    resource.message ?: "No se pudo cargar el contenido del curso.",
                            )
                        }
                    }
                }
            }
        }
    }

    private fun deleteItem(id: String, action: (String) -> Flow<Resource<Unit>>) {
        viewModelScope.launch {
            val session = sessionRepository.session.first()

            if (session == null) {
                updateMissingSession()
            } else if (session.role != StudentRole.ADMIN) {
                updateMissingAdminAccess()
            } else if (_state.value.deletingItemId == null) {
                _state.update { it.copy(deletingItemId = id, errorMessage = null) }
                action(session.studentId).collect { resource ->
                    when (resource) {
                        is Resource.Loading -> Unit
                        is Resource.Success -> {
                            _state.update { it.copy(deletingItemId = null) }
                            load(isRefresh = true)
                        }
                        is Resource.Error -> _state.update {
                            it.copy(
                                deletingItemId = null,
                                errorMessage = resource.message ?: "No se pudo eliminar el elemento.",
                            )
                        }
                    }
                }
            }
        }
    }

    private fun moveUnit(event: ContentManagementUiEvent.MoveUnit) {
        val ids = _state.value.courseContent?.units.orEmpty().map { it.id }
        move(ids, event.id, event.up) { orderedIds, adminId ->
            unitRepository.reorderUnits(adminId, courseId, ReorderItemsDto(orderedIds))
        }
    }

    private fun moveTheme(event: ContentManagementUiEvent.MoveTheme) {
        val ids =
            _state.value.courseContent
                ?.units
                ?.firstOrNull { it.id == event.unitId }
                ?.themes
                .orEmpty()
                .map { it.id }
        move(ids, event.id, event.up) { orderedIds, adminId ->
            themeRepository.reorderThemes(adminId, event.unitId, ReorderItemsDto(orderedIds))
        }
    }

    private fun moveActivity(event: ContentManagementUiEvent.MoveActivity) {
        val ids =
            _state.value.courseContent
                ?.units
                .orEmpty()
                .flatMap { it.themes }
                .firstOrNull { it.id == event.themeId }
                ?.activities
                .orEmpty()
                .map { it.id }
        move(ids, event.id, event.up) { orderedIds, adminId ->
            activityRepository.reorderActivities(adminId, event.themeId, ReorderItemsDto(orderedIds))
        }
    }

    private fun move(
        ids: List<String>,
        id: String,
        up: Boolean,
        action: (List<String>, String) -> Flow<Resource<Unit>>,
    ) {
        val currentIndex = ids.indexOf(id)
        val targetIndex = if (up) currentIndex - 1 else currentIndex + 1

        if (currentIndex in ids.indices && targetIndex in ids.indices) {
            viewModelScope.launch {
                val session = sessionRepository.session.first()

                if (session == null) {
                    updateMissingSession()
                } else if (session.role != StudentRole.ADMIN) {
                    updateMissingAdminAccess()
                } else {
                    val orderedIds = ids.toMutableList().also {
                        java.util.Collections.swap(it, currentIndex, targetIndex)
                    }
                    _state.update { it.copy(reorderingItemId = id) }
                    action(orderedIds, session.studentId).collect { resource ->
                        when (resource) {
                            is Resource.Loading -> Unit
                            is Resource.Success -> {
                                _state.update { it.copy(reorderingItemId = null) }
                                load(isRefresh = true)
                            }
                            is Resource.Error -> _state.update {
                                it.copy(
                                    reorderingItemId = null,
                                    errorMessage = resource.message ?: "No se pudo cambiar el orden.",
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun updateMissingSession() {
        _state.update {
            it.copy(
                isLoading = false,
                isRefreshing = false,
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
                isRefreshing = false,
                hasSession = true,
                hasAdminAccess = false,
                errorMessage = "No tienes permisos para administrar el contenido del curso.",
            )
        }
    }
}
