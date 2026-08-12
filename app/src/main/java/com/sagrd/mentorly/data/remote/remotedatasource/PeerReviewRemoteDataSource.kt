package com.sagrd.mentorly.data.remote.remotedatasource

import com.sagrd.mentorly.data.remote.api.PeerReviewApi
import com.sagrd.mentorly.data.remote.dto.submission.AnonymousSubmissionDto
import com.sagrd.mentorly.data.remote.dto.peerreview.CreatePeerReviewRequestDto
import com.sagrd.mentorly.data.remote.dto.peerreview.PeerReviewAuditDto
import com.sagrd.mentorly.data.remote.dto.peerreview.PeerReviewDto
import com.sagrd.mentorly.data.remote.dto.peerreview.PeerReviewResultDto
import com.sagrd.mentorly.data.remote.dto.peerreview.ReviewQueueItemDto
import retrofit2.HttpException
import javax.inject.Inject

class PeerReviewRemoteDataSource @Inject constructor(
    private val api: PeerReviewApi
) {
    suspend fun getQueue(studentId: String): Result<List<ReviewQueueItemDto>> {
        return try {
            val response = api.getQueue(studentId)
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

    suspend fun getMyReviews(studentId: String): Result<List<PeerReviewDto>> {
        return try {
            val response = api.getMyReviews(studentId)
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

    suspend fun getAnonymousSubmission(
        studentId: String,
        submissionId: String
    ): Result<AnonymousSubmissionDto> {
        return try {
            val response = api.getAnonymousSubmission(studentId, submissionId)
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

    suspend fun getAudit(
        adminId: String,
        peerReviewId: String
    ): Result<PeerReviewAuditDto> {
        return try {
            val response = api.getAudit(adminId, peerReviewId)
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

    suspend fun getAllPeerReviews(adminId: String): Result<List<PeerReviewDto>> {
        return try {
            val response = api.getAllPeerReviews(adminId)
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

    suspend fun submitReview(
        studentId: String,
        dto: CreatePeerReviewRequestDto
    ): Result<PeerReviewResultDto> {
        return try {
            val response = api.submitReview(studentId, dto)
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
}