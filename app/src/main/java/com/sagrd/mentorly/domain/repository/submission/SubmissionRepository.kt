package com.sagrd.mentorly.domain.repository.submission

import com.sagrd.mentorly.data.remote.Resource
import com.sagrd.mentorly.data.remote.dto.submission.AdminSubmissionDecisionDto
import com.sagrd.mentorly.data.remote.dto.submission.CreateSubmissionDto
import com.sagrd.mentorly.data.remote.dto.submission.UpdateSubmissionDto
import com.sagrd.mentorly.domain.model.submission.Submission
import com.sagrd.mentorly.domain.model.submission.SubmissionReview
import com.sagrd.mentorly.domain.model.submission.AdminEscalatedSubmission
import com.sagrd.mentorly.domain.model.submission.AdminSubmissionAudit
import kotlinx.coroutines.flow.Flow

interface SubmissionRepository {

    fun getEscalatedSubmissions(
        adminId: String
    ): Flow<Resource<List<AdminEscalatedSubmission>>>

    fun getEscalatedSubmissionAudit(
        adminId: String,
        submissionId: String
    ): Flow<Resource<AdminSubmissionAudit>>

    fun createSubmission(
        enrollmentId: String,
        activityId: String,
        submission: CreateSubmissionDto
    ): Flow<Resource<Submission>>

    fun updateSubmission(
        submissionId: String,
        submission: UpdateSubmissionDto
    ): Flow<Resource<Unit>>

    fun getSubmissionById(submissionId: String): Flow<Resource<Submission>>

    fun getSubmissionsByStudentId(studentId: String): Flow<Resource<List<Submission>>>

    fun getSubmissionReviews(
        studentId: String,
        submissionId: String
    ): Flow<Resource<List<SubmissionReview>>>

    fun escalateSubmission(
        studentId: String,
        submissionId: String
    ): Flow<Resource<Unit>>

    fun decideSubmission(
        adminId: String,
        submissionId: String,
        decision: AdminSubmissionDecisionDto
    ): Flow<Resource<Unit>>
}
