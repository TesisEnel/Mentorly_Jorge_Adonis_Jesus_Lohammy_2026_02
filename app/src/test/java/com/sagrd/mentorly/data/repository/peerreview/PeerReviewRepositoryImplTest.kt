package com.sagrd.mentorly.data.repository.peerreview

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.sagrd.mentorly.data.remote.Resource
import com.sagrd.mentorly.data.remote.dto.peerreview.CreatePeerReviewRequestDto
import com.sagrd.mentorly.data.remote.dto.peerreview.CreatePeerReviewRubricCriterionDto
import com.sagrd.mentorly.data.remote.dto.peerreview.PeerReviewAuditDto
import com.sagrd.mentorly.data.remote.dto.peerreview.PeerReviewDto
import com.sagrd.mentorly.data.remote.dto.peerreview.PeerReviewResultDto
import com.sagrd.mentorly.data.remote.dto.peerreview.PeerReviewRubricCriterionDto
import com.sagrd.mentorly.data.remote.dto.peerreview.ReviewQueueItemDto
import com.sagrd.mentorly.data.remote.dto.peerreview.UpdatePeerReviewRubricCriterionDto
import com.sagrd.mentorly.data.remote.dto.submission.AnonymousSubmissionDto
import com.sagrd.mentorly.data.remote.remotedatasource.PeerReviewRemoteDataSource
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
class PeerReviewRepositoryImplTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var repository: PeerReviewRepositoryImpl
    private lateinit var remoteDataSource: PeerReviewRemoteDataSource

    @Before
    fun setup() {
        remoteDataSource = mockk(relaxed = true)
        repository = PeerReviewRepositoryImpl(remoteDataSource)
    }

    @Test
    fun `getRubric retorna criterios correctamente`() = runTest {
        // Given
        val activityId = "activity-1"
        val criteria = listOf(createRubricDto("criterion-1", activityId, "Claridad"))
        coEvery { remoteDataSource.getRubric(activityId) } returns Result.success(criteria)

        // When
        val result = repository.getRubric(activityId).toList()

        // Then
        assertTrue(result[0] is Resource.Loading)
        assertTrue(result[1] is Resource.Success)
        assertEquals("Claridad", result[1].data?.first()?.title)
        coVerify { remoteDataSource.getRubric(activityId) }
    }

    @Test
    fun `getRubric retorna error cuando falla la fuente remota`() = runTest {
        // Given
        val activityId = "activity-1"
        coEvery { remoteDataSource.getRubric(activityId) } returns Result.failure(Exception("Sin conexión"))

        // When
        val result = repository.getRubric(activityId).toList()

        // Then
        assertTrue(result[0] is Resource.Loading)
        assertTrue(result[1] is Resource.Error)
        assertEquals("Sin conexión", result[1].message)
        coVerify { remoteDataSource.getRubric(activityId) }
    }

    @Test
    fun `createRubricCriterion crea criterio correctamente`() = runTest {
        // Given
        val adminId = "admin-1"
        val activityId = "activity-1"
        val request = CreatePeerReviewRubricCriterionDto("Calidad", "Evalúa la calidad.", 10, 1)
        val criterion = createRubricDto("criterion-1", activityId, request.title)
        coEvery { remoteDataSource.createRubricCriterion(adminId, activityId, request) } returns Result.success(criterion)

        // When
        val result = repository.createRubricCriterion(adminId, activityId, request).toList()

        // Then
        assertTrue(result[0] is Resource.Loading)
        assertTrue(result[1] is Resource.Success)
        assertEquals("criterion-1", result[1].data?.id)
        assertEquals("Calidad", result[1].data?.title)
        coVerify { remoteDataSource.createRubricCriterion(adminId, activityId, request) }
    }

    @Test
    fun `updateRubricCriterion actualiza criterio correctamente`() = runTest {
        // Given
        val adminId = "admin-1"
        val criterionId = "criterion-1"
        val request = UpdatePeerReviewRubricCriterionDto("Claridad", "Descripción actualizada.", 10, 1)
        coEvery { remoteDataSource.updateRubricCriterion(adminId, criterionId, request) } returns Result.success(Unit)

        // When
        val result = repository.updateRubricCriterion(adminId, criterionId, request).toList()

        // Then
        assertTrue(result[0] is Resource.Loading)
        assertTrue(result[1] is Resource.Success)
        coVerify { remoteDataSource.updateRubricCriterion(adminId, criterionId, request) }
    }

    @Test
    fun `deleteRubricCriterion elimina criterio correctamente`() = runTest {
        // Given
        val adminId = "admin-1"
        val criterionId = "criterion-1"
        coEvery { remoteDataSource.deleteRubricCriterion(adminId, criterionId) } returns Result.success(Unit)

        // When
        val result = repository.deleteRubricCriterion(adminId, criterionId).toList()

        // Then
        assertTrue(result[0] is Resource.Loading)
        assertTrue(result[1] is Resource.Success)
        coVerify { remoteDataSource.deleteRubricCriterion(adminId, criterionId) }
    }

    @Test
    fun `getQueue retorna cola de revisiones correctamente`() = runTest {
        // Given
        val studentId = "student-1"
        val queue = listOf(ReviewQueueItemDto("submission-1", "activity-1", "Proyecto final", 1, "https://example.com", "2026-08-17"))
        coEvery { remoteDataSource.getQueue(studentId) } returns Result.success(queue)

        // When
        val result = repository.getQueue(studentId).toList()

        // Then
        assertTrue(result[0] is Resource.Loading)
        assertTrue(result[1] is Resource.Success)
        assertEquals("Proyecto final", result[1].data?.first()?.activityTitle)
        coVerify { remoteDataSource.getQueue(studentId) }
    }

    @Test
    fun `getMyReviews retorna revisiones correctamente`() = runTest {
        // Given
        val studentId = "student-1"
        val reviews = listOf(createPeerReviewDto("review-1", studentId))
        coEvery { remoteDataSource.getMyReviews(studentId) } returns Result.success(reviews)

        // When
        val result = repository.getMyReviews(studentId).toList()

        // Then
        assertTrue(result[0] is Resource.Loading)
        assertTrue(result[1] is Resource.Success)
        assertEquals("review-1", result[1].data?.first()?.id)
        assertTrue(result[1].data?.first()?.isApproved == true)
        coVerify { remoteDataSource.getMyReviews(studentId) }
    }

    @Test
    fun `getAnonymousSubmission retorna envio anonimo correctamente`() = runTest {
        // Given
        val studentId = "student-1"
        val submissionId = "submission-1"
        val submission = AnonymousSubmissionDto(submissionId, "activity-1", "Proyecto final", 2, "Evidencia", "2026-08-17")
        coEvery { remoteDataSource.getAnonymousSubmission(studentId, submissionId) } returns Result.success(submission)

        // When
        val result = repository.getAnonymousSubmission(studentId, submissionId).toList()

        // Then
        assertTrue(result[0] is Resource.Loading)
        assertTrue(result[1] is Resource.Success)
        assertEquals(submissionId, result[1].data?.submissionId)
        assertEquals("Evidencia", result[1].data?.evidenceContent)
        coVerify { remoteDataSource.getAnonymousSubmission(studentId, submissionId) }
    }

    @Test
    fun `getAudit retorna auditoria correctamente`() = runTest {
        // Given
        val adminId = "admin-1"
        val peerReviewId = "review-1"
        val audit = PeerReviewAuditDto(peerReviewId, "submission-1", "author-1", "reviewer-1", true, "Buen trabajo", emptyList(), "2026-08-17", 1, "https://example.com")
        coEvery { remoteDataSource.getAudit(adminId, peerReviewId) } returns Result.success(audit)

        // When
        val result = repository.getAudit(adminId, peerReviewId).toList()

        // Then
        assertTrue(result[0] is Resource.Loading)
        assertTrue(result[1] is Resource.Success)
        assertEquals(peerReviewId, result[1].data?.peerReviewId)
        assertEquals("author-1", result[1].data?.authorStudentId)
        coVerify { remoteDataSource.getAudit(adminId, peerReviewId) }
    }

    @Test
    fun `getAllPeerReviews retorna todas las revisiones correctamente`() = runTest {
        // Given
        val adminId = "admin-1"
        val reviews = listOf(createPeerReviewDto("review-1", "student-1"), createPeerReviewDto("review-2", "student-2"))
        coEvery { remoteDataSource.getAllPeerReviews(adminId) } returns Result.success(reviews)

        // When
        val result = repository.getAllPeerReviews(adminId).toList()

        // Then
        assertTrue(result[0] is Resource.Loading)
        assertTrue(result[1] is Resource.Success)
        assertEquals(2, result[1].data?.size)
        assertEquals("review-2", result[1].data?.get(1)?.id)
        coVerify { remoteDataSource.getAllPeerReviews(adminId) }
    }

    @Test
    fun `submitReview envia revision correctamente`() = runTest {
        // Given
        val studentId = "student-1"
        val request = CreatePeerReviewRequestDto("submission-1", true, "Cumple los criterios")
        val response = PeerReviewResultDto("review-1", "submission-1", studentId, true, request.feedbackComment, "2026-08-17", 2, 2, 2)
        coEvery { remoteDataSource.submitReview(studentId, request) } returns Result.success(response)

        // When
        val result = repository.submitReview(studentId, request).toList()

        // Then
        assertTrue(result[0] is Resource.Loading)
        assertTrue(result[1] is Resource.Success)
        assertEquals("review-1", result[1].data?.peerReviewId)
        assertEquals(2, result[1].data?.positiveReviews)
        assertTrue(result[1].data?.isApproved == true)
        coVerify { remoteDataSource.submitReview(studentId, request) }
    }

    private fun createRubricDto(id: String, activityId: String, title: String) =
        PeerReviewRubricCriterionDto(id, activityId, title, "Descripción del criterio.", 10, 1)

    private fun createPeerReviewDto(id: String, reviewerStudentId: String) =
        PeerReviewDto(id, "submission-1", reviewerStudentId, true, "Buen trabajo", "2026-08-17")
}
