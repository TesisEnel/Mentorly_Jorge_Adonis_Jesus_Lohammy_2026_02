package com.sagrd.mentorly.data.remote.api

import com.sagrd.mentorly.data.remote.dto.course.CourseDto
import retrofit2.Response
import retrofit2.http.GET

interface CourseApi {
    @GET("api/Courses")
    suspend fun getCourses(): Response<List<CourseDto>>
}