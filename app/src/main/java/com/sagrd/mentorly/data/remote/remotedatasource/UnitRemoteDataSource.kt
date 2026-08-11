package com.sagrd.mentorly.data.remote.remotedatasource

import com.sagrd.mentorly.data.remote.api.UnitApi
import com.sagrd.mentorly.data.remote.dto.common.ReorderItemsDto
import com.sagrd.mentorly.data.remote.dto.content.CourseUnitDto
import com.sagrd.mentorly.data.remote.dto.unit.CreateUnitDto
import com.sagrd.mentorly.data.remote.dto.unit.UpdateUnitDto
import retrofit2.HttpException
import javax.inject.Inject

class UnitRemoteDataSource @Inject constructor(
    private val api: UnitApi
) {

    suspend fun getUnitsByCourseId(courseId: String): Result<List<CourseUnitDto>> {
        return try {
            val response = api.getUnitsByCourseId(courseId)

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

    suspend fun createUnit(
        adminId: String,
        courseId: String,
        unit: CreateUnitDto
    ): Result<CourseUnitDto> {
        return try {
            val response = api.createUnit(adminId, courseId, unit)

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

    suspend fun updateUnit(
        adminId: String,
        unitId: String,
        unit: UpdateUnitDto
    ): Result<Unit> {
        return try {
            val response = api.updateUnit(adminId, unitId, unit)

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

    suspend fun deleteUnit(
        adminId: String,
        unitId: String
    ): Result<Unit> {
        return try {
            val response = api.deleteUnit(adminId, unitId)

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

    suspend fun reorderUnits(
        adminId: String,
        courseId: String,
        reorder: ReorderItemsDto
    ): Result<Unit> {
        return try {
            val response = api.reorderUnits(adminId, courseId, reorder)

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
