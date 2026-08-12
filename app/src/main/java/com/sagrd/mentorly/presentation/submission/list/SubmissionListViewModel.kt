package com.sagrd.mentorly.presentation.submission.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sagrd.mentorly.data.remote.Resource
import com.sagrd.mentorly.domain.repository.session.SessionRepository
import com.sagrd.mentorly.domain.repository.submission.SubmissionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SubmissionListViewModel @Inject constructor(
    private val submissionRepository: SubmissionRepository,
    private val sessionRepository: SessionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SubmissionListUiState())
    val uiState: StateFlow<SubmissionListUiState> = _uiState.asStateFlow()

    init {
        onEvent(SubmissionListUiEvent.Load)
    }

    fun onEvent(event: SubmissionListUiEvent) {
        when (event) {
            is SubmissionListUiEvent.Load -> load()
            is SubmissionListUiEvent.Refresh -> load()
            is SubmissionListUiEvent.SubmissionClicked -> Unit
            is SubmissionListUiEvent.DismissError -> _uiState.update { it.copy(errorMessage = null) }
        }
    }

    private fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val session = sessionRepository.session.first()
            val studentId = session?.studentId

            if (studentId == null) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Sesión no encontrada") }
                return@launch
            }

            submissionRepository.getSubmissionsByStudentId(studentId).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _uiState.update { it.copy(isLoading = true) }
                    is Resource.Success -> _uiState.update {
                        it.copy(isLoading = false, submissions = resource.data ?: emptyList())
                    }
                    is Resource.Error -> _uiState.update {
                        it.copy(isLoading = false, errorMessage = resource.message)
                    }
                }
            }
        }
    }
}