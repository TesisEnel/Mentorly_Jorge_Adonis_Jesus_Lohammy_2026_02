package com.sagrd.mentorly.data.remote.api

import com.sagrd.mentorly.data.remote.dto.submission.AnonymousSubmissionDto
import com.sagrd.mentorly.data.remote.dto.peerreview.CreatePeerReviewRequestDto
import com.sagrd.mentorly.data.remote.dto.peerreview.CreatePeerReviewRubricCriterionDto
import com.sagrd.mentorly.data.remote.dto.peerreview.PeerReviewAuditDto
import com.sagrd.mentorly.data.remote.dto.peerreview.PeerReviewRubricCriterionDto
import com.sagrd.mentorly.data.remote.dto.peerreview.PeerReviewDto
import com.sagrd.mentorly.data.remote.dto.peerreview.PeerReviewResultDto
import com.sagrd.mentorly.data.remote.dto.peerreview.ReviewQueueItemDto
import com.sagrd.mentorly.data.remote.dto.peerreview.UpdatePeerReviewRubricCriterionDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface PeerReviewApi {

    @GET("api/activities/{activityId}/peer-review-rubric")
    suspend fun getRubric(
        @Path("activityId") activityId: String
    ): Response<List<PeerReviewRubricCriterionDto>>

    @POST("api/admins/{adminId}/activities/{activityId}/peer-review-rubric/criteria")
    suspend fun createRubricCriterion(
        @Path("adminId") adminId: String,
        @Path("activityId") activityId: String,
        @Body dto: CreatePeerReviewRubricCriterionDto
    ): Response<PeerReviewRubricCriterionDto>

    @PUT("api/admins/{adminId}/peer-review-rubric/criteria/{criterionId}")
    suspend fun updateRubricCriterion(
        @Path("adminId") adminId: String,
        @Path("criterionId") criterionId: String,
        @Body dto: UpdatePeerReviewRubricCriterionDto
    ): Response<Unit>

    @DELETE("api/admins/{adminId}/peer-review-rubric/criteria/{criterionId}")
    suspend fun deleteRubricCriterion(
        @Path("adminId") adminId: String,
        @Path("criterionId") criterionId: String
    ): Response<Unit>

    @GET("api/students/{studentId}/peer-review-queue")
    suspend fun getQueue(
        @Path("studentId") studentId: String
    ): Response<List<ReviewQueueItemDto>>

    @GET("api/students/{studentId}/peer-reviews")
    suspend fun getMyReviews(
        @Path("studentId") studentId: String
    ): Response<List<PeerReviewDto>>

    @GET("api/students/{studentId}/peer-review-queue/{submissionId}")
    suspend fun getAnonymousSubmission(
        @Path("studentId") studentId: String,
        @Path("submissionId") submissionId: String
    ): Response<AnonymousSubmissionDto>

    @GET("api/admins/{adminId}/peer-reviews/{peerReviewId}/audit")
    suspend fun getAudit(
        @Path("adminId") adminId: String,
        @Path("peerReviewId") peerReviewId: String
    ): Response<PeerReviewAuditDto>

    @GET("api/admins/{adminId}/peer-reviews")
    suspend fun getAllPeerReviews(
        @Path("adminId") adminId: String
    ): Response<List<PeerReviewDto>>

    @POST("api/students/{studentId}/peer-reviews")
    suspend fun submitReview(
        @Path("studentId") studentId: String,
        @Body dto: CreatePeerReviewRequestDto
    ): Response<PeerReviewResultDto>
}
