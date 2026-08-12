package com.sagrd.mentorly.domain.repository.analytics

import com.sagrd.mentorly.data.remote.Resource
import com.sagrd.mentorly.domain.model.analytics.*
import kotlinx.coroutines.flow.Flow

interface AnalyticsRepository {
    fun getOverview(): Flow<Resource<AnalyticsOverview>>
    fun getDropOff(courseId: String): Flow<Resource<List<DropOff>>>
    fun getCompletionTimeReport(courseId: String): Flow<Resource<CompletionTimeReport>>
    fun getBottlenecks(): Flow<Resource<List<PeerReviewBottleneck>>>
    fun getEnrollmentHistory(): Flow<Resource<List<EnrollmentHistory>>>
}
