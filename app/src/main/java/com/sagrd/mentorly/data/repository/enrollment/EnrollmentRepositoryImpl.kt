package com.sagrd.mentorly.data.repository.enrollment

import com.sagrd.mentorly.data.remote.Resource
import com.sagrd.mentorly.data.remote.dto.enrollment.CreateEnrollmentDto
import com.sagrd.mentorly.data.remote.remotedatasource.EnrollmentRemoteDataSource
import com.sagrd.mentorly.domain.model.enrollment.Certificate
import com.sagrd.mentorly.domain.model.enrollment.Enrollment
import com.sagrd.mentorly.domain.model.enrollment.EnrollmentResult
import com.sagrd.mentorly.domain.model.enrollment.EnrollmentStatus
import com.sagrd.mentorly.domain.repository.enrollment.EnrollmentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class EnrollmentRepositoryImpl @Inject constructor(
    private val remoteDataSource: EnrollmentRemoteDataSource
) : EnrollmentRepository {

    override fun createEnrollment(
        studentId: String,
        enrollment: CreateEnrollmentDto
    ): Flow<Resource<EnrollmentResult>> = flow {
        emit(Resource.Loading())

        remoteDataSource.createEnrollment(studentId, enrollment)
            .onSuccess { result -> emit(Resource.Success(result.toDomain())) }
            .onFailure {
                emit(Resource.Error(it.message ?: "No se pudo crear la inscripción."))
            }
    }

    override fun getEnrollments(studentId: String): Flow<Resource<List<Enrollment>>> = flow {
        emit(Resource.Loading())

        remoteDataSource.getEnrollments(studentId)
            .onSuccess { enrollments ->
                emit(Resource.Success(enrollments.map { it.toDomain() }))
            }
            .onFailure {
                emit(Resource.Error(it.message ?: "No se pudieron cargar las inscripciones."))
            }
    }

    override fun getAdminStudentEnrollments(
        adminId: String,
        studentId: String
    ): Flow<Resource<List<Enrollment>>> = flow {
        emit(Resource.Loading())

        remoteDataSource.getAdminStudentEnrollments(adminId, studentId)
            .onSuccess { enrollments ->
                emit(Resource.Success(enrollments.map { it.toDomain() }))
            }
            .onFailure {
                emit(Resource.Error(it.message ?: "No se pudieron cargar las inscripciones del estudiante."))
            }
    }

    override fun getEnrollmentById(enrollmentId: String): Flow<Resource<Enrollment>> = flow {
        emit(Resource.Loading())

        remoteDataSource.getEnrollmentById(enrollmentId)
            .onSuccess { enrollment -> emit(Resource.Success(enrollment.toDomain())) }
            .onFailure {
                emit(Resource.Error(it.message ?: "No se pudo cargar la inscripción."))
            }
    }

    override fun restartEnrollment(
        studentId: String,
        courseId: String
    ): Flow<Resource<EnrollmentResult>> = flow {
        emit(Resource.Loading())

        remoteDataSource.restartEnrollment(studentId, courseId)
            .onSuccess { result -> emit(Resource.Success(result.toDomain())) }
            .onFailure {
                emit(Resource.Error(it.message ?: "No se pudo reiniciar la inscripción."))
            }
    }

    override fun getEnrollmentStatus(
        enrollmentId: String
    ): Flow<Resource<EnrollmentStatus>> = flow {
        emit(Resource.Loading())

        remoteDataSource.getEnrollmentStatus(enrollmentId)
            .onSuccess { status -> emit(Resource.Success(status.toDomain())) }
            .onFailure {
                emit(Resource.Error(it.message ?: "No se pudo cargar el estado de la inscripción."))
            }
    }

    override fun getCertificate(enrollmentId: String): Flow<Resource<Certificate>> = flow {
        emit(Resource.Loading())

        remoteDataSource.getCertificate(enrollmentId)
            .onSuccess { certificate -> emit(Resource.Success(certificate.toDomain())) }
            .onFailure {
                emit(Resource.Error(it.message ?: "No se pudo cargar el certificado."))
            }
    }
}
