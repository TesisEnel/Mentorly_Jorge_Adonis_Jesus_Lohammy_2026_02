package com.sagrd.mentorly.data.repository.course

import com.sagrd.mentorly.data.remote.Resource
import com.sagrd.mentorly.data.remote.dto.course.CreateCourseDto
import com.sagrd.mentorly.data.remote.dto.course.UpdateCourseDto
import com.sagrd.mentorly.data.remote.dto.course.UpdateCoursePublicationDto
import com.sagrd.mentorly.data.remote.remotedatasource.CourseRemoteDataSource
import com.sagrd.mentorly.domain.model.course.Course
import com.sagrd.mentorly.domain.repository.course.CourseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class CourseRepositoryImpl @Inject constructor(
    private val remoteDataSource: CourseRemoteDataSource
) : CourseRepository {
    override fun getCourses(): Flow<Resource<List<Course>>> = flow {
        emit(Resource.Loading())
        remoteDataSource.getCourses()
            .onSuccess { c ->
                emit(Resource.Success(c.map { it.toDomain() }))
            }
            .onFailure {
                emit(Resource.Error(it.message ?: "No se pudieron cargar los cursos"))
            }
    }

    override fun getCourseById(
        courseId: String
    ): Flow<Resource<Course>> = flow {
        emit(Resource.Loading())

        remoteDataSource.getCourseById(courseId)
            .onSuccess { course ->
                emit(Resource.Success(course.toDomain()))
            }
            .onFailure {
                emit(Resource.Error(it.message ?: "No se pudo cargar el curso."))
            }
    }

    override fun getCourseContent(
        courseId: String
    ): Flow<Resource<Course>> = flow {
        emit(Resource.Loading())

        remoteDataSource.getCourseContent(courseId)
            .onSuccess { course ->
                emit(Resource.Success(course.toDomain()))
            }
            .onFailure {
                emit(Resource.Error(it.message ?: "No se pudo cargar el contenido del curso."))
            }
    }

    override fun createCourse(
        adminId: String,
        course: CreateCourseDto
    ): Flow<Resource<Course>> = flow {
        emit(Resource.Loading())

        remoteDataSource.createCourse(adminId, course)
            .onSuccess { createdCourse ->
                emit(Resource.Success(createdCourse.toDomain()))
            }
            .onFailure {
                emit(Resource.Error(it.message ?: "No se pudo crear el curso."))
            }
    }

    override fun updateCourse(
        adminId: String,
        courseId: String,
        course: UpdateCourseDto
    ): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())

        remoteDataSource.updateCourse(adminId, courseId, course)
            .onSuccess {
                emit(Resource.Success(Unit))
            }
            .onFailure {
                emit(Resource.Error(it.message ?: "No se pudo actualizar el curso."))
            }
    }

    override fun updateCoursePublication(
        adminId: String,
        courseId: String,
        publication: UpdateCoursePublicationDto
    ): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())

        remoteDataSource.updateCoursePublication(
            adminId,
            courseId,
            publication
        ).onSuccess {
            emit(Resource.Success(Unit))
        }.onFailure {
            emit(Resource.Error(it.message ?: "No se pudo actualizar la publicación del curso."))
        }
    }

    override fun deleteCourse(
        adminId: String,
        courseId: String
    ): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())

        remoteDataSource.deleteCourse(adminId, courseId)
            .onSuccess {
                emit(Resource.Success(Unit))
            }
            .onFailure {
                emit(Resource.Error(it.message ?: "No se pudo eliminar el curso."))
            }
    }
}