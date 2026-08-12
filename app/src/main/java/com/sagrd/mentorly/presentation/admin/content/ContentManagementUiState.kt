package com.sagrd.mentorly.presentation.admin.content
import com.sagrd.mentorly.domain.model.course.Course
data class ContentManagementUiState(val isLoading:Boolean=false,val isRefreshing:Boolean=false,val courseContent:Course?=null,val deletingItemId:String?=null,val reorderingItemId:String?=null,val errorMessage:String?=null,val hasAdminAccess:Boolean=true)
