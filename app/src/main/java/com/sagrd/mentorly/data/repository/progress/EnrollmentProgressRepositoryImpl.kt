package com.sagrd.mentorly.data.repository.progress

import com.sagrd.mentorly.data.remote.Resource
import com.sagrd.mentorly.data.remote.remotedatasource.EnrollmentProgressRemoteDataSource
import com.sagrd.mentorly.domain.model.progress.EnrollmentProgress
import com.sagrd.mentorly.domain.repository.progress.EnrollmentProgressRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class EnrollmentProgressRepositoryImpl @Inject constructor(
    private val remoteDataSource: EnrollmentProgressRemoteDataSource
) : EnrollmentProgressRepository {

    override fun getEnrollmentProgress(
        enrollmentId: String
    ): Flow<Resource<EnrollmentProgress>> = flow {
        emit(Resource.Loading())

        remoteDataSource.getEnrollmentProgress(enrollmentId)
            .onSuccess { progress ->
                emit(Resource.Success(progress.toDomain()))
            }
            .onFailure {
                emit(Resource.Error(it.message ?: "No se pudo cargar el progreso."))
            }
    }

    override fun getAdminEnrollmentProgress(
        adminId: String,
        enrollmentId: String
    ): Flow<Resource<EnrollmentProgress>> = flow {
        emit(Resource.Loading())

        remoteDataSource.getAdminEnrollmentProgress(adminId, enrollmentId)
            .onSuccess { progress ->
                emit(Resource.Success(progress.toDomain()))
            }
            .onFailure {
                emit(Resource.Error(it.message ?: "No se pudo cargar el progreso administrativo."))
            }
    }

    override fun completeTheme(
        enrollmentId: String,
        themeId: String
    ): Flow<Resource<EnrollmentProgress>> = flow {
        emit(Resource.Loading())

        remoteDataSource.completeTheme(enrollmentId, themeId)
            .onSuccess { progress ->
                emit(Resource.Success(progress.toDomain()))
            }
            .onFailure {
                emit(Resource.Error(it.message ?: "No se pudo completar el tema."))
            }
    }
}
