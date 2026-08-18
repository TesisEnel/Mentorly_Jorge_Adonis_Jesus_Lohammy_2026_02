package com.sagrd.mentorly.data.repository.quiz

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.sagrd.mentorly.data.remote.Resource
import com.sagrd.mentorly.data.remote.dto.quiz.AdminQuizQuestionDto
import com.sagrd.mentorly.data.remote.dto.quiz.CreateQuizQuestionDto
import com.sagrd.mentorly.data.remote.dto.quiz.QuizAnswerDto
import com.sagrd.mentorly.data.remote.dto.quiz.QuizAttemptDto
import com.sagrd.mentorly.data.remote.dto.quiz.QuizQuestionDto
import com.sagrd.mentorly.data.remote.dto.quiz.SubmitQuizAttemptDto
import com.sagrd.mentorly.data.remote.dto.quiz.UpdateQuizQuestionDto
import com.sagrd.mentorly.data.remote.remotedatasource.QuizRemoteDataSource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class QuizRepositoryImplTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var repository: QuizRepositoryImpl
    private lateinit var remoteDataSource: QuizRemoteDataSource

    @Before
    fun setup() {
        remoteDataSource = mockk(relaxed = true)
        repository = QuizRepositoryImpl(remoteDataSource)
    }

    @Test
    fun `getQuizQuestions retorna preguntas correctamente`() = runTest {
        // Given
        val activityId = "activity-1"
        val questions = listOf(
            QuizQuestionDto("question-1", "¿Qué es Kotlin?", 1),
            QuizQuestionDto("question-2", "¿Qué es Compose?", 2),
        )
        coEvery { remoteDataSource.getQuizQuestions(activityId) } returns Result.success(questions)

        // When
        val result = repository.getQuizQuestions(activityId).toList()

        // Then
        assertTrue(result[0] is Resource.Loading)
        assertTrue(result[1] is Resource.Success)
        assertEquals(2, result[1].data?.size)
        assertEquals("¿Qué es Kotlin?", result[1].data?.get(0)?.prompt)
        assertEquals(2, result[1].data?.get(1)?.orderIndex)
        coVerify { remoteDataSource.getQuizQuestions(activityId) }
    }

    @Test
    fun `getQuizQuestions retorna error cuando falla la fuente remota`() = runTest {
        // Given
        val activityId = "activity-1"
        coEvery { remoteDataSource.getQuizQuestions(activityId) } returns Result.failure(Exception("Sin conexión"))

        // When
        val result = repository.getQuizQuestions(activityId).toList()

        // Then
        assertTrue(result[0] is Resource.Loading)
        assertTrue(result[1] is Resource.Error)
        assertEquals("Sin conexión", result[1].message)
        coVerify { remoteDataSource.getQuizQuestions(activityId) }
    }

    @Test
    fun `createQuizQuestion crea pregunta correctamente`() = runTest {
        // Given
        val adminId = "admin-1"
        val activityId = "activity-1"
        val request = CreateQuizQuestionDto("¿Qué es un ViewModel?", "Una clase que gestiona estado", 1)
        val question = QuizQuestionDto("question-1", request.prompt, request.orderIndex)
        coEvery {
            remoteDataSource.createQuizQuestion(adminId, activityId, request)
        } returns Result.success(question)

        // When
        val result = repository.createQuizQuestion(adminId, activityId, request).toList()

        // Then
        assertTrue(result[0] is Resource.Loading)
        assertTrue(result[1] is Resource.Success)
        assertEquals("question-1", result[1].data?.id)
        assertEquals(request.prompt, result[1].data?.prompt)
        coVerify { remoteDataSource.createQuizQuestion(adminId, activityId, request) }
    }

    @Test
    fun `getAdminQuizQuestions retorna preguntas administrativas correctamente`() = runTest {
        // Given
        val adminId = "admin-1"
        val activityId = "activity-1"
        val questions = listOf(
            AdminQuizQuestionDto("question-1", "¿Qué es Compose?", "Un toolkit de UI", 1),
        )
        coEvery {
            remoteDataSource.getAdminQuizQuestions(adminId, activityId)
        } returns Result.success(questions)

        // When
        val result = repository.getAdminQuizQuestions(adminId, activityId).toList()

        // Then
        assertTrue(result[0] is Resource.Loading)
        assertTrue(result[1] is Resource.Success)
        assertEquals("¿Qué es Compose?", result[1].data?.first()?.prompt)
        assertEquals("Un toolkit de UI", result[1].data?.first()?.correctAnswer)
        coVerify { remoteDataSource.getAdminQuizQuestions(adminId, activityId) }
    }

    @Test
    fun `updateQuizQuestion actualiza pregunta correctamente`() = runTest {
        // Given
        val adminId = "admin-1"
        val questionId = "question-1"
        val request = UpdateQuizQuestionDto("Pregunta actualizada", "Respuesta actualizada", 2)
        val question = AdminQuizQuestionDto(questionId, request.prompt, request.correctAnswer, request.orderIndex)
        coEvery {
            remoteDataSource.updateQuizQuestion(adminId, questionId, request)
        } returns Result.success(question)

        // When
        val result = repository.updateQuizQuestion(adminId, questionId, request).toList()

        // Then
        assertTrue(result[0] is Resource.Loading)
        assertTrue(result[1] is Resource.Success)
        assertEquals("Pregunta actualizada", result[1].data?.prompt)
        assertEquals("Respuesta actualizada", result[1].data?.correctAnswer)
        coVerify { remoteDataSource.updateQuizQuestion(adminId, questionId, request) }
    }

    @Test
    fun `deleteQuizQuestion elimina pregunta correctamente`() = runTest {
        // Given
        val adminId = "admin-1"
        val questionId = "question-1"
        coEvery { remoteDataSource.deleteQuizQuestion(adminId, questionId) } returns Result.success(Unit)

        // When
        val result = repository.deleteQuizQuestion(adminId, questionId).toList()

        // Then
        assertTrue(result[0] is Resource.Loading)
        assertTrue(result[1] is Resource.Success)
        coVerify { remoteDataSource.deleteQuizQuestion(adminId, questionId) }
    }

    @Test
    fun `submitQuizAttempt envia intento correctamente`() = runTest {
        // Given
        val enrollmentId = "enrollment-1"
        val activityId = "activity-1"
        val attempt = SubmitQuizAttemptDto(
            studentId = "student-1",
            answers = listOf(QuizAnswerDto("question-1", "Una clase que gestiona estado")),
        )
        val attemptResult = QuizAttemptDto(
            id = "attempt-1",
            score = 100.0,
            passed = true,
            submittedAtUtc = "2026-08-17T00:00:00Z",
        )
        coEvery {
            remoteDataSource.submitQuizAttempt(enrollmentId, activityId, attempt)
        } returns Result.success(attemptResult)

        // When
        val result = repository.submitQuizAttempt(enrollmentId, activityId, attempt).toList()

        // Then
        assertTrue(result[0] is Resource.Loading)
        assertTrue(result[1] is Resource.Success)
        assertEquals("attempt-1", result[1].data?.id)
        assertEquals(100.0, result[1].data?.score)
        assertTrue(result[1].data?.passed == true)
        coVerify { remoteDataSource.submitQuizAttempt(enrollmentId, activityId, attempt) }
    }
}
