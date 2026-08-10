package com.sagrd.mentorly.data.repository.peerreview

import com.sagrd.mentorly.data.remote.Resource
import com.sagrd.mentorly.data.remote.dto.peerreview.CreatePeerReviewRequestDto
import com.sagrd.mentorly.data.remote.remotedatasource.PeerReviewRemoteDataSource
import com.sagrd.mentorly.domain.model.submission.AnonymousSubmission
import com.sagrd.mentorly.domain.model.peerreview.PeerReview
import com.sagrd.mentorly.domain.model.peerreview.PeerReviewAudit
import com.sagrd.mentorly.domain.model.peerreview.PeerReviewResult
import com.sagrd.mentorly.domain.model.peerreview.ReviewQueueItem
import com.sagrd.mentorly.domain.repository.peerreview.PeerReviewRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class PeerReviewRepositoryImpl @Inject constructor(
    private val remoteDataSource: PeerReviewRemoteDataSource
) : PeerReviewRepository {

    override fun getQueue(studentId: String): Flow<Resource<List<ReviewQueueItem>>> = flow {
        emit(Resource.Loading())
        remoteDataSource.getQueue(studentId)
            .onSuccess { items ->
                emit(Resource.Success(items.map { it.toDomain() }))
            }
            .onFailure {
                emit(Resource.Error(it.message ?: "No se pudo cargar la cola de revisiones."))
            }
    }

    override fun getMyReviews(studentId: String): Flow<Resource<List<PeerReview>>> = flow {
        emit(Resource.Loading())
        remoteDataSource.getMyReviews(studentId)
            .onSuccess { reviews ->
                emit(Resource.Success(reviews.map { it.toDomain() }))
            }
            .onFailure {
                emit(Resource.Error(it.message ?: "No se pudieron cargar tus revisiones."))
            }
    }

    override fun getAnonymousSubmission(
        studentId: String,
        submissionId: String
    ): Flow<Resource<AnonymousSubmission>> = flow {
        emit(Resource.Loading())
        remoteDataSource.getAnonymousSubmission(studentId, submissionId)
            .onSuccess { submission ->
                emit(Resource.Success(submission.toDomain()))
            }
            .onFailure {
                emit(Resource.Error(it.message ?: "No se pudo cargar el envío."))
            }
    }

    override fun getAudit(
        adminId: String,
        peerReviewId: String
    ): Flow<Resource<PeerReviewAudit>> = flow {
        emit(Resource.Loading())
        remoteDataSource.getAudit(adminId, peerReviewId)
            .onSuccess { audit ->
                emit(Resource.Success(audit.toDomain()))
            }
            .onFailure {
                emit(Resource.Error(it.message ?: "No se pudo cargar la auditoría."))
            }
    }

    override fun getAllPeerReviews(adminId: String): Flow<Resource<List<PeerReview>>> = flow {
        emit(Resource.Loading())
        remoteDataSource.getAllPeerReviews(adminId)
            .onSuccess { reviews ->
                emit(Resource.Success(reviews.map { it.toDomain() }))
            }
            .onFailure {
                emit(Resource.Error(it.message ?: "No se pudieron cargar las revisiones."))
            }
    }

    override fun submitReview(
        studentId: String,
        dto: CreatePeerReviewRequestDto
    ): Flow<Resource<PeerReviewResult>> = flow {
        emit(Resource.Loading())
        remoteDataSource.submitReview(studentId, dto)
            .onSuccess { result ->
                emit(Resource.Success(result.toDomain()))
            }
            .onFailure {
                emit(Resource.Error(it.message ?: "No se pudo enviar la revisión."))
            }
    }
}