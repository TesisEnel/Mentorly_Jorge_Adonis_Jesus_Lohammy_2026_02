package com.sagrd.mentorly.presentation.peerreview.queue

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
class PeerReviewQueueViewModel @Inject constructor(
    private val peerReviewRepository: PeerReviewRepository,
    private val sessionRepository: SessionRepository
) : ViewModel() {

    private val _state = MutableStateFlow(PeerReviewQueueUiState())
    val state: StateFlow<PeerReviewQueueUiState> = _state.asStateFlow()

    init {
        loadQueue()
    }

    fun onEvent(event: PeerReviewQueueUiEvent) {
        when (event) {
            PeerReviewQueueUiEvent.Refresh -> loadQueue(isRefresh = true)
            PeerReviewQueueUiEvent.ClearError -> _state.update { it.copy(errorMessage = null) }
        }
    }

    private fun loadQueue(isRefresh: Boolean = false) {
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
                peerReviewRepository.getQueue(session.studentId).collect { resource ->
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
                                queueItems = resource.data.orEmpty(),
                                errorMessage = null
                            )
                        }

                        is Resource.Error -> _state.update {
                            it.copy(
                                isLoading = false,
                                isRefreshing = false,
                                errorMessage = resource.message
                                    ?: "No se pudo cargar la cola de revisiones."
                            )
                        }
                    }
                }
            }
        }
    }
}
