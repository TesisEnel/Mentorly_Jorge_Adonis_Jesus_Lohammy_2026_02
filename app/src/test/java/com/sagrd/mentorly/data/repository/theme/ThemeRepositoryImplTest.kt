package com.sagrd.mentorly.data.repository.theme

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.sagrd.mentorly.data.remote.Resource
import com.sagrd.mentorly.data.remote.dto.common.ReorderItemsDto
import com.sagrd.mentorly.data.remote.dto.theme.CreateThemeDto
import com.sagrd.mentorly.data.remote.dto.theme.ThemeDto
import com.sagrd.mentorly.data.remote.dto.theme.UpdateThemeDto
import com.sagrd.mentorly.data.remote.remotedatasource.ThemeRemoteDataSource
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
class ThemeRepositoryImplTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var repository: ThemeRepositoryImpl
    private lateinit var remoteDataSource: ThemeRemoteDataSource

    @Before
    fun setup() {
        remoteDataSource = mockk(relaxed = true)
        repository = ThemeRepositoryImpl(remoteDataSource)
    }

    @Test
    fun `getThemesByUnit retorna temas correctamente`() = runTest {
        // Given
        val unitId = "unit-1"
        val themes = listOf(
            createThemeDto(id = "theme-1", title = "Tema 1"),
            createThemeDto(id = "theme-2", title = "Tema 2"),
        )
        coEvery { remoteDataSource.getThemesByUnit(unitId) } returns Result.success(themes)

        // When
        val result = repository.getThemesByUnit(unitId).toList()

        // Then
        assertTrue(result[0] is Resource.Loading)
        assertTrue(result[1] is Resource.Success)
        assertEquals(2, result[1].data?.size)
        assertEquals("Tema 1", result[1].data?.get(0)?.title)
        assertEquals("Tema 2", result[1].data?.get(1)?.title)
        coVerify { remoteDataSource.getThemesByUnit(unitId) }
    }

    @Test
    fun `getThemesByUnit retorna error cuando falla la fuente remota`() = runTest {
        // Given
        val unitId = "unit-1"
        coEvery { remoteDataSource.getThemesByUnit(unitId) } returns Result.failure(Exception("Error al cargar"))

        // When
        val result = repository.getThemesByUnit(unitId).toList()

        // Then
        assertTrue(result[0] is Resource.Loading)
        assertTrue(result[1] is Resource.Error)
        assertEquals("Error al cargar", result[1].message)
        coVerify { remoteDataSource.getThemesByUnit(unitId) }
    }

    @Test
    fun `createTheme guarda tema correctamente`() = runTest {
        // Given
        val adminId = "admin-1"
        val unitId = "unit-1"
        val dto = CreateThemeDto(
            title = "Nuevo tema",
            contentText = "Contenido del tema",
            orderIndex = 1,
        )
        val createdTheme = createThemeDto(id = "theme-1", title = "Nuevo tema")
        coEvery { remoteDataSource.createTheme(adminId, unitId, dto) } returns Result.success(createdTheme)

        // When
        val result = repository.createTheme(adminId, unitId, dto).toList()

        // Then
        assertTrue(result[0] is Resource.Loading)
        assertTrue(result[1] is Resource.Success)
        assertEquals("theme-1", result[1].data?.id)
        assertEquals("Nuevo tema", result[1].data?.title)
        coVerify { remoteDataSource.createTheme(adminId, unitId, dto) }
    }

    @Test
    fun `updateTheme actualiza tema correctamente`() = runTest {
        // Given
        val adminId = "admin-1"
        val themeId = "theme-1"
        val dto = UpdateThemeDto(
            title = "Tema actualizado",
            contentText = "Contenido actualizado",
            orderIndex = 2,
        )
        coEvery { remoteDataSource.updateTheme(adminId, themeId, dto) } returns Result.success(Unit)

        // When
        val result = repository.updateTheme(adminId, themeId, dto).toList()

        // Then
        assertTrue(result[0] is Resource.Loading)
        assertTrue(result[1] is Resource.Success)
        coVerify { remoteDataSource.updateTheme(adminId, themeId, dto) }
    }

    @Test
    fun `deleteTheme elimina tema correctamente`() = runTest {
        // Given
        val adminId = "admin-1"
        val themeId = "theme-1"
        coEvery { remoteDataSource.deleteTheme(adminId, themeId) } returns Result.success(Unit)

        // When
        val result = repository.deleteTheme(adminId, themeId).toList()

        // Then
        assertTrue(result[0] is Resource.Loading)
        assertTrue(result[1] is Resource.Success)
        coVerify { remoteDataSource.deleteTheme(adminId, themeId) }
    }

    @Test
    fun `reorderThemes reordena temas correctamente`() = runTest {
        // Given
        val adminId = "admin-1"
        val unitId = "unit-1"
        val dto = ReorderItemsDto(itemIds = listOf("theme-2", "theme-1"))
        coEvery { remoteDataSource.reorderThemes(adminId, unitId, dto) } returns Result.success(Unit)

        // When
        val result = repository.reorderThemes(adminId, unitId, dto).toList()

        // Then
        assertTrue(result[0] is Resource.Loading)
        assertTrue(result[1] is Resource.Success)
        coVerify { remoteDataSource.reorderThemes(adminId, unitId, dto) }
    }

    private fun createThemeDto(
        id: String,
        title: String,
        unitId: String = "unit-1",
        contentText: String = "Contenido",
        orderIndex: Int = 1,
    ) = ThemeDto(
        id = id,
        unitId = unitId,
        title = title,
        contentText = contentText,
        orderIndex = orderIndex
    )
}
