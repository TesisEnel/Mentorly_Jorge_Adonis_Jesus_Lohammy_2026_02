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

    private val _uiState = MutableStateFlow(PeerReviewQueueUiState())
    val uiState: StateFlow<PeerReviewQueueUiState> = _uiState.asStateFlow()

    init {
        loadQueue()
    }

    fun onEvent(event: PeerReviewQueueUiEvent) {
        when (event) {
            PeerReviewQueueUiEvent.Refresh -> loadQueue(isRefresh = true)
            PeerReviewQueueUiEvent.ClearError -> _uiState.update { it.copy(errorMessage = null) }
        }
    }

    private fun loadQueue(isRefresh: Boolean = false) {
        if (_uiState.value.isLoading || _uiState.value.isRefreshing) return

        viewModelScope.launch {
            val session = sessionRepository.session.first()
            if (session == null) {
                _uiState.update {
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
                        is Resource.Loading -> _uiState.update {
                            it.copy(
                                isLoading = !isRefresh,
                                isRefreshing = isRefresh,
                                errorMessage = null,
                                hasSession = true
                            )
                        }

                        is Resource.Success -> _uiState.update {
                            it.copy(
                                isLoading = false,
                                isRefreshing = false,
                                queueItems = resource.data.orEmpty(),
                                errorMessage = null
                            )
                        }

                        is Resource.Error -> _uiState.update {
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
