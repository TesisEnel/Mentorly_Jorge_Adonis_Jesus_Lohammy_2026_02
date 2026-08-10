package com.sagrd.mentorly.data.remote.api

import com.sagrd.mentorly.data.remote.dto.enrollment.CertificateDto
import com.sagrd.mentorly.data.remote.dto.enrollment.CreateEnrollmentDto
import com.sagrd.mentorly.data.remote.dto.enrollment.EnrollmentDto
import com.sagrd.mentorly.data.remote.dto.enrollment.EnrollmentResultDto
import com.sagrd.mentorly.data.remote.dto.enrollment.EnrollmentStatusDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface EnrollmentApi {

    @POST("api/students/{studentId}/enrollments")
    suspend fun createEnrollment(
        @Path("studentId") studentId: String,
        @Body enrollment: CreateEnrollmentDto
    ): Response<EnrollmentResultDto>

    @GET("api/students/{studentId}/enrollments")
    suspend fun getEnrollments(
        @Path("studentId") studentId: String
    ): Response<List<EnrollmentDto>>

    @GET("api/enrollments/{enrollmentId}")
    suspend fun getEnrollmentById(
        @Path("enrollmentId") enrollmentId: String
    ): Response<EnrollmentDto>

    @POST("api/students/{studentId}/courses/{courseId}/enrollments/restart")
    suspend fun restartEnrollment(
        @Path("studentId") studentId: String,
        @Path("courseId") courseId: String
    ): Response<EnrollmentResultDto>

    @GET("api/enrollments/{enrollmentId}/status")
    suspend fun getEnrollmentStatus(
        @Path("enrollmentId") enrollmentId: String
    ): Response<EnrollmentStatusDto>

    @GET("api/enrollments/{enrollmentId}/certificate")
    suspend fun getCertificate(
        @Path("enrollmentId") enrollmentId: String
    ): Response<CertificateDto>
}
