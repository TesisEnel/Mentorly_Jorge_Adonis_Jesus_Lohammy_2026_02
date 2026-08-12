package com.sagrd.mentorly.data.repository.session

import com.sagrd.mentorly.data.local.session.SessionPreferences
import com.sagrd.mentorly.domain.model.session.AppSession
import com.sagrd.mentorly.domain.repository.session.SessionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SessionRepositoryImpl @Inject constructor(
    private val sessionPreferences: SessionPreferences
) : SessionRepository {

    override val session: Flow<AppSession?> = sessionPreferences.session

    override suspend fun saveSession(session: AppSession) {
        sessionPreferences.save(session)
    }

    override suspend fun clearSession() {
        sessionPreferences.clear()
    }
}
