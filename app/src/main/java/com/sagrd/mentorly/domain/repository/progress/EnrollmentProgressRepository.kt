package com.sagrd.mentorly.domain.repository.progress

import com.sagrd.mentorly.data.remote.Resource
import com.sagrd.mentorly.domain.model.progress.EnrollmentProgress
import kotlinx.coroutines.flow.Flow

interface EnrollmentProgressRepository {

    fun getEnrollmentProgress(
        enrollmentId: String
    ): Flow<Resource<EnrollmentProgress>>

    fun completeTheme(
        enrollmentId: String,
        themeId: String
    ): Flow<Resource<EnrollmentProgress>>
}
