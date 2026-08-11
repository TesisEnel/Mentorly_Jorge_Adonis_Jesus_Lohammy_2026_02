package com.sagrd.mentorly.data.remote.api

import com.sagrd.mentorly.data.remote.dto.common.ReorderItemsDto
import com.sagrd.mentorly.data.remote.dto.content.CourseUnitDto
import com.sagrd.mentorly.data.remote.dto.unit.CreateUnitDto
import com.sagrd.mentorly.data.remote.dto.unit.UpdateUnitDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface UnitApi {

    @GET("api/courses/{courseId}/units")
    suspend fun getUnitsByCourseId(
        @Path("courseId") courseId: String
    ): Response<List<CourseUnitDto>>

    @POST("api/admins/{adminId}/courses/{courseId}/units")
    suspend fun createUnit(
        @Path("adminId") adminId: String,
        @Path("courseId") courseId: String,
        @Body unit: CreateUnitDto
    ): Response<CourseUnitDto>

    @PUT("api/admins/{adminId}/units/{unitId}")
    suspend fun updateUnit(
        @Path("adminId") adminId: String,
        @Path("unitId") unitId: String,
        @Body unit: UpdateUnitDto
    ): Response<Unit>

    @DELETE("api/admins/{adminId}/units/{unitId}")
    suspend fun deleteUnit(
        @Path("adminId") adminId: String,
        @Path("unitId") unitId: String
    ): Response<Unit>

    @PATCH("api/admins/{adminId}/courses/{courseId}/units/order")
    suspend fun reorderUnits(
        @Path("adminId") adminId: String,
        @Path("courseId") courseId: String,
        @Body reorder: ReorderItemsDto
    ): Response<Unit>
}
