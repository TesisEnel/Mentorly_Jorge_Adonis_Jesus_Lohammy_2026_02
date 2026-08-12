package com.sagrd.mentorly.presentation.startup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@Composable
fun StartupScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToCourseList: () -> Unit,
    onNavigateToAdminDashboard: () -> Unit,
    viewModel: StartupViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.destination) {
        when (state.destination) {
            StartupDestination.LOGIN -> {
                viewModel.onEvent(StartupUiEvent.DestinationHandled)
                onNavigateToLogin()
            }
            StartupDestination.COURSE_LIST -> {
                viewModel.onEvent(StartupUiEvent.DestinationHandled)
                onNavigateToCourseList()
            }
            StartupDestination.ADMIN_DASHBOARD -> {
                viewModel.onEvent(StartupUiEvent.DestinationHandled)
                onNavigateToAdminDashboard()
            }
            null -> Unit
        }
    }

    StartupContent(
        state = state,
        onRetry = { viewModel.onEvent(StartupUiEvent.Retry) },
        onSignOut = { viewModel.onEvent(StartupUiEvent.SignOut) }
    )
}

@Composable
private fun StartupContent(
    state: StartupUiState,
    onRetry: () -> Unit,
    onSignOut: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (state.isLoading) {
            CircularProgressIndicator()
            Text(
                text = "Preparando tu experiencia...",
                modifier = Modifier.padding(top = 16.dp),
                style = MaterialTheme.typography.bodyLarge
            )
        } else {
            Text(
                text = state.errorMessage.orEmpty(),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyLarge
            )

            Button(
                onClick = onRetry,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text("Reintentar")
            }

            Button(
                onClick = onSignOut,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text("Cerrar sesión")
            }
        }
    }
}
