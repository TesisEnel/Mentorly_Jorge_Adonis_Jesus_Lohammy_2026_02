package com.sagrd.mentorly.data.remote.remotedatasource

import com.sagrd.mentorly.data.remote.api.AnalyticsApi
import retrofit2.HttpException
import javax.inject.Inject

class AnalyticsRemoteDataSource @Inject constructor(
    private val api: AnalyticsApi
) {
    suspend fun getOverview(adminId: String) = try {
        val response = api.getOverview(adminId)
        if (!response.isSuccessful) {
            Result.failure(Exception("No se pudo conectar con el servidor. Verifica tu conexión e intenta de nuevo."))
        } else {
            Result.success(Unit)
        }
    } catch (exception: HttpException) {
        Result.failure(Exception("Ocurrió un problema al comunicarnos con el servidor. Intenta más tarde."))
    } catch (exception: Exception) {
        Result.failure(Exception("Algo salió mal. Intenta de nuevo más tarde."))
    }

    suspend fun getDropOff(adminId: String, courseId: String) = try {
        val response = api.getDropOff(adminId, courseId)
        if (!response.isSuccessful) {
            Result.failure(Exception("No se pudo conectar con el servidor. Verifica tu conexión e intenta de nuevo."))
        } else {
            Result.success(Unit)
        }
    } catch (exception: HttpException) {
        Result.failure(Exception("Ocurrió un problema al comunicarnos con el servidor. Intenta más tarde."))
    } catch (exception: Exception) {
        Result.failure(Exception("Algo salió mal. Intenta de nuevo más tarde."))
    }

    suspend fun getCompletionTimeReport(adminId: String, courseId: String) = try {
        val response = api.getCompletionTimeReport(adminId, courseId)
        if (!response.isSuccessful) {
            Result.failure(Exception("No se pudo conectar con el servidor. Verifica tu conexión e intenta de nuevo."))
        } else {
            Result.success(Unit)
        }
    } catch (exception: HttpException) {
        Result.failure(Exception("Ocurrió un problema al comunicarnos con el servidor. Intenta más tarde."))
    } catch (exception: Exception) {
        Result.failure(Exception("Algo salió mal. Intenta de nuevo más tarde."))
    }

    suspend fun getBottlenecks(adminId: String, courseId: String) = try {
        val response = api.getBottlenecks(adminId, courseId)
        if (!response.isSuccessful) {
            Result.failure(Exception("No se pudo conectar con el servidor. Verifica tu conexión e intenta de nuevo."))
        } else {
            Result.success(Unit)
        }
    } catch (exception: HttpException) {
        Result.failure(Exception("Ocurrió un problema al comunicarnos con el servidor. Intenta más tarde."))
    } catch (exception: Exception) {
        Result.failure(Exception("Algo salió mal. Intenta de nuevo más tarde."))
    }

    suspend fun getEnrollmentHistory(adminId: String, courseId: String) = try {
        val response = api.getEnrollmentHistory(adminId, courseId)
        if (!response.isSuccessful) {
            Result.failure(Exception("No se pudo conectar con el servidor. Verifica tu conexión e intenta de nuevo."))
        } else {
            Result.success(Unit)
        }
    } catch (exception: HttpException) {
        Result.failure(Exception("Ocurrió un problema al comunicarnos con el servidor. Intenta más tarde."))
    } catch (exception: Exception) {
        Result.failure(Exception("Algo salió mal. Intenta de nuevo más tarde."))
    }
}
