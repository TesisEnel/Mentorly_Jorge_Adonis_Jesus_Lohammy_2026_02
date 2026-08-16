package com.sagrd.mentorly.domain.repository.peerreview

import com.sagrd.mentorly.data.remote.Resource
import com.sagrd.mentorly.data.remote.dto.peerreview.CreatePeerReviewRequestDto
import com.sagrd.mentorly.data.remote.dto.peerreview.CreatePeerReviewRubricCriterionDto
import com.sagrd.mentorly.data.remote.dto.peerreview.UpdatePeerReviewRubricCriterionDto
import com.sagrd.mentorly.domain.model.submission.AnonymousSubmission
import com.sagrd.mentorly.domain.model.peerreview.PeerReview
import com.sagrd.mentorly.domain.model.peerreview.PeerReviewAudit
import com.sagrd.mentorly.domain.model.peerreview.PeerReviewResult
import com.sagrd.mentorly.domain.model.peerreview.PeerReviewRubricCriterion
import com.sagrd.mentorly.domain.model.peerreview.ReviewQueueItem
import kotlinx.coroutines.flow.Flow

interface PeerReviewRepository {
    fun getRubric(activityId: String): Flow<Resource<List<PeerReviewRubricCriterion>>>

    fun createRubricCriterion(
        adminId: String,
        activityId: String,
        dto: CreatePeerReviewRubricCriterionDto
    ): Flow<Resource<PeerReviewRubricCriterion>>

    fun updateRubricCriterion(
        adminId: String,
        criterionId: String,
        dto: UpdatePeerReviewRubricCriterionDto
    ): Flow<Resource<Unit>>

    fun deleteRubricCriterion(
        adminId: String,
        criterionId: String
    ): Flow<Resource<Unit>>

    fun getQueue(studentId: String): Flow<Resource<List<ReviewQueueItem>>>
    fun getMyReviews(studentId: String): Flow<Resource<List<PeerReview>>>
    fun getAnonymousSubmission(studentId: String, submissionId: String): Flow<Resource<AnonymousSubmission>>
    fun getAudit(adminId: String, peerReviewId: String): Flow<Resource<PeerReviewAudit>>
    fun getAllPeerReviews(adminId: String): Flow<Resource<List<PeerReview>>>
    fun submitReview(studentId: String, dto: CreatePeerReviewRequestDto): Flow<Resource<PeerReviewResult>>
}
