package com.sagrd.mentorly.data.local.session

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.sagrd.mentorly.domain.model.session.AppSession
import com.sagrd.mentorly.domain.model.student.StudentRole
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private val Context.sessionDataStore by preferencesDataStore(name = "mentorly_session")

class SessionPreferences @Inject constructor(
    @param:ApplicationContext private val context: Context
) {

    val session: Flow<AppSession?> = context.sessionDataStore.data.map { preferences ->
        preferences.toSession()
    }

    suspend fun save(session: AppSession) {
        context.sessionDataStore.edit { preferences ->
            preferences[StudentIdKey] = session.studentId
            preferences[FirebaseUserIdKey] = session.firebaseUserId
            preferences[DisplayNameKey] = session.displayName
            session.email?.let { preferences[EmailKey] = it }
                ?: preferences.remove(EmailKey)
            preferences[RoleKey] = session.role.name
            preferences[LeaderboardVisibilityKey] = session.isLeaderboardPublic
        }
    }

    suspend fun clear() {
        context.sessionDataStore.edit { preferences ->
            preferences.clear()
        }
    }

    private fun Preferences.toSession(): AppSession? {
        val studentId = this[StudentIdKey] ?: return null
        val firebaseUserId = this[FirebaseUserIdKey] ?: return null
        val displayName = this[DisplayNameKey] ?: return null
        val role = this[RoleKey]
            ?.let { roleName -> runCatching { StudentRole.valueOf(roleName) }.getOrNull() }
            ?: return null

        return AppSession(
            studentId = studentId,
            firebaseUserId = firebaseUserId,
            displayName = displayName,
            email = this[EmailKey],
            role = role,
            isLeaderboardPublic = this[LeaderboardVisibilityKey] ?: true
        )
    }

    private companion object {
        val StudentIdKey = stringPreferencesKey("student_id")
        val FirebaseUserIdKey = stringPreferencesKey("firebase_user_id")
        val DisplayNameKey = stringPreferencesKey("display_name")
        val EmailKey = stringPreferencesKey("email")
        val RoleKey = stringPreferencesKey("role")
        val LeaderboardVisibilityKey = booleanPreferencesKey("is_leaderboard_public")
    }
}
