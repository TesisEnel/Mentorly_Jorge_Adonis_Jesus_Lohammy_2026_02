package com.sagrd.mentorly.domain.repository.student

import com.sagrd.mentorly.data.remote.Resource
import com.sagrd.mentorly.data.remote.dto.student.ProvisionStudentDto
import com.sagrd.mentorly.data.remote.dto.student.UpdateLeaderboardPrivacyDto
import com.sagrd.mentorly.data.remote.dto.student.UpdateStudentDto
import com.sagrd.mentorly.domain.model.student.Student
import com.sagrd.mentorly.domain.model.student.StudentStatistics
import kotlinx.coroutines.flow.Flow

interface StudentRepository {

    fun getStudents(): Flow<Resource<List<Student>>>

    fun getStudentById(studentId: String): Flow<Resource<Student>>

    fun provisionStudent(
        student: ProvisionStudentDto
    ): Flow<Resource<Student>>

    fun updateStudent(
        studentId: String,
        student: UpdateStudentDto
    ): Flow<Resource<Unit>>

    fun updateLeaderboardPrivacy(
        studentId: String,
        privacy: UpdateLeaderboardPrivacyDto
    ): Flow<Resource<Unit>>

    fun getStudentStatistics(
        studentId: String
    ): Flow<Resource<StudentStatistics>>

    fun promoteToAdmin(
        adminId: String,
        studentId: String
    ): Flow<Resource<Unit>>
}