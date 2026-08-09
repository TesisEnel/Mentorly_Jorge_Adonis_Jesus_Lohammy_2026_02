package com.sagrd.mentorly.data.remote.api

import com.sagrd.mentorly.data.remote.dto.student.ProvisionStudentDto
import com.sagrd.mentorly.data.remote.dto.student.StudentDto
import com.sagrd.mentorly.data.remote.dto.student.StudentStatisticsDto
import com.sagrd.mentorly.data.remote.dto.student.UpdateLeaderboardPrivacyDto
import com.sagrd.mentorly.data.remote.dto.student.UpdateStudentDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface StudentApi {

    @GET("api/students")
    suspend fun getStudents(): Response<List<StudentDto>>

    @GET("api/students/{studentId}")
    suspend fun getStudentById(
        @Path("studentId") studentId: String
    ): Response<StudentDto>

    @POST("api/students/provision")
    suspend fun provisionStudent(
        @Body student: ProvisionStudentDto
    ): Response<StudentDto>

    @PUT("api/students/{studentId}")
    suspend fun updateStudent(
        @Path("studentId") studentId: String,
        @Body student: UpdateStudentDto
    ): Response<Unit>

    @PATCH("api/students/{studentId}/privacy")
    suspend fun updateLeaderboardPrivacy(
        @Path("studentId") studentId: String,
        @Body privacy: UpdateLeaderboardPrivacyDto
    ): Response<Unit>

    @GET("api/students/{studentId}/statistics")
    suspend fun getStudentStatistics(
        @Path("studentId") studentId: String
    ): Response<StudentStatisticsDto>

    @POST("api/admins/{adminId}/students/{studentId}/promote")
    suspend fun promoteToAdmin(
        @Path("adminId") adminId: String,
        @Path("studentId") studentId: String
    ): Response<Unit>
}