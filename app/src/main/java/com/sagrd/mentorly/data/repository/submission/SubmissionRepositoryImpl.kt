package com.sagrd.mentorly.data.repository.submission

import com.sagrd.mentorly.data.remote.Resource
import com.sagrd.mentorly.data.remote.dto.submission.AdminSubmissionDecisionDto
import com.sagrd.mentorly.data.remote.dto.submission.CreateSubmissionDto
import com.sagrd.mentorly.data.remote.dto.submission.UpdateSubmissionDto
import com.sagrd.mentorly.data.remote.remotedatasource.SubmissionRemoteDataSource
import com.sagrd.mentorly.domain.model.submission.Submission
import com.sagrd.mentorly.domain.model.submission.SubmissionReview
import com.sagrd.mentorly.domain.model.submission.AdminEscalatedSubmission
import com.sagrd.mentorly.domain.model.submission.AdminSubmissionAudit
import com.sagrd.mentorly.domain.repository.submission.SubmissionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class SubmissionRepositoryImpl @Inject constructor(
    private val remoteDataSource: SubmissionRemoteDataSource
) : SubmissionRepository {

    override fun getEscalatedSubmissions(
        adminId: String
    ): Flow<Resource<List<AdminEscalatedSubmission>>> = flow {
        emit(Resource.Loading())

        remoteDataSource.getEscalatedSubmissions(adminId)
            .onSuccess { submissions ->
                emit(Resource.Success(submissions.map { it.toDomain() }))
            }
            .onFailure {
                emit(Resource.Error(it.message ?: "No se pudieron cargar las entregas escaladas."))
            }
    }

    override fun getEscalatedSubmissionAudit(
        adminId: String,
        submissionId: String
    ): Flow<Resource<AdminSubmissionAudit>> = flow {
        emit(Resource.Loading())

        remoteDataSource.getEscalatedSubmissionAudit(adminId, submissionId)
            .onSuccess { audit ->
                emit(Resource.Success(audit.toDomain()))
            }
            .onFailure {
                emit(Resource.Error(it.message ?: "No se pudo cargar la auditoría de la entrega."))
            }
    }

    override fun createSubmission(
        enrollmentId: String,
        activityId: String,
        submission: CreateSubmissionDto
    ): Flow<Resource<Submission>> = flow {
        emit(Resource.Loading())

        remoteDataSource.createSubmission(enrollmentId, activityId, submission)
            .onSuccess { createdSubmission ->
                emit(Resource.Success(createdSubmission.toDomain()))
            }
            .onFailure {
                emit(Resource.Error(it.message ?: "No se pudo crear la entrega."))
            }
    }

    override fun updateSubmission(
        submissionId: String,
        submission: UpdateSubmissionDto
    ): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())

        remoteDataSource.updateSubmission(submissionId, submission)
            .onSuccess {
                emit(Resource.Success(Unit))
            }
            .onFailure {
                emit(Resource.Error(it.message ?: "No se pudo actualizar la entrega."))
            }
    }

    override fun getSubmissionById(submissionId: String): Flow<Resource<Submission>> = flow {
        emit(Resource.Loading())

        remoteDataSource.getSubmissionById(submissionId)
            .onSuccess { submission ->
                emit(Resource.Success(submission.toDomain()))
            }
            .onFailure {
                emit(Resource.Error(it.message ?: "No se pudo cargar la entrega."))
            }
    }

    override fun getSubmissionsByStudentId(
        studentId: String
    ): Flow<Resource<List<Submission>>> = flow {
        emit(Resource.Loading())

        remoteDataSource.getSubmissionsByStudentId(studentId)
            .onSuccess { submissions ->
                emit(Resource.Success(submissions.map { it.toDomain() }))
            }
            .onFailure {
                emit(Resource.Error(it.message ?: "No se pudieron cargar las entregas."))
            }
    }

    override fun getSubmissionReviews(
        studentId: String,
        submissionId: String
    ): Flow<Resource<List<SubmissionReview>>> = flow {
        emit(Resource.Loading())

        remoteDataSource.getSubmissionReviews(studentId, submissionId)
            .onSuccess { reviews ->
                emit(Resource.Success(reviews.map { it.toDomain() }))
            }
            .onFailure {
                emit(Resource.Error(it.message ?: "No se pudieron cargar las revisiones."))
            }
    }

    override fun escalateSubmission(
        studentId: String,
        submissionId: String
    ): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())

        remoteDataSource.escalateSubmission(studentId, submissionId)
            .onSuccess {
                emit(Resource.Success(Unit))
            }
            .onFailure {
                emit(Resource.Error(it.message ?: "No se pudo escalar la entrega."))
            }
    }

    override fun decideSubmission(
        adminId: String,
        submissionId: String,
        decision: AdminSubmissionDecisionDto
    ): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())

        remoteDataSource.decideSubmission(adminId, submissionId, decision)
            .onSuccess {
                emit(Resource.Success(Unit))
            }
            .onFailure {
                emit(Resource.Error(it.message ?: "No se pudo decidir la entrega."))
            }
    }
}
