package com.sagrd.mentorly.data.remote.api

import com.sagrd.mentorly.data.remote.dto.course.CourseDto
import com.sagrd.mentorly.data.remote.dto.course.CreateCourseDto
import com.sagrd.mentorly.data.remote.dto.course.UpdateCourseDto
import com.sagrd.mentorly.data.remote.dto.course.UpdateCoursePublicationDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface CourseApi {
    @GET("api/Courses")
    suspend fun getCourses(): Response<List<CourseDto>>

    @GET("api/courses/{courseId}")
    suspend fun getCourseById(
        @Path("courseId") courseId: String
    ): Response<CourseDto>

    @GET("api/courses/{courseId}/content")
    suspend fun getCourseContent(
        @Path("courseId") courseId: String
    ): Response<CourseDto>

    @POST("api/admins/{adminId}/courses")
    suspend fun createCourse(
        @Path("adminId") adminId: String,
        @Body course: CreateCourseDto
    ): Response<CourseDto>

    @PUT("api/admins/{adminId}/courses/{courseId}")
    suspend fun updateCourse(
        @Path("adminId") adminId: String,
        @Path("courseId") courseId: String,
        @Body course: UpdateCourseDto
    ): Response<Unit>

    @PATCH("api/admins/{adminId}/courses/{courseId}/publication")
    suspend fun updateCoursePublication(
        @Path("adminId") adminId: String,
        @Path("courseId") courseId: String,
        @Body publication: UpdateCoursePublicationDto
    ): Response<Unit>

    @DELETE("api/admins/{adminId}/courses/{courseId}")
    suspend fun deleteCourse(
        @Path("adminId") adminId: String,
        @Path("courseId") courseId: String
    ): Response<Unit>
}