package com.sagrd.mentorly.presentation.community.members

import com.sagrd.mentorly.domain.model.community.CourseMember

data class CourseMembersUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val members: List<CourseMember> = emptyList(),
    val searchQuery: String = "",
    val errorMessage: String? = null,
    val hasSession: Boolean = true
)
