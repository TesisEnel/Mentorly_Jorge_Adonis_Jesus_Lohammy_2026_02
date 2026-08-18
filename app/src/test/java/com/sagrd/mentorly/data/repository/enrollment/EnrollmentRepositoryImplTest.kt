package com.sagrd.mentorly.data.repository.enrollment

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.sagrd.mentorly.data.remote.Resource
import com.sagrd.mentorly.data.remote.dto.enrollment.CertificateDto
import com.sagrd.mentorly.data.remote.dto.enrollment.CreateEnrollmentDto
import com.sagrd.mentorly.data.remote.dto.enrollment.EnrollmentDto
import com.sagrd.mentorly.data.remote.dto.enrollment.EnrollmentResultDto
import com.sagrd.mentorly.data.remote.dto.enrollment.EnrollmentStatusDto
import com.sagrd.mentorly.data.remote.remotedatasource.EnrollmentRemoteDataSource
import com.sagrd.mentorly.domain.model.enrollment.EnrollmentStatus
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
class EnrollmentRepositoryImplTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var repository: EnrollmentRepositoryImpl
    private lateinit var remoteDataSource: EnrollmentRemoteDataSource

    @Before
    fun setup() {
        remoteDataSource = mockk(relaxed = true)
        repository = EnrollmentRepositoryImpl(remoteDataSource)
    }

    @Test
    fun `createEnrollment crea inscripcion correctamente`() = runTest {
        // Given
        val studentId = "student-1"
        val request = CreateEnrollmentDto(courseId = "course-1")
        val enrollment = createEnrollmentResultDto(enrollmentId = "enrollment-1")
        coEvery { remoteDataSource.createEnrollment(studentId, request) } returns Result.success(enrollment)

        // When
        val result = repository.createEnrollment(studentId, request).toList()

        // Then
        assertTrue(result[0] is Resource.Loading)
        assertTrue(result[1] is Resource.Success)
        assertEquals("enrollment-1", result[1].data?.enrollmentId)
        assertEquals(1, result[1].data?.attemptNumber)
        coVerify { remoteDataSource.createEnrollment(studentId, request) }
    }

    @Test
    fun `getEnrollments retorna inscripciones correctamente`() = runTest {
        // Given
        val studentId = "student-1"
        val enrollments = listOf(
            createEnrollmentDto(id = "enrollment-1", courseTitle = "Kotlin desde cero"),
            createEnrollmentDto(id = "enrollment-2", courseTitle = "SQL esencial"),
        )
        coEvery { remoteDataSource.getEnrollments(studentId) } returns Result.success(enrollments)

        // When
        val result = repository.getEnrollments(studentId).toList()

        // Then
        assertTrue(result[0] is Resource.Loading)
        assertTrue(result[1] is Resource.Success)
        assertEquals(2, result[1].data?.size)
        assertEquals("Kotlin desde cero", result[1].data?.get(0)?.courseTitle)
        assertEquals("SQL esencial", result[1].data?.get(1)?.courseTitle)
        coVerify { remoteDataSource.getEnrollments(studentId) }
    }

    @Test
    fun `getEnrollments retorna error cuando falla la fuente remota`() = runTest {
        // Given
        val studentId = "student-1"
        coEvery { remoteDataSource.getEnrollments(studentId) } returns Result.failure(Exception("Sin conexión"))

        // When
        val result = repository.getEnrollments(studentId).toList()

        // Then
        assertTrue(result[0] is Resource.Loading)
        assertTrue(result[1] is Resource.Error)
        assertEquals("Sin conexión", result[1].message)
        coVerify { remoteDataSource.getEnrollments(studentId) }
    }

    @Test
    fun `getAdminStudentEnrollments retorna inscripciones del estudiante correctamente`() = runTest {
        // Given
        val adminId = "admin-1"
        val studentId = "student-1"
        val enrollments = listOf(createEnrollmentDto(id = "enrollment-1"))
        coEvery {
            remoteDataSource.getAdminStudentEnrollments(adminId, studentId)
        } returns Result.success(enrollments)

        // When
        val result = repository.getAdminStudentEnrollments(adminId, studentId).toList()

        // Then
        assertTrue(result[0] is Resource.Loading)
        assertTrue(result[1] is Resource.Success)
        assertEquals("enrollment-1", result[1].data?.first()?.id)
        coVerify { remoteDataSource.getAdminStudentEnrollments(adminId, studentId) }
    }

    @Test
    fun `getEnrollmentById retorna inscripcion correctamente`() = runTest {
        // Given
        val enrollmentId = "enrollment-1"
        val enrollment = createEnrollmentDto(id = enrollmentId, courseTitle = "Desarrollo Android")
        coEvery { remoteDataSource.getEnrollmentById(enrollmentId) } returns Result.success(enrollment)

        // When
        val result = repository.getEnrollmentById(enrollmentId).toList()

        // Then
        assertTrue(result[0] is Resource.Loading)
        assertTrue(result[1] is Resource.Success)
        assertEquals(enrollmentId, result[1].data?.id)
        assertEquals("Desarrollo Android", result[1].data?.courseTitle)
        coVerify { remoteDataSource.getEnrollmentById(enrollmentId) }
    }

    @Test
    fun `restartEnrollment reinicia inscripcion correctamente`() = runTest {
        // Given
        val studentId = "student-1"
        val courseId = "course-1"
        val enrollment = createEnrollmentResultDto(enrollmentId = "enrollment-2", attemptNumber = 2)
        coEvery {
            remoteDataSource.restartEnrollment(studentId, courseId)
        } returns Result.success(enrollment)

        // When
        val result = repository.restartEnrollment(studentId, courseId).toList()

        // Then
        assertTrue(result[0] is Resource.Loading)
        assertTrue(result[1] is Resource.Success)
        assertEquals("enrollment-2", result[1].data?.enrollmentId)
        assertEquals(2, result[1].data?.attemptNumber)
        coVerify { remoteDataSource.restartEnrollment(studentId, courseId) }
    }

    @Test
    fun `getEnrollmentStatus retorna estado correctamente`() = runTest {
        // Given
        val enrollmentId = "enrollment-1"
        val status = EnrollmentStatusDto(status = 2)
        coEvery { remoteDataSource.getEnrollmentStatus(enrollmentId) } returns Result.success(status)

        // When
        val result = repository.getEnrollmentStatus(enrollmentId).toList()

        // Then
        assertTrue(result[0] is Resource.Loading)
        assertTrue(result[1] is Resource.Success)
        assertEquals(EnrollmentStatus.COMPLETED, result[1].data)
        coVerify { remoteDataSource.getEnrollmentStatus(enrollmentId) }
    }

    @Test
    fun `getCertificate retorna certificado correctamente`() = runTest {
        // Given
        val enrollmentId = "enrollment-1"
        val certificate = CertificateDto(
            certificateUrl = "https://mentorly.com/certificates/enrollment-1",
            issuedAt = "2026-08-17T00:00:00Z",
        )
        coEvery { remoteDataSource.getCertificate(enrollmentId) } returns Result.success(certificate)

        // When
        val result = repository.getCertificate(enrollmentId).toList()

        // Then
        assertTrue(result[0] is Resource.Loading)
        assertTrue(result[1] is Resource.Success)
        assertEquals(certificate.certificateUrl, result[1].data?.certificateUrl)
        assertEquals(certificate.issuedAt, result[1].data?.issuedAt)
        coVerify { remoteDataSource.getCertificate(enrollmentId) }
    }

    private fun createEnrollmentDto(
        id: String,
        courseTitle: String = "Curso de prueba",
    ) = EnrollmentDto(
        id = id,
        studentId = "student-1",
        courseId = "course-1",
        courseTitle = courseTitle,
        attemptNumber = 1,
        startedAt = "2026-08-01T00:00:00Z",
        expiresAt = "2026-09-01T00:00:00Z",
        completedAt = null,
        status = 1,
    )

    private fun createEnrollmentResultDto(
        enrollmentId: String,
        attemptNumber: Int = 1,
    ) = EnrollmentResultDto(
        enrollmentId = enrollmentId,
        attemptNumber = attemptNumber,
        startedAtUtc = "2026-08-01T00:00:00Z",
        expiresAtUtc = "2026-09-01T00:00:00Z",
        status = 1,
    )
}
