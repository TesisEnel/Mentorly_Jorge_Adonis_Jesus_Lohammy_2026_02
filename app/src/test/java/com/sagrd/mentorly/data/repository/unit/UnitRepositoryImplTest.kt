package com.sagrd.mentorly.data.repository.unit

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.sagrd.mentorly.data.remote.Resource
import com.sagrd.mentorly.data.remote.dto.common.ReorderItemsDto
import com.sagrd.mentorly.data.remote.dto.content.CourseUnitDto
import com.sagrd.mentorly.data.remote.dto.unit.CreateUnitDto
import com.sagrd.mentorly.data.remote.dto.unit.UpdateUnitDto
import com.sagrd.mentorly.data.remote.remotedatasource.UnitRemoteDataSource
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
class UnitRepositoryImplTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var repository: UnitRepositoryImpl
    private lateinit var remoteDataSource: UnitRemoteDataSource

    @Before
    fun setup() {
        remoteDataSource = mockk(relaxed = true)
        repository = UnitRepositoryImpl(remoteDataSource)
    }

    @Test
    fun `getUnitsByCourseId retorna unidades correctamente`() = runTest {
        // Given
        val courseId = "course-1"
        val units = listOf(
            createUnitDto(id = "unit-1", title = "Unidad 1"),
            createUnitDto(id = "unit-2", title = "Unidad 2"),
        )
        coEvery { remoteDataSource.getUnitsByCourseId(courseId) } returns Result.success(units)

        // When
        val result = repository.getUnitsByCourseId(courseId).toList()

        // Then
        assertTrue(result[0] is Resource.Loading)
        assertTrue(result[1] is Resource.Success)
        assertEquals(2, result[1].data?.size)
        assertEquals("Unidad 1", result[1].data?.get(0)?.title)
        coVerify { remoteDataSource.getUnitsByCourseId(courseId) }
    }

    @Test
    fun `getUnitsByCourseId retorna error cuando falla`() = runTest {
        // Given
        val courseId = "course-1"
        coEvery { remoteDataSource.getUnitsByCourseId(courseId) } returns Result.failure(Exception("Error"))

        // When
        val result = repository.getUnitsByCourseId(courseId).toList()

        // Then
        assertTrue(result[0] is Resource.Loading)
        assertTrue(result[1] is Resource.Error)
        assertEquals("Error", result[1].message)
    }

    @Test
    fun `createUnit guarda unidad correctamente`() = runTest {
        // Given
        val adminId = "admin-1"
        val courseId = "course-1"
        val unitDto = CreateUnitDto(title = "Nueva Unidad", orderIndex = 1)
        val createdUnit = createUnitDto(id = "unit-1", title = "Nueva Unidad")
        coEvery { remoteDataSource.createUnit(adminId, courseId, unitDto) } returns Result.success(createdUnit)

        // When
        val result = repository.createUnit(adminId, courseId, unitDto).toList()

        // Then
        assertTrue(result[0] is Resource.Loading)
        assertTrue(result[1] is Resource.Success)
        assertEquals("unit-1", result[1].data?.id)
        coVerify { remoteDataSource.createUnit(adminId, courseId, unitDto) }
    }

    @Test
    fun `updateUnit actualiza unidad correctamente`() = runTest {
        // Given
        val adminId = "admin-1"
        val unitId = "unit-1"
        val unitDto = UpdateUnitDto(title = "Unidad Actualizada", orderIndex = 1)
        coEvery { remoteDataSource.updateUnit(adminId, unitId, unitDto) } returns Result.success(Unit)

        // When
        val result = repository.updateUnit(adminId, unitId, unitDto).toList()

        // Then
        assertTrue(result[0] is Resource.Loading)
        assertTrue(result[1] is Resource.Success)
        coVerify { remoteDataSource.updateUnit(adminId, unitId, unitDto) }
    }

    @Test
    fun `deleteUnit elimina unidad correctamente`() = runTest {
        // Given
        val adminId = "admin-1"
        val unitId = "unit-1"
        coEvery { remoteDataSource.deleteUnit(adminId, unitId) } returns Result.success(Unit)

        // When
        val result = repository.deleteUnit(adminId, unitId).toList()

        // Then
        assertTrue(result[0] is Resource.Loading)
        assertTrue(result[1] is Resource.Success)
        coVerify { remoteDataSource.deleteUnit(adminId, unitId) }
    }

    @Test
    fun `reorderUnits reordena unidades correctamente`() = runTest {
        // Given
        val adminId = "admin-1"
        val courseId = "course-1"
        val reorderDto = ReorderItemsDto(listOf("unit-2", "unit-1"))
        coEvery { remoteDataSource.reorderUnits(adminId, courseId, reorderDto) } returns Result.success(Unit)

        // When
        val result = repository.reorderUnits(adminId, courseId, reorderDto).toList()

        // Then
        assertTrue(result[0] is Resource.Loading)
        assertTrue(result[1] is Resource.Success)
        coVerify { remoteDataSource.reorderUnits(adminId, courseId, reorderDto) }
    }

    private fun createUnitDto(id: String, title: String) = CourseUnitDto(
        id = id,
        title = title,
        orderIndex = 1,
        themes = emptyList(),
    )
}
