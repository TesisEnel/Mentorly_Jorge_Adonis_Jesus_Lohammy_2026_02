package com.sagrd.mentorly.data.repository.activity

import com.sagrd.mentorly.data.remote.Resource
import com.sagrd.mentorly.data.remote.dto.activity.CreateActivityDto
import com.sagrd.mentorly.data.remote.dto.activity.UpdateActivityDto
import com.sagrd.mentorly.data.remote.dto.common.ReorderItemsDto
import com.sagrd.mentorly.data.remote.remotedatasource.ActivityRemoteDataSource
import com.sagrd.mentorly.domain.model.content.Activity
import com.sagrd.mentorly.domain.repository.activity.ActivityRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class ActivityRepositoryImpl @Inject constructor(
    private val remoteDataSource: ActivityRemoteDataSource
) : ActivityRepository {

    override fun getActivities(themeId: String): Flow<Resource<List<Activity>>> = flow {
        emit(Resource.Loading())

        remoteDataSource.getActivities(themeId)
            .onSuccess { activities ->
                emit(Resource.Success(activities.map { it.toDomain(themeId) }))
            }
            .onFailure {
                emit(Resource.Error(it.message ?: "No se pudieron cargar las actividades."))
            }
    }

    override fun createActivity(
        adminId: String,
        themeId: String,
        activity: CreateActivityDto
    ): Flow<Resource<Activity>> = flow {
        emit(Resource.Loading())

        remoteDataSource.createActivity(adminId, themeId, activity)
            .onSuccess { createdActivity ->
                emit(Resource.Success(createdActivity.toDomain(themeId)))
            }
            .onFailure {
                emit(Resource.Error(it.message ?: "No se pudo crear la actividad."))
            }
    }

    override fun updateActivity(
        adminId: String,
        activityId: String,
        activity: UpdateActivityDto
    ): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())

        remoteDataSource.updateActivity(adminId, activityId, activity)
            .onSuccess {
                emit(Resource.Success(Unit))
            }
            .onFailure {
                emit(Resource.Error(it.message ?: "No se pudo actualizar la actividad."))
            }
    }

    override fun deleteActivity(
        adminId: String,
        activityId: String
    ): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())

        remoteDataSource.deleteActivity(adminId, activityId)
            .onSuccess {
                emit(Resource.Success(Unit))
            }
            .onFailure {
                emit(Resource.Error(it.message ?: "No se pudo eliminar la actividad."))
            }
    }

    override fun reorderActivities(
        adminId: String,
        themeId: String,
        items: ReorderItemsDto
    ): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())

        remoteDataSource.reorderActivities(adminId, themeId, items)
            .onSuccess {
                emit(Resource.Success(Unit))
            }
            .onFailure {
                emit(Resource.Error(it.message ?: "No se pudieron reordenar las actividades."))
            }
    }
}
