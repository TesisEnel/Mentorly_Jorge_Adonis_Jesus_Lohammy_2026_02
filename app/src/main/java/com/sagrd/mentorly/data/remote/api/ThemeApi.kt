package com.sagrd.mentorly.data.remote.api

import com.sagrd.mentorly.data.remote.dto.common.ReorderItemsDto
import com.sagrd.mentorly.data.remote.dto.theme.CreateThemeDto
import com.sagrd.mentorly.data.remote.dto.theme.ThemeDto
import com.sagrd.mentorly.data.remote.dto.theme.UpdateThemeDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ThemeApi {

    @GET("api/units/{unitId}/themes")
    suspend fun getThemesByUnit(
        @Path("unitId") unitId: String
    ): Response<List<ThemeDto>>

    @POST("api/admins/{adminId}/units/{unitId}/themes")
    suspend fun createTheme(
        @Path("adminId") adminId: String,
        @Path("unitId") unitId: String,
        @Body dto: CreateThemeDto
    ): Response<ThemeDto>

    @PUT("api/admins/{adminId}/themes/{themeId}")
    suspend fun updateTheme(
        @Path("adminId") adminId: String,
        @Path("themeId") themeId: String,
        @Body dto: UpdateThemeDto
    ): Response<Unit>

    @DELETE("api/admins/{adminId}/themes/{themeId}")
    suspend fun deleteTheme(
        @Path("adminId") adminId: String,
        @Path("themeId") themeId: String
    ): Response<Unit>

    @PATCH("api/admins/{adminId}/units/{unitId}/themes/order")
    suspend fun reorderThemes(
        @Path("adminId") adminId: String,
        @Path("unitId") unitId: String,
        @Body dto: ReorderItemsDto
    ): Response<Unit>
}
