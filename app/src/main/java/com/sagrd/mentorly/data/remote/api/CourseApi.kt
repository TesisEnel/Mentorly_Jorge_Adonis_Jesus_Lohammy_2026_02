package com.sagrd.mentorly.data.remote.api

import com.sagrd.mentorly.data.remote.dto.course.CourseDto
import retrofit2.Response
import retrofit2.http.GET
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
}