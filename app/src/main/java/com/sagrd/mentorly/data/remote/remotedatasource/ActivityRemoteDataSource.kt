package com.sagrd.mentorly.data.remote.remotedatasource

import com.sagrd.mentorly.data.remote.api.ActivityApi
import com.sagrd.mentorly.data.remote.dto.activity.CreateActivityDto
import com.sagrd.mentorly.data.remote.dto.activity.UpdateActivityDto
import com.sagrd.mentorly.data.remote.dto.common.ReorderItemsDto
import com.sagrd.mentorly.data.remote.dto.content.ActivityDto
import retrofit2.HttpException
import javax.inject.Inject

class ActivityRemoteDataSource @Inject constructor(
    private val api: ActivityApi
) {

    suspend fun getActivities(themeId: String): Result<List<ActivityDto>> {
        return try {
            val response = api.getActivities(themeId)

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

    suspend fun createActivity(
        adminId: String,
        themeId: String,
        activity: CreateActivityDto
    ): Result<ActivityDto> {
        return try {
            val response = api.createActivity(adminId, themeId, activity)

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

    suspend fun updateActivity(
        adminId: String,
        activityId: String,
        activity: UpdateActivityDto
    ): Result<Unit> {
        return try {
            val response = api.updateActivity(adminId, activityId, activity)

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

    suspend fun deleteActivity(
        adminId: String,
        activityId: String
    ): Result<Unit> {
        return try {
            val response = api.deleteActivity(adminId, activityId)

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

    suspend fun reorderActivities(
        adminId: String,
        themeId: String,
        items: ReorderItemsDto
    ): Result<Unit> {
        return try {
            val response = api.reorderActivities(adminId, themeId, items)

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
}
