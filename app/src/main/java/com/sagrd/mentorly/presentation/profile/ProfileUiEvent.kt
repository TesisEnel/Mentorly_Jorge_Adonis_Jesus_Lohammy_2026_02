package com.sagrd.mentorly.presentation.profile

sealed interface ProfileUiEvent {
    data object Load : ProfileUiEvent
    data object ShowEditDialog : ProfileUiEvent
    data object DismissEditDialog : ProfileUiEvent
    data class DisplayNameChanged(val value: String) : ProfileUiEvent
    data class EmailChanged(val value: String) : ProfileUiEvent
    data object SaveProfile : ProfileUiEvent
    data class PrivacyChanged(val isPublic: Boolean) : ProfileUiEvent
    data object ShowSignOutDialog : ProfileUiEvent
    data object DismissSignOutDialog : ProfileUiEvent
    data object ConfirmSignOut : ProfileUiEvent
    data object DismissError : ProfileUiEvent
}