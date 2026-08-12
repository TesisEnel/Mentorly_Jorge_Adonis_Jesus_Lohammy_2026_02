package com.sagrd.mentorly.data.remote.api

import com.sagrd.mentorly.data.remote.dto.analytics.*
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface AnalyticsApi {
    @GET("api/Analytics/Overview")
    suspend fun getOverview(): Response<AnalyticsOverviewDto>

    @GET("api/Analytics/DropOff/{courseId}")
    suspend fun getDropOff(@Path("courseId") courseId: String): Response<List<DropOffDto>>

    @GET("api/Analytics/CompletionTime/{courseId}")
    suspend fun getCompletionTimeReport(@Path("courseId") courseId: String): Response<CompletionTimeReportDto>

    @GET("api/Analytics/Bottlenecks")
    suspend fun getBottlenecks(): Response<List<PeerReviewBottleneckDto>>

    @GET("api/Analytics/EnrollmentHistory")
    suspend fun getEnrollmentHistory(): Response<List<EnrollmentHistoryDto>>
}
