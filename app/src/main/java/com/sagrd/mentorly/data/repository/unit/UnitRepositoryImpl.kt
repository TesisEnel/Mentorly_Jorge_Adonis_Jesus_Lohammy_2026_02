package com.sagrd.mentorly.data.repository.unit

import com.sagrd.mentorly.data.remote.Resource
import com.sagrd.mentorly.data.remote.dto.common.ReorderItemsDto
import com.sagrd.mentorly.data.remote.dto.unit.CreateUnitDto
import com.sagrd.mentorly.data.remote.dto.unit.UpdateUnitDto
import com.sagrd.mentorly.data.remote.remotedatasource.UnitRemoteDataSource
import com.sagrd.mentorly.domain.model.content.CourseUnit
import com.sagrd.mentorly.domain.repository.unit.UnitRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class UnitRepositoryImpl @Inject constructor(
    private val remoteDataSource: UnitRemoteDataSource
) : UnitRepository {

    override fun getUnitsByCourseId(courseId: String): Flow<Resource<List<CourseUnit>>> = flow {
        emit(Resource.Loading())

        remoteDataSource.getUnitsByCourseId(courseId)
            .onSuccess { units ->
                emit(Resource.Success(units.map { it.toDomain(courseId) }))
            }
            .onFailure {
                emit(Resource.Error(it.message ?: "No se pudieron cargar las unidades."))
            }
    }

    override fun createUnit(
        adminId: String,
        courseId: String,
        unit: CreateUnitDto
    ): Flow<Resource<CourseUnit>> = flow {
        emit(Resource.Loading())

        remoteDataSource.createUnit(adminId, courseId, unit)
            .onSuccess { createdUnit ->
                emit(Resource.Success(createdUnit.toDomain(courseId)))
            }
            .onFailure {
                emit(Resource.Error(it.message ?: "No se pudo crear la unidad."))
            }
    }

    override fun updateUnit(
        adminId: String,
        unitId: String,
        unit: UpdateUnitDto
    ): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())

        remoteDataSource.updateUnit(adminId, unitId, unit)
            .onSuccess { emit(Resource.Success(Unit)) }
            .onFailure {
                emit(Resource.Error(it.message ?: "No se pudo actualizar la unidad."))
            }
    }

    override fun deleteUnit(
        adminId: String,
        unitId: String
    ): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())

        remoteDataSource.deleteUnit(adminId, unitId)
            .onSuccess { emit(Resource.Success(Unit)) }
            .onFailure {
                emit(Resource.Error(it.message ?: "No se pudo eliminar la unidad."))
            }
    }

    override fun reorderUnits(
        adminId: String,
        courseId: String,
        reorder: ReorderItemsDto
    ): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())

        remoteDataSource.reorderUnits(adminId, courseId, reorder)
            .onSuccess { emit(Resource.Success(Unit)) }
            .onFailure {
                emit(Resource.Error(it.message ?: "No se pudieron reordenar las unidades."))
            }
    }
}
