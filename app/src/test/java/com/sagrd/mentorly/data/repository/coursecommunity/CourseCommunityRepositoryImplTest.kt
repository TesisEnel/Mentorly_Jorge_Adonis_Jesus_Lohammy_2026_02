package com.sagrd.mentorly.data.repository.coursecommunity

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.sagrd.mentorly.data.remote.Resource
import com.sagrd.mentorly.data.remote.dto.community.CourseMemberDto
import com.sagrd.mentorly.data.remote.dto.community.LeaderboardEntryDto
import com.sagrd.mentorly.data.remote.remotedatasource.CourseCommunityRemoteDataSource
import com.sagrd.mentorly.data.repository.community.CourseCommunityRepositoryImpl
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
class CourseCommunityRepositoryImplTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var repository: CourseCommunityRepositoryImpl
    private lateinit var remoteDataSource: CourseCommunityRemoteDataSource

    @Before
    fun setup() {
        remoteDataSource = mockk(relaxed = true)
        repository = CourseCommunityRepositoryImpl(remoteDataSource)
    }

    @Test
    fun `getCourseMembers retorna miembros correctamente`() = runTest {
        // Given
        val courseId = "course-1"
        val viewerStudentId = "student-1"
        val members = listOf(
            createCourseMemberDto("student-1", "Ana García", 1250),
            createCourseMemberDto("student-2", "Carlos Cruz", 980)
        )
        coEvery {
            remoteDataSource.getCourseMembers(courseId, viewerStudentId)
        } returns Result.success(members)

        // When
        val result = repository.getCourseMembers(courseId, viewerStudentId).toList()

        // Then
        assertTrue(result[0] is Resource.Loading)
        assertTrue(result[1] is Resource.Success)
        assertEquals(2, result[1].data?.size)
        assertEquals("Ana García", result[1].data?.get(0)?.displayName)
        assertEquals(980, result[1].data?.get(1)?.totalPoints)
        coVerify { remoteDataSource.getCourseMembers(courseId, viewerStudentId) }
    }

    @Test
    fun `getCourseMembers retorna error cuando falla la fuente remota`() = runTest {
        // Given
        val courseId = "course-1"
        val viewerStudentId = "student-1"
        coEvery {
            remoteDataSource.getCourseMembers(courseId, viewerStudentId)
        } returns Result.failure(Exception("Sin conexión"))

        // When
        val result = repository.getCourseMembers(courseId, viewerStudentId).toList()

        // Then
        assertTrue(result[0] is Resource.Loading)
        assertTrue(result[1] is Resource.Error)
        assertEquals("Sin conexión", result[1].message)
        coVerify { remoteDataSource.getCourseMembers(courseId, viewerStudentId) }
    }

    @Test
    fun `getLeaderboard retorna ranking correctamente`() = runTest {
        // Given
        val courseId = "course-1"
        val viewerStudentId = "student-1"
        val entries = listOf(
            createLeaderboardEntryDto("student-2", "Carlos Cruz", 3400, 1),
            createLeaderboardEntryDto("student-1", "Ana García", 1250, 12)
        )
        coEvery {
            remoteDataSource.getLeaderboard(courseId, viewerStudentId)
        } returns Result.success(entries)

        // When
        val result = repository.getLeaderboard(courseId, viewerStudentId).toList()

        // Then
        assertTrue(result[0] is Resource.Loading)
        assertTrue(result[1] is Resource.Success)
        assertEquals(2, result[1].data?.size)
        assertEquals(1, result[1].data?.get(0)?.rank)
        assertEquals("Ana García", result[1].data?.get(1)?.displayName)
        coVerify { remoteDataSource.getLeaderboard(courseId, viewerStudentId) }
    }

    @Test
    fun `getLeaderboard retorna error cuando falla la fuente remota`() = runTest {
        // Given
        val courseId = "course-1"
        val viewerStudentId = "student-1"
        coEvery {
            remoteDataSource.getLeaderboard(courseId, viewerStudentId)
        } returns Result.failure(Exception("No se pudo consultar el ranking"))

        // When
        val result = repository.getLeaderboard(courseId, viewerStudentId).toList()

        // Then
        assertTrue(result[0] is Resource.Loading)
        assertTrue(result[1] is Resource.Error)
        assertEquals("No se pudo consultar el ranking", result[1].message)
        coVerify { remoteDataSource.getLeaderboard(courseId, viewerStudentId) }
    }

    @Test
    fun `getLeaderboardEntry retorna posicion correctamente`() = runTest {
        // Given
        val courseId = "course-1"
        val studentId = "student-1"
        val entry = createLeaderboardEntryDto(studentId, "Ana García", 1250, 12)
        coEvery {
            remoteDataSource.getLeaderboardEntry(courseId, studentId)
        } returns Result.success(entry)

        // When
        val result = repository.getLeaderboardEntry(courseId, studentId).toList()

        // Then
        assertTrue(result[0] is Resource.Loading)
        assertTrue(result[1] is Resource.Success)
        assertEquals(studentId, result[1].data?.studentId)
        assertEquals(12, result[1].data?.rank)
        assertEquals(1250, result[1].data?.totalPoints)
        coVerify { remoteDataSource.getLeaderboardEntry(courseId, studentId) }
    }

    @Test
    fun `getLeaderboardEntry retorna error cuando falla la fuente remota`() = runTest {
        // Given
        val courseId = "course-1"
        val studentId = "student-1"
        coEvery {
            remoteDataSource.getLeaderboardEntry(courseId, studentId)
        } returns Result.failure(Exception("Posición no disponible"))

        // When
        val result = repository.getLeaderboardEntry(courseId, studentId).toList()

        // Then
        assertTrue(result[0] is Resource.Loading)
        assertTrue(result[1] is Resource.Error)
        assertEquals("Posición no disponible", result[1].message)
        coVerify { remoteDataSource.getLeaderboardEntry(courseId, studentId) }
    }

    @Test
    fun `getAdminLeaderboard retorna ranking administrativo correctamente`() = runTest {
        // Given
        val adminId = "admin-1"
        val courseId = "course-1"
        val entries = listOf(
            createLeaderboardEntryDto("student-1", "Ana García", 1250, 1),
            createLeaderboardEntryDto("student-2", "Carlos Cruz", 980, 2)
        )
        coEvery {
            remoteDataSource.getAdminLeaderboard(adminId, courseId)
        } returns Result.success(entries)

        // When
        val result = repository.getAdminLeaderboard(adminId, courseId).toList()

        // Then
        assertTrue(result[0] is Resource.Loading)
        assertTrue(result[1] is Resource.Success)
        assertEquals(2, result[1].data?.size)
        assertEquals("Ana García", result[1].data?.get(0)?.displayName)
        assertEquals(2, result[1].data?.get(1)?.rank)
        coVerify { remoteDataSource.getAdminLeaderboard(adminId, courseId) }
    }

    @Test
    fun `getAdminLeaderboard retorna error cuando falla la fuente remota`() = runTest {
        // Given
        val adminId = "admin-1"
        val courseId = "course-1"
        coEvery {
            remoteDataSource.getAdminLeaderboard(adminId, courseId)
        } returns Result.failure(Exception("Acceso administrativo no disponible"))

        // When
        val result = repository.getAdminLeaderboard(adminId, courseId).toList()

        // Then
        assertTrue(result[0] is Resource.Loading)
        assertTrue(result[1] is Resource.Error)
        assertEquals("Acceso administrativo no disponible", result[1].message)
        coVerify { remoteDataSource.getAdminLeaderboard(adminId, courseId) }
    }

    private fun createCourseMemberDto(
        studentId: String,
        displayName: String,
        totalPoints: Int
    ) = CourseMemberDto(
        studentId = studentId,
        displayName = displayName,
        totalPoints = totalPoints
    )

    private fun createLeaderboardEntryDto(
        studentId: String,
        displayName: String,
        totalPoints: Int,
        rank: Int
    ) = LeaderboardEntryDto(
        studentId = studentId,
        displayName = displayName,
        totalPoints = totalPoints,
        rank = rank
    )
}
