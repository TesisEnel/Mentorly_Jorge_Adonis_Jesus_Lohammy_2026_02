package com.sagrd.mentorly.domain.repository.session

import com.sagrd.mentorly.domain.model.session.AppSession
import kotlinx.coroutines.flow.Flow

interface SessionRepository {

    val session: Flow<AppSession?>

    suspend fun saveSession(session: AppSession)

    suspend fun clearSession()
}
