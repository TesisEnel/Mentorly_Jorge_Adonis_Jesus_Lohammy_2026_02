package com.sagrd.mentorly.presentation.peerreview.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sagrd.mentorly.data.remote.Resource
import com.sagrd.mentorly.domain.repository.peerreview.PeerReviewRepository
import com.sagrd.mentorly.domain.repository.session.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PeerReviewHistoryViewModel @Inject constructor(
    private val peerReviewRepository: PeerReviewRepository,
    private val sessionRepository: SessionRepository
) : ViewModel() {

    private val _state = MutableStateFlow(PeerReviewHistoryUiState())
    val state: StateFlow<PeerReviewHistoryUiState> = _state.asStateFlow()

    init {
        loadHistory()
    }

    fun onEvent(event: PeerReviewHistoryUiEvent) {
        when (event) {
            PeerReviewHistoryUiEvent.Refresh -> loadHistory(isRefresh = true)
            PeerReviewHistoryUiEvent.ClearError -> _state.update { it.copy(errorMessage = null) }
        }
    }

    private fun loadHistory(isRefresh: Boolean = false) {
        if (_state.value.isLoading || _state.value.isRefreshing) return

        viewModelScope.launch {
            val session = sessionRepository.session.first()
            if (session == null) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        hasSession = false,
                        errorMessage = "No se encontró una sesión activa."
                    )
                }
            } else {
                peerReviewRepository.getMyReviews(session.studentId).collect { resource ->
                    when (resource) {
                        is Resource.Loading -> _state.update {
                            it.copy(
                                isLoading = !isRefresh,
                                isRefreshing = isRefresh,
                                errorMessage = null,
                                hasSession = true
                            )
                        }

                        is Resource.Success -> _state.update {
                            it.copy(
                                isLoading = false,
                                isRefreshing = false,
                                reviews = resource.data.orEmpty(),
                                errorMessage = null
                            )
                        }

                        is Resource.Error -> _state.update {
                            it.copy(
                                isLoading = false,
                                isRefreshing = false,
                                errorMessage = resource.message
                                    ?: "No se pudieron cargar tus revisiones."
                            )
                        }
                    }
                }
            }
        }
    }
}
