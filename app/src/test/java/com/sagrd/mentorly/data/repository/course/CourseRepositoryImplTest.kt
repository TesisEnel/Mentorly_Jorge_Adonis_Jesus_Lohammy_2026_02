package com.sagrd.mentorly.data.repository.course

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.sagrd.mentorly.data.remote.Resource
import com.sagrd.mentorly.data.remote.dto.course.CourseDto
import com.sagrd.mentorly.data.remote.dto.course.CreateCourseDto
import com.sagrd.mentorly.data.remote.dto.course.UpdateCourseDto
import com.sagrd.mentorly.data.remote.dto.course.UpdateCoursePublicationDto
import com.sagrd.mentorly.data.remote.remotedatasource.CourseRemoteDataSource
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
class CourseRepositoryImplTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var repository: CourseRepositoryImpl
    private lateinit var remoteDataSource: CourseRemoteDataSource

    @Before
    fun setup() {
        remoteDataSource = mockk(relaxed = true)
        repository = CourseRepositoryImpl(remoteDataSource)
    }

    @Test
    fun `getCourses retorna cursos correctamente`() = runTest {
        // Given
        val courses = listOf(
            createCourseDto(id = "course-1", title = "Kotlin desde cero"),
            createCourseDto(id = "course-2", title = "SQL esencial")
        )
        coEvery { remoteDataSource.getCourses() } returns Result.success(courses)

        // When
        val result = repository.getCourses().toList()

        // Then
        assertTrue(result[0] is Resource.Loading)
        assertTrue(result[1] is Resource.Success)
        assertEquals(2, result[1].data?.size)
        assertEquals("Kotlin desde cero", result[1].data?.get(0)?.title)
        assertEquals("SQL esencial", result[1].data?.get(1)?.title)
        coVerify { remoteDataSource.getCourses() }
    }

    @Test
    fun `getCourses retorna error cuando falla la fuente remota`() = runTest {
        // Given
        coEvery { remoteDataSource.getCourses() } returns Result.failure(Exception("Sin conexión"))

        // When
        val result = repository.getCourses().toList()

        // Then
        assertTrue(result[0] is Resource.Loading)
        assertTrue(result[1] is Resource.Error)
        assertEquals("Sin conexión", result[1].message)
        coVerify { remoteDataSource.getCourses() }
    }

    @Test
    fun `getCourseById retorna curso correctamente`() = runTest {
        // Given
        val courseId = "course-1"
        val course = createCourseDto(id = courseId, title = "Desarrollo Android")
        coEvery { remoteDataSource.getCourseById(courseId) } returns Result.success(course)

        // When
        val result = repository.getCourseById(courseId).toList()

        // Then
        assertTrue(result[0] is Resource.Loading)
        assertTrue(result[1] is Resource.Success)
        assertEquals(courseId, result[1].data?.id)
        assertEquals("Desarrollo Android", result[1].data?.title)
        coVerify { remoteDataSource.getCourseById(courseId) }
    }

    @Test
    fun `getCourseContent retorna contenido del curso correctamente`() = runTest {
        // Given
        val courseId = "course-1"
        val course = createCourseDto(id = courseId, title = "Arquitectura de software")
        coEvery { remoteDataSource.getCourseContent(courseId) } returns Result.success(course)

        // When
        val result = repository.getCourseContent(courseId).toList()

        // Then
        assertTrue(result[0] is Resource.Loading)
        assertTrue(result[1] is Resource.Success)
        assertEquals(courseId, result[1].data?.id)
        assertEquals("Arquitectura de software", result[1].data?.title)
        coVerify { remoteDataSource.getCourseContent(courseId) }
    }

    @Test
    fun `createCourse crea curso correctamente`() = runTest {
        // Given
        val adminId = "admin-1"
        val request = CreateCourseDto(
            title = "Programación orientada a objetos",
            description = "Fundamentos de programación orientada a objetos.",
            requiredPeerReviews = 2,
            imageUrl = null
        )
        val createdCourse = createCourseDto(
            id = "course-1",
            title = request.title,
            description = request.description,
            requiredPeerReviews = request.requiredPeerReviews
        )
        coEvery { remoteDataSource.createCourse(adminId, request) } returns Result.success(createdCourse)

        // When
        val result = repository.createCourse(adminId, request).toList()

        // Then
        assertTrue(result[0] is Resource.Loading)
        assertTrue(result[1] is Resource.Success)
        assertEquals("course-1", result[1].data?.id)
        assertEquals(request.title, result[1].data?.title)
        coVerify { remoteDataSource.createCourse(adminId, request) }
    }

    @Test
    fun `updateCourse actualiza curso correctamente`() = runTest {
        // Given
        val adminId = "admin-1"
        val courseId = "course-1"
        val request = UpdateCourseDto(
            title = "Curso actualizado",
            description = "Descripción actualizada.",
            requiredPeerReviews = 3,
            imageUrl = "https://example.com/course.png"
        )
        coEvery { remoteDataSource.updateCourse(adminId, courseId, request) } returns Result.success(Unit)

        // When
        val result = repository.updateCourse(adminId, courseId, request).toList()

        // Then
        assertTrue(result[0] is Resource.Loading)
        assertTrue(result[1] is Resource.Success)
        coVerify { remoteDataSource.updateCourse(adminId, courseId, request) }
    }

    @Test
    fun `updateCoursePublication actualiza publicacion correctamente`() = runTest {
        // Given
        val adminId = "admin-1"
        val courseId = "course-1"
        val publication = UpdateCoursePublicationDto(isPublished = true)
        coEvery {
            remoteDataSource.updateCoursePublication(adminId, courseId, publication)
        } returns Result.success(Unit)

        // When
        val result = repository.updateCoursePublication(adminId, courseId, publication).toList()

        // Then
        assertTrue(result[0] is Resource.Loading)
        assertTrue(result[1] is Resource.Success)
        coVerify { remoteDataSource.updateCoursePublication(adminId, courseId, publication) }
    }

    @Test
    fun `deleteCourse elimina curso correctamente`() = runTest {
        // Given
        val adminId = "admin-1"
        val courseId = "course-1"
        coEvery { remoteDataSource.deleteCourse(adminId, courseId) } returns Result.success(Unit)

        // When
        val result = repository.deleteCourse(adminId, courseId).toList()

        // Then
        assertTrue(result[0] is Resource.Loading)
        assertTrue(result[1] is Resource.Success)
        coVerify { remoteDataSource.deleteCourse(adminId, courseId) }
    }

    private fun createCourseDto(
        id: String,
        title: String,
        description: String = "Descripción del curso.",
        requiredPeerReviews: Int = 1
    ) = CourseDto(
        id = id,
        title = title,
        description = description,
        imageUrl = null,
        isPublished = true,
        requiredPeerReviews = requiredPeerReviews
    )
}
