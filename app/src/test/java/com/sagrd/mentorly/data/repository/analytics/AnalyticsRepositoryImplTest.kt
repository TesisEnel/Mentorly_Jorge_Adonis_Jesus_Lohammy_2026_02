package com.sagrd.mentorly.data.repository.analytics

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.sagrd.mentorly.data.remote.Resource
import com.sagrd.mentorly.data.remote.dto.analytics.AnalyticsOverviewDto
import com.sagrd.mentorly.data.remote.remotedatasource.AnalyticsRemoteDataSource
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
class AnalyticsRepositoryImplTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var repository: AnalyticsRepositoryImpl
    private lateinit var remoteDataSource: AnalyticsRemoteDataSource

    @Before
    fun setup() {
        remoteDataSource = mockk(relaxed = true)
        repository = AnalyticsRepositoryImpl(remoteDataSource)
    }

    @Test
    fun `getOverview retorna datos correctamente`() = runTest {
        // Given
        val adminId = "admin-1"
        val overviewDto = AnalyticsOverviewDto(
            courses = 5,
            activeEnrollments = 10,
            completedEnrollments = 20,
            expiredEnrollments = 2,
            pendingPeerReviewSubmissions = 3,
        )
        coEvery { remoteDataSource.getOverview(adminId) } returns Result.success(overviewDto)

        // When
        val result = repository.getOverview(adminId).toList()

        // Then
        assertTrue(result[0] is Resource.Loading)
        assertTrue(result[1] is Resource.Success)
        assertEquals(5, result[1].data?.courses)
        coVerify { remoteDataSource.getOverview(adminId) }
    }

    @Test
    fun `getOverview retorna error cuando falla`() = runTest {
        // Given
        val adminId = "admin-1"
        coEvery { remoteDataSource.getOverview(adminId) } returns Result.failure(Exception("Error"))

        // When
        val result = repository.getOverview(adminId).toList()

        // Then
        assertTrue(result[0] is Resource.Loading)
        assertTrue(result[1] is Resource.Error)
        assertEquals("Error", result[1].message)
    }

    @Test
    fun `getDropOff retorna lista correctamente`() = runTest {
        // Given
        val adminId = "admin-1"
        val courseId = "course-1"
        coEvery { remoteDataSource.getDropOff(adminId, courseId) } returns Result.success(emptyList())

        // When
        val result = repository.getDropOff(adminId, courseId).toList()

        // Then
        assertTrue(result[1] is Resource.Success)
        coVerify { remoteDataSource.getDropOff(adminId, courseId) }
    }

    @Test
    fun `getBottlenecks retorna lista correctamente`() = runTest {
        // Given
        val adminId = "admin-1"
        val courseId = "course-1"
        coEvery { remoteDataSource.getBottlenecks(adminId, courseId) } returns Result.success(emptyList())

        // When
        val result = repository.getBottlenecks(adminId, courseId).toList()

        // Then
        assertTrue(result[1] is Resource.Success)
        coVerify { remoteDataSource.getBottlenecks(adminId, courseId) }
    }

    @Test
    fun `getEnrollmentHistory retorna lista correctamente`() = runTest {
        // Given
        val adminId = "admin-1"
        val courseId = "course-1"
        coEvery { remoteDataSource.getEnrollmentHistory(adminId, courseId) } returns Result.success(emptyList())

        // When
        val result = repository.getEnrollmentHistory(adminId, courseId).toList()

        // Then
        assertTrue(result[1] is Resource.Success)
        coVerify { remoteDataSource.getEnrollmentHistory(adminId, courseId) }
    }
}
