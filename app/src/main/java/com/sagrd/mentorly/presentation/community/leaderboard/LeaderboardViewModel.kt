package com.sagrd.mentorly.presentation.community.leaderboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sagrd.mentorly.data.remote.Resource
import com.sagrd.mentorly.domain.repository.community.CourseCommunityRepository
import com.sagrd.mentorly.domain.repository.session.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LeaderboardViewModel @Inject constructor(
    private val communityRepository: CourseCommunityRepository,
    private val sessionRepository: SessionRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(LeaderboardUiState())
    val state: StateFlow<LeaderboardUiState> = _state.asStateFlow()

    private var courseId: String = ""

    fun setCourseId(id: String) {
        if (courseId != id) {
            courseId = id
            onEvent(LeaderboardUiEvent.Load)
        }
    }

    fun onEvent(event: LeaderboardUiEvent) {
        when (event) {
            LeaderboardUiEvent.Load -> loadData()
            LeaderboardUiEvent.Refresh -> loadData(isRefreshing = true)
            is LeaderboardUiEvent.SearchChanged -> {
                _state.update { it.copy(searchQuery = event.value) }
            }
            LeaderboardUiEvent.ClearError -> {
                _state.update { it.copy(errorMessage = null) }
            }
        }
    }

    private fun loadData(isRefreshing: Boolean = false) {
        if (courseId.isBlank()) return

        viewModelScope.launch {
            val session = sessionRepository.session.firstOrNull()
            if (session == null) {
                _state.update { it.copy(hasSession = false, errorMessage = "No se encontró una sesión activa.") }
                return@launch
            }

            val viewerId = session.studentId

            // Leaderboard
            launch {
                communityRepository.getLeaderboard(courseId, viewerId).collect { result ->
                    when (result) {
                        is Resource.Loading -> {
                            if (isRefreshing) {
                                _state.update { it.copy(isRefreshing = true) }
                            } else {
                                _state.update { it.copy(isLoading = true) }
                            }
                        }
                        is Resource.Success -> {
                            _state.update {
                                it.copy(
                                    isLoading = false,
                                    isRefreshing = false,
                                    entries = result.data ?: emptyList(),
                                    errorMessage = null
                                )
                            }
                        }
                        is Resource.Error -> {
                            _state.update {
                                it.copy(
                                    isLoading = false,
                                    isRefreshing = false,
                                    errorMessage = result.message ?: "No se pudo cargar el ranking del curso."
                                )
                            }
                        }
                    }
                }
            }

            // Own Position
            launch {
                communityRepository.getLeaderboardEntry(courseId, viewerId).collect { result ->
                    if (result is Resource.Success) {
                        _state.update { it.copy(ownPosition = result.data) }
                    }
                }
            }
        }
    }

    val filteredEntries = state.map { state ->
        if (state.searchQuery.isBlank()) {
            state.entries
        } else {
            state.entries.filter { it.displayName.contains(state.searchQuery, ignoreCase = true) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
