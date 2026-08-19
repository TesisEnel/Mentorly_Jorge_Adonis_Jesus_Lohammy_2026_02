package com.sagrd.mentorly.data.remote.remotedatasource

import com.sagrd.mentorly.data.remote.api.StudentApi
import com.sagrd.mentorly.data.remote.dto.student.ProvisionStudentDto
import com.sagrd.mentorly.data.remote.dto.student.StudentDto
import com.sagrd.mentorly.data.remote.dto.student.StudentStatisticsDto
import com.sagrd.mentorly.data.remote.dto.student.UpdateLeaderboardPrivacyDto
import com.sagrd.mentorly.data.remote.dto.student.UpdateStudentDto
import retrofit2.HttpException
import javax.inject.Inject

class StudentRemoteDataSource @Inject constructor(
    private val api: StudentApi
) {
    suspend fun getStudents(): Result<List<StudentDto>> {
        return try {
            val response = api.getStudents()

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

    suspend fun getStudentById(studentId: String): Result<StudentDto> {
        return try {
            val response = api.getStudentById(studentId)

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

    suspend fun provisionStudent(
        student: ProvisionStudentDto
    ): Result<StudentDto> {
        return try {
            val response = api.provisionStudent(student)

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

    suspend fun updateStudent(
        studentId: String,
        student: UpdateStudentDto
    ): Result<Unit> {
        return try {
            val response = api.updateStudent(studentId, student)

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

    suspend fun updateLeaderboardPrivacy(
        studentId: String,
        privacy: UpdateLeaderboardPrivacyDto
    ): Result<Unit> {
        return try {
            val response = api.updateLeaderboardPrivacy(studentId, privacy)

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

    suspend fun getStudentStatistics(
        studentId: String
    ): Result<StudentStatisticsDto> {
        return try {
            val response = api.getStudentStatistics(studentId)

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

    suspend fun promoteToAdmin(
        adminId: String,
        studentId: String
    ): Result<Unit> {
        return try {
            val response = api.promoteToAdmin(adminId, studentId)

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