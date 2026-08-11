package com.sagrd.mentorly.data.repository.community

import com.sagrd.mentorly.data.remote.Resource
import com.sagrd.mentorly.data.remote.remotedatasource.CourseCommunityRemoteDataSource
import com.sagrd.mentorly.domain.model.community.CourseMember
import com.sagrd.mentorly.domain.model.community.LeaderboardEntry
import com.sagrd.mentorly.domain.repository.community.CourseCommunityRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class CourseCommunityRepositoryImpl @Inject constructor(
    private val remoteDataSource: CourseCommunityRemoteDataSource
) : CourseCommunityRepository {

    override fun getCourseMembers(
        courseId: String,
        viewerStudentId: String
    ): Flow<Resource<List<CourseMember>>> = flow {
        emit(Resource.Loading())

        remoteDataSource.getCourseMembers(courseId, viewerStudentId)
            .onSuccess { members ->
                emit(Resource.Success(members.map { it.toDomain() }))
            }
            .onFailure {
                emit(Resource.Error(it.message ?: "No se pudieron cargar los miembros del curso."))
            }
    }

    override fun getLeaderboard(
        courseId: String,
        viewerStudentId: String
    ): Flow<Resource<List<LeaderboardEntry>>> = flow {
        emit(Resource.Loading())

        remoteDataSource.getLeaderboard(courseId, viewerStudentId)
            .onSuccess { entries ->
                emit(Resource.Success(entries.map { it.toDomain() }))
            }
            .onFailure {
                emit(Resource.Error(it.message ?: "No se pudo cargar el ranking."))
            }
    }

    override fun getLeaderboardEntry(
        courseId: String,
        studentId: String
    ): Flow<Resource<LeaderboardEntry>> = flow {
        emit(Resource.Loading())

        remoteDataSource.getLeaderboardEntry(courseId, studentId)
            .onSuccess { entry ->
                emit(Resource.Success(entry.toDomain()))
            }
            .onFailure {
                emit(Resource.Error(it.message ?: "No se pudo cargar la posición del estudiante."))
            }
    }

    override fun getAdminLeaderboard(
        adminId: String,
        courseId: String
    ): Flow<Resource<List<LeaderboardEntry>>> = flow {
        emit(Resource.Loading())

        remoteDataSource.getAdminLeaderboard(adminId, courseId)
            .onSuccess { entries ->
                emit(Resource.Success(entries.map { it.toDomain() }))
            }
            .onFailure {
                emit(Resource.Error(it.message ?: "No se pudo cargar el ranking administrativo."))
            }
    }
}
