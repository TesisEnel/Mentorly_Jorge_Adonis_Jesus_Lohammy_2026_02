package com.sagrd.mentorly.domain.repository.theme

import com.sagrd.mentorly.data.remote.Resource
import com.sagrd.mentorly.data.remote.dto.theme.CreateThemeDto
import com.sagrd.mentorly.data.remote.dto.theme.ReorderItemsDto
import com.sagrd.mentorly.data.remote.dto.theme.UpdateThemeDto
import com.sagrd.mentorly.domain.model.theme.Theme
import kotlinx.coroutines.flow.Flow

interface ThemeRepository {
    fun getThemesByUnit(unitId: String): Flow<Resource<List<Theme>>>
    fun createTheme(adminId: String, unitId: String, dto: CreateThemeDto): Flow<Resource<Theme>>
    fun updateTheme(adminId: String, themeId: String, dto: UpdateThemeDto): Flow<Resource<Unit>>
    fun deleteTheme(adminId: String, themeId: String): Flow<Resource<Unit>>
    fun reorderThemes(adminId: String, unitId: String, dto: ReorderItemsDto): Flow<Resource<Unit>>
}