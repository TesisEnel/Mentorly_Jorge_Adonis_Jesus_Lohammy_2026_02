package com.sagrd.mentorly.data.remote.remotedatasource

import com.sagrd.mentorly.data.remote.api.SubmissionApi
import com.sagrd.mentorly.data.remote.dto.submission.AdminSubmissionDecisionDto
import com.sagrd.mentorly.data.remote.dto.submission.CreateSubmissionDto
import com.sagrd.mentorly.data.remote.dto.submission.SubmissionDto
import com.sagrd.mentorly.data.remote.dto.submission.SubmissionReviewDto
import com.sagrd.mentorly.data.remote.dto.submission.UpdateSubmissionDto
import retrofit2.HttpException
import javax.inject.Inject

class SubmissionRemoteDataSource @Inject constructor(
    private val api: SubmissionApi
) {

    suspend fun createSubmission(
        enrollmentId: String,
        activityId: String,
        submission: CreateSubmissionDto
    ): Result<SubmissionDto> {
        return try {
            val response = api.createSubmission(enrollmentId, activityId, submission)

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

    suspend fun updateSubmission(
        submissionId: String,
        submission: UpdateSubmissionDto
    ): Result<Unit> {
        return try {
            val response = api.updateSubmission(submissionId, submission)

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

    suspend fun getSubmissionById(submissionId: String): Result<SubmissionDto> {
        return try {
            val response = api.getSubmissionById(submissionId)

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

    suspend fun getSubmissionsByStudentId(
        studentId: String
    ): Result<List<SubmissionDto>> {
        return try {
            val response = api.getSubmissionsByStudentId(studentId)

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

    suspend fun getSubmissionReviews(
        studentId: String,
        submissionId: String
    ): Result<List<SubmissionReviewDto>> {
        return try {
            val response = api.getSubmissionReviews(studentId, submissionId)

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

    suspend fun escalateSubmission(
        studentId: String,
        submissionId: String
    ): Result<Unit> {
        return try {
            val response = api.escalateSubmission(studentId, submissionId)

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

    suspend fun decideSubmission(
        adminId: String,
        submissionId: String,
        decision: AdminSubmissionDecisionDto
    ): Result<Unit> {
        return try {
            val response = api.decideSubmission(adminId, submissionId, decision)

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
