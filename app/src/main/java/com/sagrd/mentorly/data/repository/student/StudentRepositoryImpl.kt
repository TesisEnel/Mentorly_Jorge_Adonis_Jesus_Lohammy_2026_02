package com.sagrd.mentorly.data.repository.student

import com.sagrd.mentorly.data.remote.Resource
import com.sagrd.mentorly.data.remote.dto.student.ProvisionStudentDto
import com.sagrd.mentorly.data.remote.dto.student.UpdateLeaderboardPrivacyDto
import com.sagrd.mentorly.data.remote.dto.student.UpdateStudentDto
import com.sagrd.mentorly.data.remote.remotedatasource.StudentRemoteDataSource
import com.sagrd.mentorly.domain.model.student.Student
import com.sagrd.mentorly.domain.model.student.StudentStatistics
import com.sagrd.mentorly.domain.repository.student.StudentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class StudentRepositoryImpl @Inject constructor(
    private val remoteDataSource: StudentRemoteDataSource
) : StudentRepository {

    override fun getStudents(): Flow<Resource<List<Student>>> = flow {
        emit(Resource.Loading())

        remoteDataSource.getStudents()
            .onSuccess { students ->
                emit(Resource.Success(students.map { it.toDomain() }))
            }
            .onFailure {
                emit(Resource.Error(it.message ?: "No se pudieron cargar los estudiantes."))
            }
    }

    override fun getStudentById(
        studentId: String
    ): Flow<Resource<Student>> = flow {
        emit(Resource.Loading())

        remoteDataSource.getStudentById(studentId)
            .onSuccess { student ->
                emit(Resource.Success(student.toDomain()))
            }
            .onFailure {
                emit(Resource.Error(it.message ?: "No se pudo cargar el estudiante."))
            }
    }

    override fun provisionStudent(
        student: ProvisionStudentDto
    ): Flow<Resource<Student>> = flow {
        emit(Resource.Loading())

        remoteDataSource.provisionStudent(student)
            .onSuccess { provisionedStudent ->
                emit(Resource.Success(provisionedStudent.toDomain()))
            }
            .onFailure {
                emit(Resource.Error(it.message ?: "No se pudo crear o sincronizar el estudiante."))
            }
    }

    override fun updateStudent(
        studentId: String,
        student: UpdateStudentDto
    ): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())

        remoteDataSource.updateStudent(studentId, student)
            .onSuccess {
                emit(Resource.Success(Unit))
            }
            .onFailure {
                emit(Resource.Error(it.message ?: "No se pudo actualizar el estudiante."))
            }
    }

    override fun updateLeaderboardPrivacy(
        studentId: String,
        privacy: UpdateLeaderboardPrivacyDto
    ): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())

        remoteDataSource.updateLeaderboardPrivacy(studentId, privacy)
            .onSuccess {
                emit(Resource.Success(Unit))
            }
            .onFailure {
                emit(Resource.Error(it.message ?: "No se pudo actualizar la privacidad."))
            }
    }

    override fun getStudentStatistics(
        studentId: String
    ): Flow<Resource<StudentStatistics>> = flow {
        emit(Resource.Loading())

        remoteDataSource.getStudentStatistics(studentId)
            .onSuccess { statistics ->
                emit(Resource.Success(statistics.toDomain()))
            }
            .onFailure {
                emit(Resource.Error(it.message ?: "No se pudieron cargar las estadísticas."))
            }
    }

    override fun promoteToAdmin(
        adminId: String,
        studentId: String
    ): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())

        remoteDataSource.promoteToAdmin(adminId, studentId)
            .onSuccess {
                emit(Resource.Success(Unit))
            }
            .onFailure {
                emit(Resource.Error(it.message ?: "No se pudo promover al estudiante."))
            }
    }
}