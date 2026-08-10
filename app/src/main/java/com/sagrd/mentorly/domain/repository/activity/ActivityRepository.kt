package com.sagrd.mentorly.domain.repository.activity

import com.sagrd.mentorly.data.remote.Resource
import com.sagrd.mentorly.data.remote.dto.activity.CreateActivityDto
import com.sagrd.mentorly.data.remote.dto.activity.UpdateActivityDto
import com.sagrd.mentorly.data.remote.dto.common.ReorderItemsDto
import com.sagrd.mentorly.domain.model.content.Activity
import kotlinx.coroutines.flow.Flow

interface ActivityRepository {

    fun getActivities(themeId: String): Flow<Resource<List<Activity>>>

    fun createActivity(
        adminId: String,
        themeId: String,
        activity: CreateActivityDto
    ): Flow<Resource<Activity>>

    fun updateActivity(
        adminId: String,
        activityId: String,
        activity: UpdateActivityDto
    ): Flow<Resource<Unit>>

    fun deleteActivity(
        adminId: String,
        activityId: String
    ): Flow<Resource<Unit>>

    fun reorderActivities(
        adminId: String,
        themeId: String,
        items: ReorderItemsDto
    ): Flow<Resource<Unit>>
}
