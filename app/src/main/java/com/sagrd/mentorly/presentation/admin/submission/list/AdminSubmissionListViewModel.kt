package com.sagrd.mentorly.presentation.admin.submission.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sagrd.mentorly.data.remote.Resource
import com.sagrd.mentorly.domain.model.student.StudentRole
import com.sagrd.mentorly.domain.model.submission.AdminEscalatedSubmission
import com.sagrd.mentorly.domain.repository.session.SessionRepository
import com.sagrd.mentorly.domain.repository.submission.SubmissionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminSubmissionListViewModel @Inject constructor(
    private val submissionRepository: SubmissionRepository,
    private val sessionRepository: SessionRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminSubmissionListUiState())
    val uiState: StateFlow<AdminSubmissionListUiState> = _uiState.asStateFlow()

    init {
        checkSessionAndLoad()
    }

    private fun checkSessionAndLoad() {
        viewModelScope.launch {
            sessionRepository.session.firstOrNull()?.let { session ->
                if (session.role == StudentRole.ADMIN) {
                    _uiState.update { it.copy(
                        hasSession = true,
                        hasAdminAccess = true
                    ) }
                    loadSubmissions()
                } else {
                    _uiState.update { it.copy(
                        hasAdminAccess = false,
                        errorMessage = "No tienes permisos para administrar entregas escaladas."
                    ) }
                }
            } ?: run {
                _uiState.update { it.copy(
                    hasSession = false,
                    errorMessage = "No se encontró una sesión activa."
                ) }
            }
        }
    }

    fun onEvent(event: AdminSubmissionListUiEvent) {
        when (event) {
            AdminSubmissionListUiEvent.Load -> loadSubmissions()
            AdminSubmissionListUiEvent.Refresh -> loadSubmissions(isRefreshing = true)
            is AdminSubmissionListUiEvent.SearchChanged -> {
                _uiState.update { it.copy(searchQuery = event.value) }
            }
            is AdminSubmissionListUiEvent.FilterChanged -> {
                _uiState.update { it.copy(selectedFilter = event.filter) }
            }
            AdminSubmissionListUiEvent.ClearError -> {
                _uiState.update { it.copy(errorMessage = null) }
            }
        }
    }

    private fun loadSubmissions(isRefreshing: Boolean = false) {
        viewModelScope.launch {
            val session = sessionRepository.session.firstOrNull()
            val adminId = session?.studentId ?: return@launch

            submissionRepository.getEscalatedSubmissions(adminId).collect { result ->
                when (result) {
                    is Resource.Loading<*> -> {
                        if (isRefreshing) _uiState.update { it.copy(isRefreshing = true) }
                        else _uiState.update { it.copy(isLoading = true) }
                    }
                    is Resource.Success -> {
                        _uiState.update { it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            submissions = result.data ?: emptyList(),
                            errorMessage = null
                        ) }
                    }
                    is Resource.Error -> {
                        _uiState.update { it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            errorMessage = result.message ?: "No se pudieron cargar las entregas escaladas."
                        ) }
                    }
                }
            }
        }
    }

    val filteredSubmissions = combine(_uiState, _uiState.map { it.submissions }) { state, submissions ->
        submissions.filter { submission ->
            val matchesSearch = state.searchQuery.isBlank() ||
                submission.authorDisplayName.contains(state.searchQuery, ignoreCase = true) ||
                submission.courseTitle.contains(state.searchQuery, ignoreCase = true) ||
                submission.activityTitle.contains(state.searchQuery, ignoreCase = true)
            
            matchesSearch
        }.let { filtered ->
            when (state.selectedFilter) {
                EscalatedSubmissionFilter.All -> filtered
                EscalatedSubmissionFilter.MostApproved -> filtered.sortedByDescending { it.positiveReviews }
                EscalatedSubmissionFilter.MostRejected -> filtered.sortedByDescending { it.rejectedReviews }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
