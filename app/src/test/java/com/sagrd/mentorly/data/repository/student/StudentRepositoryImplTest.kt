package com.sagrd.mentorly.data.repository.student

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.sagrd.mentorly.data.remote.Resource
import com.sagrd.mentorly.data.remote.dto.student.BadgeDto
import com.sagrd.mentorly.data.remote.dto.student.ProvisionStudentDto
import com.sagrd.mentorly.data.remote.dto.student.StudentDto
import com.sagrd.mentorly.data.remote.dto.student.StudentStatisticsDto
import com.sagrd.mentorly.data.remote.dto.student.UpdateLeaderboardPrivacyDto
import com.sagrd.mentorly.data.remote.dto.student.UpdateStudentDto
import com.sagrd.mentorly.data.remote.remotedatasource.StudentRemoteDataSource
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
class StudentRepositoryImplTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var repository: StudentRepositoryImpl
    private lateinit var remoteDataSource: StudentRemoteDataSource

    @Before
    fun setup() {
        remoteDataSource = mockk(relaxed = true)
        repository = StudentRepositoryImpl(remoteDataSource)
    }

    @Test
    fun `getStudents retorna lista de estudiantes correctamente`() = runTest {
        // Given
        val students = listOf(createStudentDto("student-1", "Jorge Moya"))
        coEvery { remoteDataSource.getStudents() } returns Result.success(students)

        // When
        val result = repository.getStudents().toList()

        // Then
        assertTrue(result[0] is Resource.Loading)
        assertTrue(result[1] is Resource.Success)
        assertEquals("Jorge Moya", result[1].data?.first()?.displayName)
        coVerify { remoteDataSource.getStudents() }
    }

    @Test
    fun `getStudents retorna error cuando falla la fuente remota`() = runTest {
        // Given
        coEvery { remoteDataSource.getStudents() } returns Result.failure(Exception("Sin conexión"))

        // When
        val result = repository.getStudents().toList()

        // Then
        assertTrue(result[0] is Resource.Loading)
        assertTrue(result[1] is Resource.Error)
        assertEquals("Sin conexión", result[1].message)
        coVerify { remoteDataSource.getStudents() }
    }

    @Test
    fun `getStudentById retorna estudiante correctamente`() = runTest {
        // Given
        val studentId = "student-1"
        val student = createStudentDto(studentId, "Jorge Moya")
        coEvery { remoteDataSource.getStudentById(studentId) } returns Result.success(student)

        // When
        val result = repository.getStudentById(studentId).toList()

        // Then
        assertTrue(result[0] is Resource.Loading)
        assertTrue(result[1] is Resource.Success)
        assertEquals(studentId, result[1].data?.id)
        assertEquals("Jorge Moya", result[1].data?.displayName)
        coVerify { remoteDataSource.getStudentById(studentId) }
    }

    @Test
    fun `getStudentById retorna error cuando falla la fuente remota`() = runTest {
        // Given
        val studentId = "student-1"
        coEvery { remoteDataSource.getStudentById(studentId) } returns Result.failure(Exception("Estudiante no encontrado"))

        // When
        val result = repository.getStudentById(studentId).toList()

        // Then
        assertTrue(result[0] is Resource.Loading)
        assertTrue(result[1] is Resource.Error)
        assertEquals("Estudiante no encontrado", result[1].message)
        coVerify { remoteDataSource.getStudentById(studentId) }
    }

    @Test
    fun `provisionStudent crea o sincroniza estudiante correctamente`() = runTest {
        // Given
        val request = ProvisionStudentDto("google-uid-1", "jorge@example.com", "Jorge Moya")
        val provisioned = createStudentDto("student-1", request.displayName)
        coEvery { remoteDataSource.provisionStudent(request) } returns Result.success(provisioned)

        // When
        val result = repository.provisionStudent(request).toList()

        // Then
        assertTrue(result[0] is Resource.Loading)
        assertTrue(result[1] is Resource.Success)
        assertEquals("student-1", result[1].data?.id)
        assertEquals("Jorge Moya", result[1].data?.displayName)
        coVerify { remoteDataSource.provisionStudent(request) }
    }

    @Test
    fun `provisionStudent retorna error cuando falla la fuente remota`() = runTest {
        // Given
        val request = ProvisionStudentDto("google-uid-1", "jorge@example.com", "Jorge Moya")
        coEvery { remoteDataSource.provisionStudent(request) } returns Result.failure(Exception("No se pudo provisionar"))

        // When
        val result = repository.provisionStudent(request).toList()

        // Then
        assertTrue(result[0] is Resource.Loading)
        assertTrue(result[1] is Resource.Error)
        assertEquals("No se pudo provisionar", result[1].message)
        coVerify { remoteDataSource.provisionStudent(request) }
    }

    @Test
    fun `updateStudent actualiza estudiante correctamente`() = runTest {
        // Given
        val studentId = "student-1"
        val request = UpdateStudentDto("jorge.nuevo@example.com", "Jorge Moya A.")
        coEvery { remoteDataSource.updateStudent(studentId, request) } returns Result.success(Unit)

        // When
        val result = repository.updateStudent(studentId, request).toList()

        // Then
        assertTrue(result[0] is Resource.Loading)
        assertTrue(result[1] is Resource.Success)
        coVerify { remoteDataSource.updateStudent(studentId, request) }
    }

    @Test
    fun `updateStudent retorna error cuando falla la fuente remota`() = runTest {
        // Given
        val studentId = "student-1"
        val request = UpdateStudentDto("jorge.nuevo@example.com", "Jorge Moya A.")
        coEvery { remoteDataSource.updateStudent(studentId, request) } returns Result.failure(Exception("No se pudo actualizar"))

        // When
        val result = repository.updateStudent(studentId, request).toList()

        // Then
        assertTrue(result[0] is Resource.Loading)
        assertTrue(result[1] is Resource.Error)
        assertEquals("No se pudo actualizar", result[1].message)
        coVerify { remoteDataSource.updateStudent(studentId, request) }
    }

    @Test
    fun `updateLeaderboardPrivacy actualiza privacidad correctamente`() = runTest {
        // Given
        val studentId = "student-1"
        val request = UpdateLeaderboardPrivacyDto(false)
        coEvery { remoteDataSource.updateLeaderboardPrivacy(studentId, request) } returns Result.success(Unit)

        // When
        val result = repository.updateLeaderboardPrivacy(studentId, request).toList()

        // Then
        assertTrue(result[0] is Resource.Loading)
        assertTrue(result[1] is Resource.Success)
        coVerify { remoteDataSource.updateLeaderboardPrivacy(studentId, request) }
    }

    @Test
    fun `updateLeaderboardPrivacy retorna error cuando falla la fuente remota`() = runTest {
        // Given
        val studentId = "student-1"
        val request = UpdateLeaderboardPrivacyDto(false)
        coEvery { remoteDataSource.updateLeaderboardPrivacy(studentId, request) } returns Result.failure(Exception("No se pudo actualizar la privacidad"))

        // When
        val result = repository.updateLeaderboardPrivacy(studentId, request).toList()

        // Then
        assertTrue(result[0] is Resource.Loading)
        assertTrue(result[1] is Resource.Error)
        assertEquals("No se pudo actualizar la privacidad", result[1].message)
        coVerify { remoteDataSource.updateLeaderboardPrivacy(studentId, request) }
    }

    @Test
    fun `getStudentStatistics retorna estadisticas correctamente`() = runTest {
        // Given
        val studentId = "student-1"
        val statistics = createStudentStatisticsDto(studentId)
        coEvery { remoteDataSource.getStudentStatistics(studentId) } returns Result.success(statistics)

        // When
        val result = repository.getStudentStatistics(studentId).toList()

        // Then
        assertTrue(result[0] is Resource.Loading)
        assertTrue(result[1] is Resource.Success)
        assertEquals(studentId, result[1].data?.studentId)
        assertEquals(1, result[1].data?.badges?.size)
        assertEquals("Primer Ejercicio", result[1].data?.badges?.first()?.name)
        coVerify { remoteDataSource.getStudentStatistics(studentId) }
    }

    @Test
    fun `getStudentStatistics retorna error cuando falla la fuente remota`() = runTest {
        // Given
        val studentId = "student-1"
        coEvery { remoteDataSource.getStudentStatistics(studentId) } returns Result.failure(Exception("No se pudieron cargar"))

        // When
        val result = repository.getStudentStatistics(studentId).toList()

        // Then
        assertTrue(result[0] is Resource.Loading)
        assertTrue(result[1] is Resource.Error)
        assertEquals("No se pudieron cargar", result[1].message)
        coVerify { remoteDataSource.getStudentStatistics(studentId) }
    }

    @Test
    fun `promoteToAdmin promueve estudiante correctamente`() = runTest {
        // Given
        val adminId = "admin-1"
        val studentId = "student-1"
        coEvery { remoteDataSource.promoteToAdmin(adminId, studentId) } returns Result.success(Unit)

        // When
        val result = repository.promoteToAdmin(adminId, studentId).toList()

        // Then
        assertTrue(result[0] is Resource.Loading)
        assertTrue(result[1] is Resource.Success)
        coVerify { remoteDataSource.promoteToAdmin(adminId, studentId) }
    }

    @Test
    fun `promoteToAdmin retorna error cuando falla la fuente remota`() = runTest {
        // Given
        val adminId = "admin-1"
        val studentId = "student-1"
        coEvery { remoteDataSource.promoteToAdmin(adminId, studentId) } returns Result.failure(Exception("No se pudo promover"))

        // When
        val result = repository.promoteToAdmin(adminId, studentId).toList()

        // Then
        assertTrue(result[0] is Resource.Loading)
        assertTrue(result[1] is Resource.Error)
        assertEquals("No se pudo promover", result[1].message)
        coVerify { remoteDataSource.promoteToAdmin(adminId, studentId) }
    }

    private fun createStudentDto(id: String, displayName: String) =
        StudentDto(
            id = id,
            email = "$id@example.com",
            displayName = displayName,
            role = 1,
            isLeaderboardPublic = true,
            totalPoints = 100
        )

    private fun createStudentStatisticsDto(studentId: String) =
        StudentStatisticsDto(
            studentId = studentId,
            role = 1,
            isLeaderboardPublic = true,
            totalPoints = 100,
            badges = listOf(createBadgeDto("badge-1", "Primer Ejercicio"))
        )

    private fun createBadgeDto(id: String, name: String) =
        BadgeDto(
            id = id,
            name = name,
            description = "Completó su primer ejercicio.",
            imageUrl = null,
            grantedAt = "2026-08-17"
        )
}