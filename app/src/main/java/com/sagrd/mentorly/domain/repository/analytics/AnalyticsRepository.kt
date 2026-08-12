package com.sagrd.mentorly.domain.repository.analytics

import com.sagrd.mentorly.data.remote.Resource
import com.sagrd.mentorly.domain.model.analytics.*
import kotlinx.coroutines.flow.Flow

interface AnalyticsRepository {
    fun getOverview(adminId: String): Flow<Resource<AnalyticsOverview>>
    fun getDropOff(adminId: String, courseId: String): Flow<Resource<List<DropOff>>>
    fun getCompletionTimeReport(adminId: String, courseId: String): Flow<Resource<CompletionTimeReport>>
    fun getBottlenecks(adminId: String, courseId: String): Flow<Resource<List<PeerReviewBottleneck>>>
    fun getEnrollmentHistory(adminId: String, courseId: String): Flow<Resource<List<EnrollmentHistory>>>
}
