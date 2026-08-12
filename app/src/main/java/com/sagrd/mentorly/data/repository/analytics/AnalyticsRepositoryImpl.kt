package com.sagrd.mentorly.data.repository.analytics

import com.sagrd.mentorly.data.remote.Resource
import com.sagrd.mentorly.data.remote.remotedatasource.AnalyticsRemoteDataSource
import com.sagrd.mentorly.domain.model.analytics.*
import com.sagrd.mentorly.domain.repository.analytics.AnalyticsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class AnalyticsRepositoryImpl @Inject constructor(
    private val remoteDataSource: AnalyticsRemoteDataSource
) : AnalyticsRepository {
    override fun getOverview(adminId: String): Flow<Resource<AnalyticsOverview>> = flow {
        emit(Resource.Loading())
        remoteDataSource.getOverview(adminId)
            .onSuccess { emit(Resource.Success(it.toDomain())) }
            .onFailure { emit(Resource.Error(it.message ?: "Error desconocido")) }
    }

    override fun getDropOff(adminId: String, courseId: String): Flow<Resource<List<DropOff>>> = flow {
        emit(Resource.Loading())
        remoteDataSource.getDropOff(adminId, courseId)
            .onSuccess { list -> emit(Resource.Success(list.map { it.toDomain() })) }
            .onFailure { emit(Resource.Error(it.message ?: "Error desconocido")) }
    }

    override fun getCompletionTimeReport(adminId: String, courseId: String): Flow<Resource<CompletionTimeReport>> = flow {
        emit(Resource.Loading())
        remoteDataSource.getCompletionTimeReport(adminId, courseId)
            .onSuccess { emit(Resource.Success(it.toDomain())) }
            .onFailure { emit(Resource.Error(it.message ?: "Error desconocido")) }
    }

    override fun getBottlenecks(adminId: String, courseId: String): Flow<Resource<List<PeerReviewBottleneck>>> = flow {
        emit(Resource.Loading())
        remoteDataSource.getBottlenecks(adminId, courseId)
            .onSuccess { list -> emit(Resource.Success(list.map { it.toDomain() })) }
            .onFailure { emit(Resource.Error(it.message ?: "Error desconocido")) }
    }

    override fun getEnrollmentHistory(adminId: String, courseId: String): Flow<Resource<List<EnrollmentHistory>>> = flow {
        emit(Resource.Loading())
        remoteDataSource.getEnrollmentHistory(adminId, courseId)
            .onSuccess { list -> emit(Resource.Success(list.map { it.toDomain() })) }
            .onFailure { emit(Resource.Error(it.message ?: "Error desconocido")) }
    }
}
