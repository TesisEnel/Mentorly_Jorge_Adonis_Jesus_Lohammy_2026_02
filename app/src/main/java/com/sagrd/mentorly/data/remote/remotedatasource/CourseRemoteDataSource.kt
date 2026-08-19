package com.sagrd.mentorly.data.remote.remotedatasource

import com.sagrd.mentorly.data.remote.api.CourseApi
import com.sagrd.mentorly.data.remote.dto.course.CourseDto
import com.sagrd.mentorly.data.remote.dto.course.CreateCourseDto
import com.sagrd.mentorly.data.remote.dto.course.UpdateCourseDto
import com.sagrd.mentorly.data.remote.dto.course.UpdateCoursePublicationDto
import retrofit2.HttpException
import javax.inject.Inject

class CourseRemoteDataSource @Inject constructor(
    private val api: CourseApi
) {
    suspend fun getCourses(): Result<List<CourseDto>> {
        return try {
            val response = api.getCourses()

            if (!response.isSuccessful) {
                Result.failure(Exception("No se pudo completar la solicitud. Intenta de nuevo más tarde."))
            } else {
                Result.success(response.body()!!)
            }
        } catch (exception: HttpException) {
            Result.failure(Exception("Ocurrió un problema al comunicarnos con el servidor. Intenta más tarde.", exception))
        } catch (exception: Exception) {
            Result.failure(Exception("Algo salió mal. Intenta de nuevo más tarde.", exception))
        }
    }

    suspend fun getCourseById(courseId: String): Result<CourseDto> {
        return try {
            val response = api.getCourseById(courseId)

            if (!response.isSuccessful) {
                Result.failure(Exception("No se pudo completar la solicitud. Intenta de nuevo más tarde."))
            } else {
                Result.success(response.body()!!)
            }
        } catch (exception: HttpException) {
            Result.failure(Exception("Ocurrió un problema al comunicarnos con el servidor. Intenta más tarde.", exception))
        } catch (exception: Exception) {
            Result.failure(Exception("Algo salió mal. Intenta de nuevo más tarde.", exception))
        }
    }

    suspend fun getCourseContent(courseId: String): Result<CourseDto> {
        return try {
            val response = api.getCourseContent(courseId)

            if (!response.isSuccessful) {
                Result.failure(Exception("No se pudo completar la solicitud. Intenta de nuevo más tarde."))
            } else {
                Result.success(response.body()!!)
            }
        } catch (exception: HttpException) {
            Result.failure(Exception("Ocurrió un problema al comunicarnos con el servidor. Intenta más tarde.", exception))
        } catch (exception: Exception) {
            Result.failure(Exception("Algo salió mal. Intenta de nuevo más tarde.", exception))
        }
    }

    suspend fun createCourse(
        adminId: String,
        course: CreateCourseDto
    ): Result<CourseDto> {
        return try {
            val response = api.createCourse(adminId, course)

            if (!response.isSuccessful) {
                Result.failure(Exception("No se pudo completar la solicitud. Intenta de nuevo más tarde."))
            } else {
                Result.success(response.body()!!)
            }
        } catch (exception: HttpException) {
            Result.failure(Exception("Ocurrió un problema al comunicarnos con el servidor. Intenta más tarde.", exception))
        } catch (exception: Exception) {
            Result.failure(Exception("Algo salió mal. Intenta de nuevo más tarde.", exception))
        }
    }

    suspend fun updateCourse(
        adminId: String,
        courseId: String,
        course: UpdateCourseDto
    ): Result<Unit> {
        return try {
            val response = api.updateCourse(adminId, courseId, course)

            if (!response.isSuccessful) {
                Result.failure(Exception("No se pudo completar la solicitud. Intenta de nuevo más tarde."))
            } else {
                Result.success(response.body()!!)
            }
        } catch (exception: HttpException) {
            Result.failure(Exception("Ocurrió un problema al comunicarnos con el servidor. Intenta más tarde.", exception))
        } catch (exception: Exception) {
            Result.failure(Exception("Algo salió mal. Intenta de nuevo más tarde.", exception))
        }
    }

    suspend fun updateCoursePublication(
        adminId: String,
        courseId: String,
        publication: UpdateCoursePublicationDto
    ): Result<Unit> {
        return try {
            val response = api.updateCoursePublication(
                adminId,
                courseId,
                publication
            )

            if (!response.isSuccessful) {
                Result.failure(Exception("No se pudo completar la solicitud. Intenta de nuevo más tarde."))
            } else {
                Result.success(response.body()!!)
            }
        } catch (exception: HttpException) {
            Result.failure(Exception("Ocurrió un problema al comunicarnos con el servidor. Intenta más tarde.", exception))
        } catch (exception: Exception) {
            Result.failure(Exception("Algo salió mal. Intenta de nuevo más tarde.", exception))
        }
    }

    suspend fun deleteCourse(
        adminId: String,
        courseId: String
    ): Result<Unit> {
        return try {
            val response = api.deleteCourse(adminId, courseId)

            if (!response.isSuccessful) {
                Result.failure(Exception("No se pudo completar la solicitud. Intenta de nuevo más tarde."))
            } else {
                Result.success(response.body()!!)
            }
        } catch (exception: HttpException) {
            Result.failure(Exception("Ocurrió un problema al comunicarnos con el servidor. Intenta más tarde.", exception))
        } catch (exception: Exception) {
            Result.failure(Exception("Algo salió mal. Intenta de nuevo más tarde.", exception))
        }
    }
}