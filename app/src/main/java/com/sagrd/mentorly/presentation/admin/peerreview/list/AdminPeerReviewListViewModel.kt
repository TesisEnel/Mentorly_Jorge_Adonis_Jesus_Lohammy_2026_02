package com.sagrd.mentorly.presentation.admin.peerreview.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sagrd.mentorly.data.remote.Resource
import com.sagrd.mentorly.domain.model.student.StudentRole
import com.sagrd.mentorly.domain.repository.peerreview.PeerReviewRepository
import com.sagrd.mentorly.domain.repository.session.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminPeerReviewListViewModel @Inject constructor(
    private val peerReviewRepository: PeerReviewRepository,
    private val sessionRepository: SessionRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(AdminPeerReviewListUiState())
    val state: StateFlow<AdminPeerReviewListUiState> = _state.asStateFlow()

    init {
        checkSessionAndLoad()
    }

    private fun checkSessionAndLoad() {
        viewModelScope.launch {
            val session = sessionRepository.session.firstOrNull()
            if (session != null) {
                if (session.role == StudentRole.ADMIN) {
                    _state.update { it.copy(
                        hasSession = true,
                        hasAdminAccess = true
                    ) }
                    loadPeerReviews()
                } else {
                    _state.update { it.copy(
                        hasAdminAccess = false,
                        errorMessage = "No tienes permisos para consultar revisiones administrativas."
                    ) }
                }
            } else {
                _state.update { it.copy(
                    hasSession = false,
                    errorMessage = "No se encontró una sesión activa."
                ) }
            }
        }
    }

    fun onEvent(event: AdminPeerReviewListUiEvent) {
        when (event) {
            AdminPeerReviewListUiEvent.Load -> loadPeerReviews()
            AdminPeerReviewListUiEvent.Refresh -> loadPeerReviews(isRefreshing = true)
            is AdminPeerReviewListUiEvent.SearchChanged -> {
                _state.update { it.copy(searchQuery = event.value) }
            }
            is AdminPeerReviewListUiEvent.FilterChanged -> {
                _state.update { it.copy(selectedFilter = event.filter) }
            }
            AdminPeerReviewListUiEvent.ClearError -> {
                _state.update { it.copy(errorMessage = null) }
            }
        }
    }

    private fun loadPeerReviews(isRefreshing: Boolean = false) {
        viewModelScope.launch {
            val session = sessionRepository.session.firstOrNull()
            if (session != null) {
                val adminId = session.studentId

                peerReviewRepository.getAllPeerReviews(adminId).collect { result ->
                    when (result) {
                        is Resource.Loading<*> -> {
                            if (isRefreshing) _state.update { it.copy(isRefreshing = true) }
                            else _state.update { it.copy(isLoading = true) }
                        }
                        is Resource.Success -> {
                            _state.update { it.copy(
                                isLoading = false,
                                isRefreshing = false,
                                peerReviews = result.data ?: emptyList(),
                                errorMessage = null
                            ) }
                        }
                        is Resource.Error -> {
                            _state.update { it.copy(
                                isLoading = false,
                                isRefreshing = false,
                                errorMessage = result.message ?: "No se pudieron cargar las revisiones."
                            ) }
                        }
                    }
                }
            }
        }
    }

    val filteredPeerReviews = combine(_state, _state.map { it.peerReviews }) { state, reviews ->
        reviews.filter { review ->
            val matchesSearch = state.searchQuery.isBlank() ||
                review.feedbackComment.contains(state.searchQuery, ignoreCase = true) ||
                review.id.contains(state.searchQuery, ignoreCase = true)

            val matchesFilter = when (state.selectedFilter) {
                PeerReviewFilter.All -> true
                PeerReviewFilter.Approved -> review.isApproved
                PeerReviewFilter.Rejected -> !review.isApproved
            }

            matchesSearch && matchesFilter
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
