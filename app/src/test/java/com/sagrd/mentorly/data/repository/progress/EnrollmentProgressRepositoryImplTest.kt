package com.sagrd.mentorly.data.repository.progress

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.sagrd.mentorly.data.remote.Resource
import com.sagrd.mentorly.data.remote.dto.progress.EnrollmentProgressDto
import com.sagrd.mentorly.data.remote.remotedatasource.EnrollmentProgressRemoteDataSource
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
class EnrollmentProgressRepositoryImplTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var repository: EnrollmentProgressRepositoryImpl
    private lateinit var remoteDataSource: EnrollmentProgressRemoteDataSource

    @Before
    fun setup() {
        remoteDataSource = mockk(relaxed = true)
        repository = EnrollmentProgressRepositoryImpl(remoteDataSource)
    }

    @Test
    fun `getEnrollmentProgress retorna progreso correctamente`() = runTest {
        // Given
        val enrollmentId = "enrollment-1"
        val progressDto = createEnrollmentProgressDto(enrollmentId)
        coEvery { remoteDataSource.getEnrollmentProgress(enrollmentId) } returns Result.success(progressDto)

        // When
        val result = repository.getEnrollmentProgress(enrollmentId).toList()

        // Then
        assertTrue(result[0] is Resource.Loading)
        assertTrue(result[1] is Resource.Success)
        assertEquals(enrollmentId, result[1].data?.enrollmentId)
        coVerify { remoteDataSource.getEnrollmentProgress(enrollmentId) }
    }

    @Test
    fun `getEnrollmentProgress retorna error cuando falla`() = runTest {
        // Given
        val enrollmentId = "enrollment-1"
        coEvery { remoteDataSource.getEnrollmentProgress(enrollmentId) } returns Result.failure(Exception("Error"))

        // When
        val result = repository.getEnrollmentProgress(enrollmentId).toList()

        // Then
        assertTrue(result[0] is Resource.Loading)
        assertTrue(result[1] is Resource.Error)
        assertEquals("Error", result[1].message)
    }

    @Test
    fun `getAdminEnrollmentProgress retorna progreso administrativo correctamente`() = runTest {
        // Given
        val adminId = "admin-1"
        val enrollmentId = "enrollment-admin"
        val progressDto = createEnrollmentProgressDto(enrollmentId)
        coEvery { 
            remoteDataSource.getAdminEnrollmentProgress(adminId, enrollmentId) 
        } returns Result.success(progressDto)

        // When
        val result = repository.getAdminEnrollmentProgress(adminId, enrollmentId).toList()

        // Then
        assertTrue(result[0] is Resource.Loading)
        assertTrue(result[1] is Resource.Success)
        assertEquals(enrollmentId, result[1].data?.enrollmentId)
        coVerify { remoteDataSource.getAdminEnrollmentProgress(adminId, enrollmentId) }
    }

    @Test
    fun `completeTheme completa tema correctamente`() = runTest {
        // Given
        val enrollmentId = "enrollment-1"
        val themeId = "theme-1"
        val progressDto = createEnrollmentProgressDto(enrollmentId)
        coEvery { remoteDataSource.completeTheme(enrollmentId, themeId) } returns Result.success(progressDto)

        // When
        val result = repository.completeTheme(enrollmentId, themeId).toList()

        // Then
        assertTrue(result[0] is Resource.Loading)
        assertTrue(result[1] is Resource.Success)
        assertEquals(enrollmentId, result[1].data?.enrollmentId)
        coVerify { remoteDataSource.completeTheme(enrollmentId, themeId) }
    }

    private fun createEnrollmentProgressDto(enrollmentId: String) = EnrollmentProgressDto(
        enrollmentId = enrollmentId,
        percentage = 50,
        completedThemes = 5,
        totalThemes = 10,
        approvedMandatoryActivities = 2,
        totalMandatoryActivities = 4,
        canSubmitNextUnit = true,
        blockedReason = null,
        units = emptyList(),
    )
}
