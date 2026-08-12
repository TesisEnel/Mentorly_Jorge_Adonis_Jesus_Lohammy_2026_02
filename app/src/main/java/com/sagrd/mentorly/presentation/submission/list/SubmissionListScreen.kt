package com.sagrd.mentorly.presentation.submission.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.sagrd.mentorly.domain.model.submission.Submission
import com.sagrd.mentorly.domain.model.submission.SubmissionStatus
import com.sagrd.mentorly.ui.theme.MentorlyTheme
import com.sagrd.mentorly.util.DateFormatter

@Composable
fun SubmissionListScreen(
    onBackClick: () -> Unit,
    onSubmissionClick: (String) -> Unit,
    viewModel: SubmissionListViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    SubmissionListContent(
        state = state,
        onBackClick = onBackClick,
        onSubmissionClick = onSubmissionClick,
        onRetry = { viewModel.onEvent(SubmissionListUiEvent.Refresh) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SubmissionListContent(
    state: SubmissionListUiState,
    onBackClick: () -> Unit,
    onSubmissionClick: (String) -> Unit,
    onRetry: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Mis entregas",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        when {
            state.isLoading && state.submissions.isEmpty() -> {
                LoadingContent(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                )
            }

            state.errorMessage != null && state.submissions.isEmpty() -> {
                ErrorContent(
                    message = state.errorMessage,
                    onRetry = onRetry,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                )
            }

            state.submissions.isEmpty() -> {
                EmptyContent(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = state.submissions,
                        key = { submission -> submission.id }
                    ) { submission ->
                        SubmissionCard(
                            submission = submission,
                            onClick = { onSubmissionClick(submission.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SubmissionCard(
    submission: Submission,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = submission.activityTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = submission.evidenceUrl,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = submission.status.displayName(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Enviada: ${DateFormatter.format(submission.submittedAt)}",
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@Composable
private fun LoadingContent(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun EmptyContent(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Aún no tienes entregas.",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyLarge
            )
            Button(onClick = onRetry) {
                Text("Reintentar")
            }
        }
    }
}

private fun SubmissionStatus.displayName(): String = when (this) {
    SubmissionStatus.PENDING -> "Pendiente"
    SubmissionStatus.APPROVED -> "Aprobada"
    SubmissionStatus.REJECTED -> "Rechazada"
    SubmissionStatus.ESCALATED -> "Escalada"
    SubmissionStatus.UNKNOWN -> "Desconocido"
}

@Preview(showBackground = true)
@Composable
private fun SubmissionListPreview() {
    MentorlyTheme {
        SubmissionListContent(
            state = SubmissionListUiState(
                isLoading = false,
                submissions = listOf(
                    Submission(
                        id = "1",
                        enrollmentId = "e1",
                        activityId = "activity-1",
                        activityTitle = "Crear consultas SQL para una base de datos",
                        evidenceUrl = "https://github.com/user/repo",
                        status = SubmissionStatus.PENDING,
                        submittedAt = "2026-08-01"
                    )
                )
            ),
            onBackClick = {},
            onSubmissionClick = {},
            onRetry = {}
        )
    }
}
