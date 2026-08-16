package com.sagrd.mentorly.presentation.peerreview.queue

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.TextButton
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
import com.sagrd.mentorly.domain.model.peerreview.ReviewQueueItem
import com.sagrd.mentorly.domain.model.submission.EvidenceType
import com.sagrd.mentorly.ui.theme.MentorlyTheme
import com.sagrd.mentorly.util.DateFormatter

@Composable
fun PeerReviewQueueScreen(
    onSubmissionClick: (String) -> Unit,
    onHistoryClick: () -> Unit,
    viewModel: PeerReviewQueueViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    PeerReviewQueueContent(
        uiState = uiState,
        onSubmissionClick = onSubmissionClick,
        onHistoryClick = onHistoryClick,
        onRetry = { viewModel.onEvent(PeerReviewQueueUiEvent.Refresh) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PeerReviewQueueContent(
    uiState: PeerReviewQueueUiState,
    onSubmissionClick: (String) -> Unit,
    onHistoryClick: () -> Unit,
    onRetry: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Revisiones por pares", fontWeight = FontWeight.Bold) },
                actions = {
                    TextButton(onClick = onHistoryClick) {
                        Text("Historial")
                    }
                }
            )
        }
    ) { innerPadding ->
        when {
            uiState.isLoading && uiState.queueItems.isEmpty() -> LoadingContent(
                Modifier.fillMaxSize().padding(innerPadding)
            )

            uiState.errorMessage != null && uiState.queueItems.isEmpty() -> ErrorContent(
                message = uiState.errorMessage,
                onRetry = onRetry,
                modifier = Modifier.fillMaxSize().padding(innerPadding)
            )

            uiState.queueItems.isEmpty() -> EmptyContent(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                onHistoryClick = onHistoryClick
            )

            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                uiState.errorMessage?.let { message ->
                    item { ErrorBanner(message, onRetry) }
                }

                items(uiState.queueItems, key = { it.submissionId }) { item ->
                    QueueItemCard(item, onClick = { onSubmissionClick(item.submissionId) })
                }
            }
        }
    }
}

@Composable
private fun QueueItemCard(item: ReviewQueueItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(item.activityTitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text("Entrega anónima", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
            Text(
                text = item.evidenceContent,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text("Enviada: ${DateFormatter.format(item.submittedAtUtc)}", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun LoadingContent(modifier: Modifier) {
    Box(modifier, contentAlignment = Alignment.Center) { CircularProgressIndicator() }
}

@Composable
private fun EmptyContent(modifier: Modifier, onHistoryClick: () -> Unit) {
    Box(modifier.padding(24.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("No hay entregas disponibles para revisar.", style = MaterialTheme.typography.bodyLarge)
            TextButton(onClick = onHistoryClick) { Text("Ver mis revisiones") }
        }
    }
}

@Composable
private fun ErrorContent(message: String, onRetry: () -> Unit, modifier: Modifier) {
    Box(modifier.padding(24.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(message, color = MaterialTheme.colorScheme.error)
            Button(onClick = onRetry) { Text("Reintentar") }
        }
    }
}

@Composable
private fun ErrorBanner(message: String, onRetry: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(message, color = MaterialTheme.colorScheme.error)
            TextButton(onClick = onRetry) { Text("Reintentar") }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PeerReviewQueuePreview() {
    MentorlyTheme {
        PeerReviewQueueContent(
            uiState = PeerReviewQueueUiState(
                queueItems = listOf(
                    ReviewQueueItem(
                        submissionId = "submission-1",
                        activityId = "activity-1",
                        activityTitle = "Repositorio de Compose",
                        evidenceType = EvidenceType.URL,
                        evidenceContent = "https://github.com/example/repo",
                        submittedAtUtc = "2026-08-12"
                    )
                )
            ),
            onSubmissionClick = {},
            onHistoryClick = {},
            onRetry = {}
        )
    }
}
