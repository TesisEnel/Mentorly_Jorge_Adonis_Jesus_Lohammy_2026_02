package com.sagrd.mentorly.data.remote.api

import com.sagrd.mentorly.data.remote.dto.analytics.AnalyticsOverviewDto
import com.sagrd.mentorly.data.remote.dto.analytics.CompletionTimeReportDto
import com.sagrd.mentorly.data.remote.dto.analytics.DropOffDto
import com.sagrd.mentorly.data.remote.dto.analytics.EnrollmentHistoryDto
import com.sagrd.mentorly.data.remote.dto.analytics.PeerReviewBottleneckDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface AnalyticsApi {
    @GET("api/admins/{adminId}/analytics/overview")
    suspend fun getOverview(
        @Path("adminId") adminId: String
    ): Response<AnalyticsOverviewDto>

    @GET("api/admins/{adminId}/analytics/courses/{courseId}/drop-off")
    suspend fun getDropOff(
        @Path("adminId") adminId: String,
        @Path("courseId") courseId: String
    ): Response<List<DropOffDto>>

    @GET("api/admins/{adminId}/analytics/courses/{courseId}/completion-time")
    suspend fun getCompletionTimeReport(
        @Path("adminId") adminId: String,
        @Path("courseId") courseId: String
    ): Response<CompletionTimeReportDto>

    @GET("api/admins/{adminId}/analytics/courses/{courseId}/peer-review-bottlenecks")
    suspend fun getBottlenecks(
        @Path("adminId") adminId: String,
        @Path("courseId") courseId: String
    ): Response<List<PeerReviewBottleneckDto>>

    @GET("api/admins/{adminId}/analytics/courses/{courseId}/enrollment-history")
    suspend fun getEnrollmentHistory(
        @Path("adminId") adminId: String,
        @Path("courseId") courseId: String
    ): Response<List<EnrollmentHistoryDto>>
}
