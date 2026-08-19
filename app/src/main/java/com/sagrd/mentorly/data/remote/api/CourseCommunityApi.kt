package com.sagrd.mentorly.data.remote.api

import com.sagrd.mentorly.data.remote.dto.community.CourseMemberDto
import com.sagrd.mentorly.data.remote.dto.community.LeaderboardEntryDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface CourseCommunityApi {

    @GET("api/courses/{courseId}/members")
    suspend fun getCourseMembers(
        @Path("courseId") courseId: String,
        @Query("viewerStudentId") viewerStudentId: String? = null
    ): Response<List<CourseMemberDto>>

    @GET("api/courses/{courseId}/leaderboard")
    suspend fun getLeaderboard(
        @Path("courseId") courseId: String,
        @Query("viewerStudentId") viewerStudentId: String? = null
    ): Response<List<LeaderboardEntryDto>>

    @GET("api/courses/{courseId}/leaderboard/{studentId}")
    suspend fun getLeaderboardEntry(
        @Path("courseId") courseId: String,
        @Path("studentId") studentId: String
    ): Response<LeaderboardEntryDto>

    @GET("api/admins/{adminId}/courses/{courseId}/leaderboard")
    suspend fun getAdminLeaderboard(
        @Path("adminId") adminId: String,
        @Path("courseId") courseId: String
    ): Response<List<LeaderboardEntryDto>>
}
