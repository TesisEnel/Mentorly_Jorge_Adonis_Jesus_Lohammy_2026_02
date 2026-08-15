package com.sagrd.mentorly.presentation.community.leaderboard

import com.sagrd.mentorly.domain.model.community.LeaderboardEntry

data class LeaderboardUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val entries: List<LeaderboardEntry> = emptyList(),
    val ownPosition: LeaderboardEntry? = null,
    val searchQuery: String = "",
    val errorMessage: String? = null,
    val hasSession: Boolean = true
)
