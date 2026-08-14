package com.sagrd.mentorly.presentation.community.leaderboard

sealed interface LeaderboardUiEvent {
    data object Load : LeaderboardUiEvent
    data object Refresh : LeaderboardUiEvent
    data class SearchChanged(val value: String) : LeaderboardUiEvent
    data object ClearError : LeaderboardUiEvent
}
