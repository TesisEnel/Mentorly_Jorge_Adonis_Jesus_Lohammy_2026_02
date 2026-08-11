package com.sagrd.mentorly.data.remote.remotedatasource

import com.sagrd.mentorly.data.remote.api.ThemeApi
import com.sagrd.mentorly.data.remote.dto.common.ReorderItemsDto
import com.sagrd.mentorly.data.remote.dto.theme.CreateThemeDto
import com.sagrd.mentorly.data.remote.dto.theme.ThemeDto
import com.sagrd.mentorly.data.remote.dto.theme.UpdateThemeDto
import retrofit2.HttpException
import javax.inject.Inject

class ThemeRemoteDataSource @Inject constructor(
    private val api: ThemeApi
) {
    suspend fun getThemesByUnit(unitId: String): Result<List<ThemeDto>> {
        return try {
            val response = api.getThemesByUnit(unitId)
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

    suspend fun createTheme(
        adminId: String,
        unitId: String,
        dto: CreateThemeDto
    ): Result<ThemeDto> {
        return try {
            val response = api.createTheme(adminId, unitId, dto)
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

    suspend fun updateTheme(
        adminId: String,
        themeId: String,
        dto: UpdateThemeDto
    ): Result<Unit> {
        return try {
            val response = api.updateTheme(adminId, themeId, dto)
            if (!response.isSuccessful) {
                Result.failure(Exception("Error de red ${response.code()}"))
            } else {
                Result.success(Unit)
            }
        } catch (exception: HttpException) {
            Result.failure(Exception("Error de servidor", exception))
        } catch (exception: Exception) {
            Result.failure(Exception("Error desconocido", exception))
        }
    }

    suspend fun deleteTheme(adminId: String, themeId: String): Result<Unit> {
        return try {
            val response = api.deleteTheme(adminId, themeId)
            if (!response.isSuccessful) {
                Result.failure(Exception("Error de red ${response.code()}"))
            } else {
                Result.success(Unit)
            }
        } catch (exception: HttpException) {
            Result.failure(Exception("Error de servidor", exception))
        } catch (exception: Exception) {
            Result.failure(Exception("Error desconocido", exception))
        }
    }

    suspend fun reorderThemes(
        adminId: String,
        unitId: String,
        dto: ReorderItemsDto
    ): Result<Unit> {
        return try {
            val response = api.reorderThemes(adminId, unitId, dto)
            if (!response.isSuccessful) {
                Result.failure(Exception("Error de red ${response.code()}"))
            } else {
                Result.success(Unit)
            }
        } catch (exception: HttpException) {
            Result.failure(Exception("Error de servidor", exception))
        } catch (exception: Exception) {
            Result.failure(Exception("Error desconocido", exception))
        }
    }
}
