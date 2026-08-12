package com.sagrd.mentorly.domain.repository.enrollment

import com.sagrd.mentorly.data.remote.Resource
import com.sagrd.mentorly.data.remote.dto.enrollment.CreateEnrollmentDto
import com.sagrd.mentorly.domain.model.enrollment.Certificate
import com.sagrd.mentorly.domain.model.enrollment.Enrollment
import com.sagrd.mentorly.domain.model.enrollment.EnrollmentResult
import com.sagrd.mentorly.domain.model.enrollment.EnrollmentStatus
import kotlinx.coroutines.flow.Flow

interface EnrollmentRepository {

    fun createEnrollment(
        studentId: String,
        enrollment: CreateEnrollmentDto
    ): Flow<Resource<EnrollmentResult>>

    fun getEnrollments(studentId: String): Flow<Resource<List<Enrollment>>>

    fun getEnrollmentById(enrollmentId: String): Flow<Resource<Enrollment>>

    fun restartEnrollment(
        studentId: String,
        courseId: String
    ): Flow<Resource<EnrollmentResult>>

    fun getEnrollmentStatus(enrollmentId: String): Flow<Resource<EnrollmentStatus>>

    fun getCertificate(enrollmentId: String): Flow<Resource<Certificate>>
}
