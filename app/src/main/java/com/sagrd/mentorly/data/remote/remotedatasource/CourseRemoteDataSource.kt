package com.sagrd.mentorly.data.remote.remotedatasource

import com.sagrd.mentorly.data.remote.api.CourseApi
import com.sagrd.mentorly.data.remote.dto.course.CourseDto
import retrofit2.HttpException
import javax.inject.Inject

class CourseRemoteDataSource @Inject constructor(
    private val api: CourseApi
) {
    suspend fun getCourses(): Result<List<CourseDto>> {
        return try {
            val response = api.getCourses()

            if (!response.isSuccessful) {
                Result.failure(Exception("Error de red ${response.code()}"))
            } else {
                Result.success(response.body()!!)
            }
        } catch (exception: HttpException) {
            Result.failure(Exception("Error de servidor", exception))
        } catch (exception: Exception) {
            Result.failure(Exception("Error desconocido", exception))
        }
    }

    suspend fun getCourseById(courseId: String): Result<CourseDto> {
        return try {
            val response = api.getCourseById(courseId)

            if (!response.isSuccessful) {
                Result.failure(Exception("Error de red ${response.code()}"))
            } else {
                Result.success(response.body()!!)
            }
        } catch (exception: HttpException) {
            Result.failure(Exception("Error de servidor", exception))
        } catch (exception: Exception) {
            Result.failure(Exception("Error desconocido", exception))
        }
    }

    suspend fun getCourseContent(courseId: String): Result<CourseDto> {
        return try {
            val response = api.getCourseContent(courseId)

            if (!response.isSuccessful) {
                Result.failure(Exception("Error de red ${response.code()}"))
            } else {
                Result.success(response.body()!!)
            }
        } catch (exception: HttpException) {
            Result.failure(Exception("Error de servidor", exception))
        } catch (exception: Exception) {
            Result.failure(Exception("Error desconocido", exception))
        }
    }
}