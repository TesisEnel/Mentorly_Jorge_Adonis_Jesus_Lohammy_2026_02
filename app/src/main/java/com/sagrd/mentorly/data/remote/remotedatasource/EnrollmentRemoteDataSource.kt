package com.sagrd.mentorly.data.remote.remotedatasource

import com.sagrd.mentorly.data.remote.api.EnrollmentApi
import com.sagrd.mentorly.data.remote.dto.enrollment.CertificateDto
import com.sagrd.mentorly.data.remote.dto.enrollment.CreateEnrollmentDto
import com.sagrd.mentorly.data.remote.dto.enrollment.EnrollmentDto
import com.sagrd.mentorly.data.remote.dto.enrollment.EnrollmentResultDto
import com.sagrd.mentorly.data.remote.dto.enrollment.EnrollmentStatusDto
import retrofit2.HttpException
import javax.inject.Inject

class EnrollmentRemoteDataSource @Inject constructor(
    private val api: EnrollmentApi
) {

    suspend fun createEnrollment(
        studentId: String,
        enrollment: CreateEnrollmentDto
    ): Result<EnrollmentResultDto> {
        return try {
            val response = api.createEnrollment(studentId, enrollment)

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

    suspend fun getEnrollments(studentId: String): Result<List<EnrollmentDto>> {
        return try {
            val response = api.getEnrollments(studentId)

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

    suspend fun getAdminStudentEnrollments(
        adminId: String,
        studentId: String
    ): Result<List<EnrollmentDto>> {
        return try {
            val response = api.getAdminStudentEnrollments(adminId, studentId)

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

    suspend fun getEnrollmentById(enrollmentId: String): Result<EnrollmentDto> {
        return try {
            val response = api.getEnrollmentById(enrollmentId)

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

    suspend fun restartEnrollment(
        studentId: String,
        courseId: String
    ): Result<EnrollmentResultDto> {
        return try {
            val response = api.restartEnrollment(studentId, courseId)

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

    suspend fun getEnrollmentStatus(
        enrollmentId: String
    ): Result<EnrollmentStatusDto> {
        return try {
            val response = api.getEnrollmentStatus(enrollmentId)

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

    suspend fun getCertificate(enrollmentId: String): Result<CertificateDto> {
        return try {
            val response = api.getCertificate(enrollmentId)

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
