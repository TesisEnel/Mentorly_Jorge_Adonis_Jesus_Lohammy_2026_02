package com.sagrd.mentorly.presentation.theme.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sagrd.mentorly.data.remote.Resource
import com.sagrd.mentorly.domain.repository.progress.EnrollmentProgressRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ThemeDetailViewModel @Inject constructor(
    private val enrollmentProgressRepository: EnrollmentProgressRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ThemeDetailUiState())
    val state: StateFlow<ThemeDetailUiState> = _state.asStateFlow()

    private var enrollmentId: String? = null
    private var themeId: String? = null

    fun onEvent(event: ThemeDetailUiEvent) {
        when (event) {
            is ThemeDetailUiEvent.LoadTheme -> {
                enrollmentId = event.enrollmentId
                themeId = event.themeId
                loadThemeContent(event.enrollmentId, event.themeId)
            }

            ThemeDetailUiEvent.CompleteTheme -> completeCurrentTheme()
            ThemeDetailUiEvent.ClearError -> _state.update { it.copy(errorMessage = null) }
        }
    }

    private fun loadThemeContent(enrollmentId: String, targetThemeId: String) {
        viewModelScope.launch {
            enrollmentProgressRepository.getEnrollmentProgress(enrollmentId).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _state.update {
                            it.copy(
                                isLoading = it.theme == null,
                                errorMessage = null
                            )
                        }
                    }

                    is Resource.Success -> {
                        val progress = resource.data
                        var foundUnitTitle: String? = null
                        var foundUnitIndex = 1
                        var foundTheme = progress?.units?.firstNotNullOfOrNull { unit ->
                            val theme = unit.themes.firstOrNull { it.themeId == targetThemeId }
                            if (theme != null) {
                                foundUnitTitle = unit.title
                                foundUnitIndex = progress.units.indexOf(unit) + 1
                                theme
                            } else {
                                null
                            }
                        }

                        if (foundTheme != null) {
                            _state.update {
                                it.copy(
                                    isLoading = false,
                                    unitTitle = foundUnitTitle,
                                    unitOrderIndex = foundUnitIndex,
                                    theme = foundTheme,
                                    isCompleted = foundTheme.isCompleted,
                                    errorMessage = null
                                )
                            }
                        } else {
                            _state.update {
                                it.copy(
                                    isLoading = false,
                                    errorMessage = "No se encontró el tema solicitado."
                                )
                            }
                        }
                    }

                    is Resource.Error -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = resource.message ?: "No se pudo cargar el tema."
                            )
                        }
                    }
                }
            }
        }
    }

    private fun completeCurrentTheme() {
        val currentEnrollmentId = enrollmentId ?: return
        val currentThemeId = themeId ?: return
        if (_state.value.isCompleting || _state.value.isCompleted) return

        viewModelScope.launch {
            enrollmentProgressRepository.completeTheme(currentEnrollmentId, currentThemeId).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _state.update {
                            it.copy(
                                isCompleting = true,
                                errorMessage = null
                            )
                        }
                    }

                    is Resource.Success -> {
                        val progress = resource.data
                        val updatedTheme = progress?.units?.flatMap { it.themes }?.firstOrNull { it.themeId == currentThemeId }
                        _state.update {
                            it.copy(
                                isCompleting = false,
                                isCompleted = true,
                                theme = updatedTheme ?: it.theme?.copy(isCompleted = true),
                                errorMessage = null
                            )
                        }
                    }

                    is Resource.Error -> {
                        _state.update {
                            it.copy(
                                isCompleting = false,
                                errorMessage = resource.message ?: "No se pudo marcar el tema como completado."
                            )
                        }
                    }
                }
            }
        }
    }
}
