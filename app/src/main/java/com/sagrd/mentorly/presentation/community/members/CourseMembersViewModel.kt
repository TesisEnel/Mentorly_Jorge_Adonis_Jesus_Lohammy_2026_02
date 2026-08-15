package com.sagrd.mentorly.presentation.community.members

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sagrd.mentorly.data.remote.Resource
import com.sagrd.mentorly.domain.model.community.CourseMember
import com.sagrd.mentorly.domain.repository.community.CourseCommunityRepository
import com.sagrd.mentorly.domain.repository.session.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CourseMembersViewModel @Inject constructor(
    private val communityRepository: CourseCommunityRepository,
    private val sessionRepository: SessionRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(CourseMembersUiState())
    val state: StateFlow<CourseMembersUiState> = _state.asStateFlow()

    private var courseId: String = ""

    fun setCourseId(id: String) {
        if (courseId != id) {
            courseId = id
            onEvent(CourseMembersUiEvent.Load)
        }
    }

    fun onEvent(event: CourseMembersUiEvent) {
        when (event) {
            CourseMembersUiEvent.Load -> loadMembers()
            CourseMembersUiEvent.Refresh -> loadMembers(isRefreshing = true)
            is CourseMembersUiEvent.SearchChanged -> {
                _state.update { it.copy(searchQuery = event.value) }
            }
            CourseMembersUiEvent.ClearError -> {
                _state.update { it.copy(errorMessage = null) }
            }
        }
    }

    private fun loadMembers(isRefreshing: Boolean = false) {
        if (courseId.isBlank()) return

        viewModelScope.launch {
            val session = sessionRepository.session.firstOrNull()
            if (session == null) {
                _state.update {
                    it.copy(
                        hasSession = false,
                        errorMessage = "No se encontró una sesión activa."
                    )
                }
                return@launch
            }

            communityRepository.getCourseMembers(courseId, session.studentId).collect { result ->
                when (result) {
                    is Resource.Loading<*> -> {
                        _state.update {
                            if (isRefreshing) it.copy(isRefreshing = true)
                            else it.copy(isLoading = true)
                        }
                    }

                    is Resource.Success -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                isRefreshing = false,
                                members = result.data ?: emptyList(),
                                errorMessage = null
                            )
                        }
                    }

                    is Resource.Error -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                isRefreshing = false,
                                errorMessage = result.message
                                    ?: "No se pudieron cargar los compañeros del curso."
                            )
                        }
                    }
                }
            }
        }
    }

    val filteredMembers = state.map { state ->
        if (state.searchQuery.isBlank()) {
            state.members
        } else {
            state.members.filter { it.displayName.contains(state.searchQuery, ignoreCase = true) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
