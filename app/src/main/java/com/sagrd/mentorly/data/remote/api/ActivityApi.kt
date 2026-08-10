package com.sagrd.mentorly.data.remote.api

import com.sagrd.mentorly.data.remote.dto.activity.CreateActivityDto
import com.sagrd.mentorly.data.remote.dto.activity.UpdateActivityDto
import com.sagrd.mentorly.data.remote.dto.common.ReorderItemsDto
import com.sagrd.mentorly.data.remote.dto.content.ActivityDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ActivityApi {

    @GET("api/themes/{themeId}/activities")
    suspend fun getActivities(
        @Path("themeId") themeId: String
    ): Response<List<ActivityDto>>

    @POST("api/admins/{adminId}/themes/{themeId}/activities")
    suspend fun createActivity(
        @Path("adminId") adminId: String,
        @Path("themeId") themeId: String,
        @Body activity: CreateActivityDto
    ): Response<ActivityDto>

    @PUT("api/admins/{adminId}/activities/{activityId}")
    suspend fun updateActivity(
        @Path("adminId") adminId: String,
        @Path("activityId") activityId: String,
        @Body activity: UpdateActivityDto
    ): Response<Unit>

    @DELETE("api/admins/{adminId}/activities/{activityId}")
    suspend fun deleteActivity(
        @Path("adminId") adminId: String,
        @Path("activityId") activityId: String
    ): Response<Unit>

    @PATCH("api/admins/{adminId}/themes/{themeId}/activities/order")
    suspend fun reorderActivities(
        @Path("adminId") adminId: String,
        @Path("themeId") themeId: String,
        @Body items: ReorderItemsDto
    ): Response<Unit>
}
