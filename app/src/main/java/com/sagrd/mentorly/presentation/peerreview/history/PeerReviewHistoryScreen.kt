package com.sagrd.mentorly.presentation.peerreview.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import com.sagrd.mentorly.domain.model.peerreview.PeerReview
import com.sagrd.mentorly.ui.theme.MentorlyTheme

@Composable
fun PeerReviewHistoryScreen(
    onBackClick: () -> Unit,
    viewModel: PeerReviewHistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    PeerReviewHistoryContent(
        uiState = uiState,
        onBackClick = onBackClick,
        onRetry = { viewModel.onEvent(PeerReviewHistoryUiEvent.Refresh) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PeerReviewHistoryContent(
    uiState: PeerReviewHistoryUiState,
    onBackClick: () -> Unit,
    onRetry: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Mis revisiones", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { innerPadding ->
        when {
            uiState.isLoading && uiState.reviews.isEmpty() -> LoadingContent(
                Modifier.fillMaxSize().padding(innerPadding)
            )

            uiState.errorMessage != null && uiState.reviews.isEmpty() -> ErrorContent(
                message = uiState.errorMessage,
                onRetry = onRetry,
                modifier = Modifier.fillMaxSize().padding(innerPadding)
            )

            uiState.reviews.isEmpty() -> EmptyContent(
                Modifier.fillMaxSize().padding(innerPadding)
            )

            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                uiState.errorMessage?.let { message ->
                    item { Text(message, color = MaterialTheme.colorScheme.error) }
                }

                items(uiState.reviews, key = { it.id }) { review ->
                    ReviewHistoryCard(review)
                }
            }
        }
    }
}

@Composable
private fun ReviewHistoryCard(review: PeerReview) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Revisión realizada", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(
                text = if (review.isApproved) "Aprobaste la entrega" else "Solicitaste cambios",
                color = if (review.isApproved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary,
                style = MaterialTheme.typography.labelLarge
            )
            Text(review.feedbackComment, maxLines = 3, overflow = TextOverflow.Ellipsis)
            Text("Fecha: ${review.createdAt}", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun LoadingContent(modifier: Modifier) {
    Box(modifier, contentAlignment = Alignment.Center) { CircularProgressIndicator() }
}

@Composable
private fun EmptyContent(modifier: Modifier) {
    Box(modifier.padding(24.dp), contentAlignment = Alignment.Center) {
        Text("Aún no has realizado revisiones.", style = MaterialTheme.typography.bodyLarge)
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

@Preview(showBackground = true)
@Composable
private fun PeerReviewHistoryPreview() {
    MentorlyTheme {
        PeerReviewHistoryContent(
            uiState = PeerReviewHistoryUiState(
                reviews = listOf(
                    PeerReview("review-1", "submission-1", "student-1", true, "La solución cumple los requisitos y tiene una estructura clara.", "2026-08-12")
                )
            ),
            onBackClick = {},
            onRetry = {}
        )
    }
}
