package com.sagrd.mentorly.domain.repository.unit

import com.sagrd.mentorly.data.remote.Resource
import com.sagrd.mentorly.data.remote.dto.common.ReorderItemsDto
import com.sagrd.mentorly.data.remote.dto.unit.CreateUnitDto
import com.sagrd.mentorly.data.remote.dto.unit.UpdateUnitDto
import com.sagrd.mentorly.domain.model.content.CourseUnit
import kotlinx.coroutines.flow.Flow

interface UnitRepository {

    fun getUnitsByCourseId(courseId: String): Flow<Resource<List<CourseUnit>>>

    fun createUnit(
        adminId: String,
        courseId: String,
        unit: CreateUnitDto
    ): Flow<Resource<CourseUnit>>

    fun updateUnit(
        adminId: String,
        unitId: String,
        unit: UpdateUnitDto
    ): Flow<Resource<Unit>>

    fun deleteUnit(
        adminId: String,
        unitId: String
    ): Flow<Resource<Unit>>

    fun reorderUnits(
        adminId: String,
        courseId: String,
        reorder: ReorderItemsDto
    ): Flow<Resource<Unit>>
}
