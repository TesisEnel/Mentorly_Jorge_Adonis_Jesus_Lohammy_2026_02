package com.sagrd.mentorly.domain.repository.community

import com.sagrd.mentorly.data.remote.Resource
import com.sagrd.mentorly.domain.model.community.CourseMember
import com.sagrd.mentorly.domain.model.community.LeaderboardEntry
import kotlinx.coroutines.flow.Flow

interface CourseCommunityRepository {

    fun getCourseMembers(
        courseId: String,
        viewerStudentId: String
    ): Flow<Resource<List<CourseMember>>>

    fun getLeaderboard(
        courseId: String,
        viewerStudentId: String
    ): Flow<Resource<List<LeaderboardEntry>>>

    fun getLeaderboardEntry(
        courseId: String,
        studentId: String
    ): Flow<Resource<LeaderboardEntry>>

    fun getAdminLeaderboard(
        adminId: String,
        courseId: String
    ): Flow<Resource<List<LeaderboardEntry>>>
}
