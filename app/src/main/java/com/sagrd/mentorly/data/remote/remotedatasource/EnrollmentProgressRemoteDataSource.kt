package com.sagrd.mentorly.data.remote.remotedatasource

import com.sagrd.mentorly.data.remote.api.EnrollmentProgressApi
import com.sagrd.mentorly.data.remote.dto.progress.EnrollmentProgressDto
import retrofit2.HttpException
import javax.inject.Inject

class EnrollmentProgressRemoteDataSource @Inject constructor(
    private val api: EnrollmentProgressApi
) {

    suspend fun getEnrollmentProgress(
        enrollmentId: String
    ): Result<EnrollmentProgressDto> {
        return try {
            val response = api.getEnrollmentProgress(enrollmentId)

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

    suspend fun getAdminEnrollmentProgress(
        adminId: String,
        enrollmentId: String
    ): Result<EnrollmentProgressDto> {
        return try {
            val response = api.getAdminEnrollmentProgress(adminId, enrollmentId)

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

    suspend fun completeTheme(
        enrollmentId: String,
        themeId: String
    ): Result<EnrollmentProgressDto> {
        return try {
            val response = api.completeTheme(enrollmentId, themeId)

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
