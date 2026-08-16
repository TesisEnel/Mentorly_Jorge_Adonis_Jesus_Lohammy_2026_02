package com.sagrd.mentorly.data.remote.api

import com.sagrd.mentorly.data.remote.dto.progress.EnrollmentProgressDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface EnrollmentProgressApi {

    @GET("api/enrollments/{enrollmentId}/progress")
    suspend fun getEnrollmentProgress(
        @Path("enrollmentId") enrollmentId: String
    ): Response<EnrollmentProgressDto>

    @GET("api/admins/{adminId}/enrollments/{enrollmentId}/progress")
    suspend fun getAdminEnrollmentProgress(
        @Path("adminId") adminId: String,
        @Path("enrollmentId") enrollmentId: String
    ): Response<EnrollmentProgressDto>

    @POST("api/enrollments/{enrollmentId}/themes/{themeId}/complete")
    suspend fun completeTheme(
        @Path("enrollmentId") enrollmentId: String,
        @Path("themeId") themeId: String
    ): Response<EnrollmentProgressDto>
}
