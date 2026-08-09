package com.sagrd.mentorly.data.repository.course

import com.sagrd.mentorly.data.remote.Resource
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
}