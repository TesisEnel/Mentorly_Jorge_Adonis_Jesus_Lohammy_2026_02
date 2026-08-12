package com.sagrd.mentorly.data.remote.remotedatasource

import com.sagrd.mentorly.data.remote.api.AnalyticsApi
import javax.inject.Inject

class AnalyticsRemoteDataSource @Inject constructor(
    private val api: AnalyticsApi
) {
    suspend fun getOverview(adminId: String) = try {
        val response = api.getOverview(adminId)
        if (response.isSuccessful) {
            Result.success(response.body()!!)
        } else {
            Result.failure(Exception(response.message()))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getDropOff(adminId: String, courseId: String) = try {
        val response = api.getDropOff(adminId, courseId)
        if (response.isSuccessful) {
            Result.success(response.body() ?: emptyList())
        } else {
            Result.failure(Exception(response.message()))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getCompletionTimeReport(adminId: String, courseId: String) = try {
        val response = api.getCompletionTimeReport(adminId, courseId)
        if (response.isSuccessful) {
            Result.success(response.body()!!)
        } else {
            Result.failure(Exception(response.message()))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getBottlenecks(adminId: String, courseId: String) = try {
        val response = api.getBottlenecks(adminId, courseId)
        if (response.isSuccessful) {
            Result.success(response.body() ?: emptyList())
        } else {
            Result.failure(Exception(response.message()))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getEnrollmentHistory(adminId: String, courseId: String) = try {
        val response = api.getEnrollmentHistory(adminId, courseId)
        if (response.isSuccessful) {
            Result.success(response.body() ?: emptyList())
        } else {
            Result.failure(Exception(response.message()))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}
