package com.sagrd.mentorly.data.repository.submission

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.sagrd.mentorly.data.remote.Resource
import com.sagrd.mentorly.data.remote.dto.submission.AdminEscalatedSubmissionDto
import com.sagrd.mentorly.data.remote.dto.submission.AdminSubmissionAuditDto
import com.sagrd.mentorly.data.remote.dto.submission.AdminSubmissionDecisionDto
import com.sagrd.mentorly.data.remote.dto.submission.CreateSubmissionDto
import com.sagrd.mentorly.data.remote.dto.submission.SubmissionDto
import com.sagrd.mentorly.data.remote.dto.submission.SubmissionReviewDto
import com.sagrd.mentorly.data.remote.dto.submission.UpdateSubmissionDto
import com.sagrd.mentorly.data.remote.remotedatasource.SubmissionRemoteDataSource
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
class SubmissionRepositoryImplTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var repository: SubmissionRepositoryImpl
    private lateinit var remoteDataSource: SubmissionRemoteDataSource

    @Before
    fun setup() {
        remoteDataSource = mockk(relaxed = true)
        repository = SubmissionRepositoryImpl(remoteDataSource)
    }

    @Test
    fun `getEscalatedSubmissions retorna entregas escaladas correctamente`() = runTest {
        // Given
        val adminId = "admin-1"
        val submissions = listOf(createEscalatedSubmissionDto("submission-1"))
        coEvery { remoteDataSource.getEscalatedSubmissions(adminId) } returns Result.success(submissions)

        // When
        val result = repository.getEscalatedSubmissions(adminId).toList()

        // Then
        assertTrue(result[0] is Resource.Loading)
        assertTrue(result[1] is Resource.Success)
        assertEquals("submission-1", result[1].data?.first()?.submissionId)
        assertEquals("Curso de Kotlin", result[1].data?.first()?.courseTitle)
        coVerify { remoteDataSource.getEscalatedSubmissions(adminId) }
    }

    @Test
    fun `getEscalatedSubmissionAudit retorna auditoria correctamente`() = runTest {
        // Given
        val adminId = "admin-1"
        val submissionId = "submission-1"
        val audit = createSubmissionAuditDto(submissionId)
        coEvery {
            remoteDataSource.getEscalatedSubmissionAudit(adminId, submissionId)
        } returns Result.success(audit)

        // When
        val result = repository.getEscalatedSubmissionAudit(adminId, submissionId).toList()

        // Then
        assertTrue(result[0] is Resource.Loading)
        assertTrue(result[1] is Resource.Success)
        assertEquals(submissionId, result[1].data?.submissionId)
        assertEquals("Adonis", result[1].data?.authorDisplayName)
        coVerify { remoteDataSource.getEscalatedSubmissionAudit(adminId, submissionId) }
    }

    @Test
    fun `createSubmission crea entrega correctamente`() = runTest {
        // Given
        val enrollmentId = "enrollment-1"
        val activityId = "activity-1"
        val request = CreateSubmissionDto(evidenceType = 1, evidenceContent = "https://github.com/mentorly")
        val submission = createSubmissionDto("submission-1")
        coEvery {
            remoteDataSource.createSubmission(enrollmentId, activityId, request)
        } returns Result.success(submission)

        // When
        val result = repository.createSubmission(enrollmentId, activityId, request).toList()

        // Then
        assertTrue(result[0] is Resource.Loading)
        assertTrue(result[1] is Resource.Success)
        assertEquals("submission-1", result[1].data?.id)
        assertEquals("Práctica de variables", result[1].data?.activityTitle)
        coVerify { remoteDataSource.createSubmission(enrollmentId, activityId, request) }
    }

    @Test
    fun `updateSubmission actualiza entrega correctamente`() = runTest {
        // Given
        val submissionId = "submission-1"
        val request = UpdateSubmissionDto(evidenceType = 2, evidenceContent = "Código actualizado")
        coEvery { remoteDataSource.updateSubmission(submissionId, request) } returns Result.success(Unit)

        // When
        val result = repository.updateSubmission(submissionId, request).toList()

        // Then
        assertTrue(result[0] is Resource.Loading)
        assertTrue(result[1] is Resource.Success)
        coVerify { remoteDataSource.updateSubmission(submissionId, request) }
    }

    @Test
    fun `getSubmissionById retorna entrega correctamente`() = runTest {
        // Given
        val submissionId = "submission-1"
        val submission = createSubmissionDto(submissionId)
        coEvery { remoteDataSource.getSubmissionById(submissionId) } returns Result.success(submission)

        // When
        val result = repository.getSubmissionById(submissionId).toList()

        // Then
        assertTrue(result[0] is Resource.Loading)
        assertTrue(result[1] is Resource.Success)
        assertEquals(submissionId, result[1].data?.id)
        assertEquals("https://github.com/mentorly", result[1].data?.evidenceContent)
        coVerify { remoteDataSource.getSubmissionById(submissionId) }
    }

    @Test
    fun `getSubmissionsByStudentId retorna entregas correctamente`() = runTest {
        // Given
        val studentId = "student-1"
        val submissions = listOf(createSubmissionDto("submission-1"), createSubmissionDto("submission-2"))
        coEvery { remoteDataSource.getSubmissionsByStudentId(studentId) } returns Result.success(submissions)

        // When
        val result = repository.getSubmissionsByStudentId(studentId).toList()

        // Then
        assertTrue(result[0] is Resource.Loading)
        assertTrue(result[1] is Resource.Success)
        assertEquals(2, result[1].data?.size)
        assertEquals("submission-2", result[1].data?.get(1)?.id)
        coVerify { remoteDataSource.getSubmissionsByStudentId(studentId) }
    }

    @Test
    fun `getSubmissionReviews retorna revisiones correctamente`() = runTest {
        // Given
        val studentId = "student-1"
        val submissionId = "submission-1"
        val reviews = listOf(SubmissionReviewDto("review-1", true, "Buen trabajo", "2026-08-17T00:00:00Z"))
        coEvery {
            remoteDataSource.getSubmissionReviews(studentId, submissionId)
        } returns Result.success(reviews)

        // When
        val result = repository.getSubmissionReviews(studentId, submissionId).toList()

        // Then
        assertTrue(result[0] is Resource.Loading)
        assertTrue(result[1] is Resource.Success)
        assertEquals("Buen trabajo", result[1].data?.first()?.feedbackComment)
        assertTrue(result[1].data?.first()?.isApproved == true)
        coVerify { remoteDataSource.getSubmissionReviews(studentId, submissionId) }
    }

    @Test
    fun `escalateSubmission escala entrega correctamente`() = runTest {
        // Given
        val studentId = "student-1"
        val submissionId = "submission-1"
        coEvery { remoteDataSource.escalateSubmission(studentId, submissionId) } returns Result.success(Unit)

        // When
        val result = repository.escalateSubmission(studentId, submissionId).toList()

        // Then
        assertTrue(result[0] is Resource.Loading)
        assertTrue(result[1] is Resource.Success)
        coVerify { remoteDataSource.escalateSubmission(studentId, submissionId) }
    }

    @Test
    fun `decideSubmission registra decision correctamente`() = runTest {
        // Given
        val adminId = "admin-1"
        val submissionId = "submission-1"
        val decision = AdminSubmissionDecisionDto(isApproved = true, feedbackComment = "Entrega aprobada")
        coEvery {
            remoteDataSource.decideSubmission(adminId, submissionId, decision)
        } returns Result.success(Unit)

        // When
        val result = repository.decideSubmission(adminId, submissionId, decision).toList()

        // Then
        assertTrue(result[0] is Resource.Loading)
        assertTrue(result[1] is Resource.Success)
        coVerify { remoteDataSource.decideSubmission(adminId, submissionId, decision) }
    }

    @Test
    fun `getSubmissionById retorna error cuando falla la fuente remota`() = runTest {
        // Given
        val submissionId = "submission-1"
        coEvery { remoteDataSource.getSubmissionById(submissionId) } returns Result.failure(Exception("Sin conexión"))

        // When
        val result = repository.getSubmissionById(submissionId).toList()

        // Then
        assertTrue(result[0] is Resource.Loading)
        assertTrue(result[1] is Resource.Error)
        assertEquals("Sin conexión", result[1].message)
        coVerify { remoteDataSource.getSubmissionById(submissionId) }
    }

    private fun createSubmissionDto(id: String) = SubmissionDto(
        id = id,
        enrollmentId = "enrollment-1",
        activityId = "activity-1",
        activityTitle = "Práctica de variables",
        evidenceType = 1,
        evidenceContent = "https://github.com/mentorly",
        status = 1,
        submittedAt = "2026-08-17T00:00:00Z",
    )

    private fun createEscalatedSubmissionDto(submissionId: String) = AdminEscalatedSubmissionDto(
        submissionId = submissionId,
        enrollmentId = "enrollment-1",
        authorStudentId = "student-1",
        authorDisplayName = "Adonis",
        courseId = "course-1",
        courseTitle = "Curso de Kotlin",
        activityId = "activity-1",
        activityTitle = "Práctica de variables",
        evidenceType = 1,
        evidenceContent = "https://github.com/mentorly",
        submittedAtUtc = "2026-08-17T00:00:00Z",
        escalatedAtUtc = "2026-08-18T00:00:00Z",
        positiveReviews = 1,
        rejectedReviews = 1,
    )

    private fun createSubmissionAuditDto(submissionId: String) = AdminSubmissionAuditDto(
        submissionId = submissionId,
        enrollmentId = "enrollment-1",
        authorStudentId = "student-1",
        authorDisplayName = "Adonis",
        authorEmail = "adonis@mentorly.com",
        courseId = "course-1",
        courseTitle = "Curso de Kotlin",
        activityId = "activity-1",
        activityTitle = "Práctica de variables",
        evidenceType = 1,
        evidenceContent = "https://github.com/mentorly",
        status = 1,
        submittedAtUtc = "2026-08-17T00:00:00Z",
        reviewedAtUtc = null,
        peerReviews = emptyList(),
    )
}
