package com.sagrd.mentorly.data.remote.api

import com.sagrd.mentorly.data.remote.dto.submission.AdminSubmissionDecisionDto
import com.sagrd.mentorly.data.remote.dto.submission.AdminEscalatedSubmissionDto
import com.sagrd.mentorly.data.remote.dto.submission.AdminSubmissionAuditDto
import com.sagrd.mentorly.data.remote.dto.submission.CreateSubmissionDto
import com.sagrd.mentorly.data.remote.dto.submission.SubmissionDto
import com.sagrd.mentorly.data.remote.dto.submission.SubmissionReviewDto
import com.sagrd.mentorly.data.remote.dto.submission.UpdateSubmissionDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface SubmissionApi {

    @GET("api/admins/{adminId}/submissions/escalated")
    suspend fun getEscalatedSubmissions(
        @Path("adminId") adminId: String
    ): Response<List<AdminEscalatedSubmissionDto>>

    @GET("api/admins/{adminId}/submissions/{submissionId}/audit")
    suspend fun getEscalatedSubmissionAudit(
        @Path("adminId") adminId: String,
        @Path("submissionId") submissionId: String
    ): Response<AdminSubmissionAuditDto>

    @POST("api/enrollments/{enrollmentId}/activities/{activityId}/submissions")
    suspend fun createSubmission(
        @Path("enrollmentId") enrollmentId: String,
        @Path("activityId") activityId: String,
        @Body submission: CreateSubmissionDto
    ): Response<SubmissionDto>

    @PUT("api/submissions/{submissionId}")
    suspend fun updateSubmission(
        @Path("submissionId") submissionId: String,
        @Body submission: UpdateSubmissionDto
    ): Response<Unit>

    @GET("api/submissions/{submissionId}")
    suspend fun getSubmissionById(
        @Path("submissionId") submissionId: String
    ): Response<SubmissionDto>

    @GET("api/students/{studentId}/submissions")
    suspend fun getSubmissionsByStudentId(
        @Path("studentId") studentId: String
    ): Response<List<SubmissionDto>>

    @GET("api/students/{studentId}/submissions/{submissionId}/reviews")
    suspend fun getSubmissionReviews(
        @Path("studentId") studentId: String,
        @Path("submissionId") submissionId: String
    ): Response<List<SubmissionReviewDto>>

    @POST("api/students/{studentId}/submissions/{submissionId}/escalate")
    suspend fun escalateSubmission(
        @Path("studentId") studentId: String,
        @Path("submissionId") submissionId: String
    ): Response<Unit>

    @POST("api/admins/{adminId}/submissions/{submissionId}/decision")
    suspend fun decideSubmission(
        @Path("adminId") adminId: String,
        @Path("submissionId") submissionId: String,
        @Body decision: AdminSubmissionDecisionDto
    ): Response<Unit>
}
