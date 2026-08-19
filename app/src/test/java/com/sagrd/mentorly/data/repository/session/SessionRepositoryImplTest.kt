package com.sagrd.mentorly.data.repository.session

import com.sagrd.mentorly.domain.model.session.AppSession
import com.sagrd.mentorly.domain.model.student.StudentRole
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class SessionRepositoryImplTest {

    private lateinit var repository: SessionRepositoryImpl

    @Before
    fun setup() {
        repository = SessionRepositoryImpl()
    }

    @Test
    fun `session inicia en null cuando no se ha guardado ninguna sesion`() = runTest {
        // When
        val currentSession = repository.session.value

        // Then
        assertNull(currentSession)
    }

    @Test
    fun `saveSession guarda la sesion correctamente`() = runTest {
        // Given
        val session = createAppSession()

        // When
        repository.saveSession(session)

        // Then
        assertEquals(session, repository.session.value)
        assertEquals("student-1", repository.session.value?.studentId)
        assertEquals(StudentRole.STUDENT, repository.session.value?.role)
    }

    @Test
    fun `saveSession sobreescribe una sesion previa`() = runTest {
        // Given
        val firstSession = createAppSession()
        val secondSession = createAppSession(
            studentId = "student-2",
            displayName = "Ana Pérez",
            role = StudentRole.ADMIN
        )
        repository.saveSession(firstSession)

        // When
        repository.saveSession(secondSession)

        // Then
        assertEquals(secondSession, repository.session.value)
        assertEquals("student-2", repository.session.value?.studentId)
        assertEquals(StudentRole.ADMIN, repository.session.value?.role)
    }

    @Test
    fun `clearSession limpia la sesion correctamente`() = runTest {
        // Given
        val session = createAppSession()
        repository.saveSession(session)

        // When
        repository.clearSession()

        // Then
        assertNull(repository.session.value)
    }

    private fun createAppSession(
        studentId: String = "student-1",
        firebaseUserId: String = "firebase-uid-1",
        displayName: String = "Jorge Moya",
        email: String? = "jorge@example.com",
        role: StudentRole = StudentRole.STUDENT,
        isLeaderboardPublic: Boolean = true
    ) = AppSession(
        studentId = studentId,
        firebaseUserId = firebaseUserId,
        displayName = displayName,
        email = email,
        role = role,
        isLeaderboardPublic = isLeaderboardPublic
    )
}