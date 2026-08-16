package com.sagrd.mentorly.data.repository.session

import com.sagrd.mentorly.domain.model.session.AppSession
import com.sagrd.mentorly.domain.repository.session.SessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class SessionRepositoryImpl @Inject constructor() : SessionRepository {

    private val _session = MutableStateFlow<AppSession?>(null)

    override val session: StateFlow<AppSession?> = _session.asStateFlow()

    override suspend fun saveSession(session: AppSession) {
        _session.value = session
    }

    override suspend fun clearSession() {
        _session.value = null
    }
}
