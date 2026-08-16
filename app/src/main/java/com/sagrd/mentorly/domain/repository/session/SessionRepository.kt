package com.sagrd.mentorly.domain.repository.session

import com.sagrd.mentorly.domain.model.session.AppSession
import kotlinx.coroutines.flow.StateFlow

interface SessionRepository {

    val session: StateFlow<AppSession?>

    suspend fun saveSession(session: AppSession)

    suspend fun clearSession()
}
