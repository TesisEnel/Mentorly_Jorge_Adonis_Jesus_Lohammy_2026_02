package com.sagrd.mentorly.data.repository.activity

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.sagrd.mentorly.data.remote.Resource
import com.sagrd.mentorly.data.remote.dto.activity.CreateActivityDto
import com.sagrd.mentorly.data.remote.dto.activity.UpdateActivityDto
import com.sagrd.mentorly.data.remote.dto.common.ReorderItemsDto
import com.sagrd.mentorly.data.remote.dto.content.ActivityDto
import com.sagrd.mentorly.data.remote.remotedatasource.ActivityRemoteDataSource
import com.sagrd.mentorly.domain.model.content.ActivityType
import com.sagrd.mentorly.domain.model.content.ApprovalStrategy
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
class ActivityRepositoryImplTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var repository: ActivityRepositoryImpl
    private lateinit var remoteDataSource: ActivityRemoteDataSource

    @Before
    fun setup() {
        remoteDataSource = mockk(relaxed = true)
        repository = ActivityRepositoryImpl(remoteDataSource)
    }

    @Test
    fun `getActivities retorna actividades correctamente`() = runTest {
        // Given
        val themeId = "theme-1"
        val activities = listOf(
            createActivityDto(id = "activity-1", title = "Práctica de variables"),
            createActivityDto(id = "activity-2", title = "Cuestionario inicial", type = 2),
        )
        coEvery { remoteDataSource.getActivities(themeId) } returns Result.success(activities)

        // When
        val result = repository.getActivities(themeId).toList()

        // Then
        assertTrue(result[0] is Resource.Loading)
        assertTrue(result[1] is Resource.Success)
        assertEquals(2, result[1].data?.size)
        assertEquals("Práctica de variables", result[1].data?.get(0)?.title)
        assertEquals(ActivityType.QUIZ, result[1].data?.get(1)?.type)
        coVerify { remoteDataSource.getActivities(themeId) }
    }

    @Test
    fun `getActivities retorna error cuando falla la fuente remota`() = runTest {
        // Given
        val themeId = "theme-1"
        coEvery { remoteDataSource.getActivities(themeId) } returns Result.failure(Exception("Sin conexión"))

        // When
        val result = repository.getActivities(themeId).toList()

        // Then
        assertTrue(result[0] is Resource.Loading)
        assertTrue(result[1] is Resource.Error)
        assertEquals("Sin conexión", result[1].message)
        coVerify { remoteDataSource.getActivities(themeId) }
    }

    @Test
    fun `createActivity crea actividad correctamente`() = runTest {
        // Given
        val adminId = "admin-1"
        val themeId = "theme-1"
        val request = CreateActivityDto(
            title = "Ejercicio de Kotlin",
            type = 1,
            isMandatory = true,
            approvalStrategy = 2,
            orderIndex = 1,
        )
        val activity = createActivityDto(id = "activity-1", title = request.title)
        coEvery { remoteDataSource.createActivity(adminId, themeId, request) } returns Result.success(activity)

        // When
        val result = repository.createActivity(adminId, themeId, request).toList()

        // Then
        assertTrue(result[0] is Resource.Loading)
        assertTrue(result[1] is Resource.Success)
        assertEquals("activity-1", result[1].data?.id)
        assertEquals(request.title, result[1].data?.title)
        assertEquals(themeId, result[1].data?.themeId)
        coVerify { remoteDataSource.createActivity(adminId, themeId, request) }
    }

    @Test
    fun `updateActivity actualiza actividad correctamente`() = runTest {
        // Given
        val adminId = "admin-1"
        val activityId = "activity-1"
        val request = UpdateActivityDto(
            title = "Actividad actualizada",
            type = 1,
            isMandatory = false,
            approvalStrategy = 3,
            orderIndex = 2,
        )
        coEvery { remoteDataSource.updateActivity(adminId, activityId, request) } returns Result.success(Unit)

        // When
        val result = repository.updateActivity(adminId, activityId, request).toList()

        // Then
        assertTrue(result[0] is Resource.Loading)
        assertTrue(result[1] is Resource.Success)
        coVerify { remoteDataSource.updateActivity(adminId, activityId, request) }
    }

    @Test
    fun `deleteActivity elimina actividad correctamente`() = runTest {
        // Given
        val adminId = "admin-1"
        val activityId = "activity-1"
        coEvery { remoteDataSource.deleteActivity(adminId, activityId) } returns Result.success(Unit)

        // When
        val result = repository.deleteActivity(adminId, activityId).toList()

        // Then
        assertTrue(result[0] is Resource.Loading)
        assertTrue(result[1] is Resource.Success)
        coVerify { remoteDataSource.deleteActivity(adminId, activityId) }
    }

    @Test
    fun `reorderActivities reordena actividades correctamente`() = runTest {
        // Given
        val adminId = "admin-1"
        val themeId = "theme-1"
        val items = ReorderItemsDto(itemIds = listOf("activity-2", "activity-1"))
        coEvery {
            remoteDataSource.reorderActivities(adminId, themeId, items)
        } returns Result.success(Unit)

        // When
        val result = repository.reorderActivities(adminId, themeId, items).toList()

        // Then
        assertTrue(result[0] is Resource.Loading)
        assertTrue(result[1] is Resource.Success)
        coVerify { remoteDataSource.reorderActivities(adminId, themeId, items) }
    }

    private fun createActivityDto(
        id: String,
        title: String,
        type: Int = 1,
    ) = ActivityDto(
        id = id,
        themeId = null,
        title = title,
        type = type,
        isMandatory = true,
        approvalStrategy = 1,
        orderIndex = 1,
    )
}
