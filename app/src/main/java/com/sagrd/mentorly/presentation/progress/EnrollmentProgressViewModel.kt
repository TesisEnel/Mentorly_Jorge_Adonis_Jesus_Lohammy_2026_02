package com.sagrd.mentorly.presentation.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sagrd.mentorly.data.remote.Resource
import com.sagrd.mentorly.domain.repository.progress.EnrollmentProgressRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EnrollmentProgressViewModel @Inject constructor(
    private val enrollmentProgressRepository: EnrollmentProgressRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EnrollmentProgressUiState())
    val uiState: StateFlow<EnrollmentProgressUiState> = _uiState.asStateFlow()

    private var enrollmentId: String? = null

    fun initialize(id: String) {
        if (enrollmentId == id && (_uiState.value.progress != null || _uiState.value.isLoading)) return

        enrollmentId = id
        loadProgress()
    }

    fun onEvent(event: EnrollmentProgressUiEvent) {
        when (event) {
            EnrollmentProgressUiEvent.Refresh -> loadProgress(isRefresh = true)
            EnrollmentProgressUiEvent.ClearError -> _uiState.update { it.copy(errorMessage = null) }
        }
    }

    private fun loadProgress(isRefresh: Boolean = false) {
        val id = enrollmentId ?: return
        if (_uiState.value.isLoading || _uiState.value.isRefreshing) return

        viewModelScope.launch {
            enrollmentProgressRepository.getEnrollmentProgress(id).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _uiState.update {
                        it.copy(
                            isLoading = !isRefresh,
                            isRefreshing = isRefresh,
                            errorMessage = null
                        )
                    }

                    is Resource.Success -> _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            progress = resource.data,
                            errorMessage = null
                        )
                    }

                    is Resource.Error -> _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            errorMessage = resource.message ?: "No se pudo cargar el progreso."
                        )
                    }
                }
            }
        }
    }
}
