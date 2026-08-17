package com.sagrd.mentorly.presentation.peerreview.history

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sagrd.mentorly.domain.model.peerreview.PeerReview
import com.sagrd.mentorly.ui.theme.MentorlyTheme
import com.sagrd.mentorly.util.DateFormatter

private val PrimaryBlue = Color(0xFF1565C0)
private val ApprovedGreen = Color(0xFF2E7D32)
private val ChangesAmber = Color(0xFFEF6C00)
private val AnonymousContainer = Color(0xFFEDE7F6)
private val AnonymousContent = Color(0xFF6D6478)

@Composable
fun PeerReviewHistoryScreen(
    onBackClick: () -> Unit,
    viewModel: PeerReviewHistoryViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    PeerReviewHistoryBody(
        state = state,
        onEvent = viewModel::onEvent,
        onBackClick = onBackClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeerReviewHistoryBody(
    state: PeerReviewHistoryUiState,
    onEvent: (PeerReviewHistoryUiEvent) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text("Mentorly", color = PrimaryBlue, fontWeight = FontWeight.Bold, fontSize = 24.sp)
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Volver")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { onEvent(PeerReviewHistoryUiEvent.Refresh) },
                        enabled = !state.isLoading && !state.isRefreshing && state.hasSession
                    ) {
                        Icon(Icons.Outlined.Refresh, "Actualizar", tint = PrimaryBlue)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        when {
            state.isLoading && state.reviews.isEmpty() -> LoadingContent(
                Modifier.fillMaxSize().padding(innerPadding)
            )
            !state.hasSession -> ErrorContent(
                state.errorMessage ?: "No se encontró una sesión activa.",
                { onEvent(PeerReviewHistoryUiEvent.Refresh) },
                Modifier.fillMaxSize().padding(innerPadding)
            )
            else -> HistoryList(
                state = state,
                onEvent = onEvent,
                modifier = Modifier.fillMaxSize().padding(innerPadding)
            )
        }
    }
}

@Composable
private fun HistoryList(
    state: PeerReviewHistoryUiState,
    onEvent: (PeerReviewHistoryUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Historial de revisiones", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(
                    "Consulta las revisiones por pares que ya has realizado.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (state.isRefreshing) item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                CircularProgressIndicator(Modifier.size(24.dp), color = PrimaryBlue)
            }
        }

        state.errorMessage?.let { message ->
            item {
                ErrorCard(
                    message = message,
                    onRetry = { onEvent(PeerReviewHistoryUiEvent.Refresh) },
                    onDismiss = { onEvent(PeerReviewHistoryUiEvent.ClearError) }
                )
            }
        }

        if (state.reviews.isEmpty()) item { EmptyCard() }
        else items(state.reviews, key = { it.id }) { review -> ReviewHistoryCard(review) }

        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
private fun ReviewHistoryCard(review: PeerReview) {
    val resultColor = if (review.isApproved) ApprovedGreen else ChangesAmber
    val resultText = if (review.isApproved) "Entrega aprobada" else "Cambios solicitados"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Revisión realizada", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.background(AnonymousContainer, RoundedCornerShape(12.dp)).padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Outlined.VisibilityOff, null, Modifier.size(17.dp), tint = AnonymousContent)
                Spacer(Modifier.width(6.dp))
                Text("Entrega anónima", style = MaterialTheme.typography.labelMedium, color = AnonymousContent)
            }
            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.CalendarToday, null, Modifier.size(19.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.width(7.dp))
                Text(DateFormatter.format(review.createdAt), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.width(12.dp))
                Text("•", color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(Modifier.width(12.dp))
                Icon(Icons.Outlined.CheckCircle, null, Modifier.size(19.dp), tint = resultColor)
                Spacer(Modifier.width(6.dp))
                Text(resultText, style = MaterialTheme.typography.bodyMedium, color = resultColor, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(16.dp))
            Box(
                Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f), RoundedCornerShape(14.dp)).padding(16.dp)
            ) {
                Text(
                    review.feedbackComment,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f))
            Spacer(Modifier.height(12.dp))
            Text(
                "Revisión completada",
                modifier = Modifier.align(Alignment.End),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = resultColor
            )
        }
    }
}

@Composable
private fun EmptyCard() {
    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Text(
            "Aún no has realizado revisiones.",
            Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ErrorCard(message: String, onRetry: () -> Unit, onDismiss: () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer), shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(16.dp)) {
            Text(message, color = MaterialTheme.colorScheme.onErrorContainer, style = MaterialTheme.typography.bodyMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onRetry) { Text("Reintentar", color = MaterialTheme.colorScheme.onErrorContainer) }
                TextButton(onClick = onDismiss) { Text("Cerrar", color = MaterialTheme.colorScheme.onErrorContainer) }
            }
        }
    }
}

@Composable
private fun LoadingContent(modifier: Modifier) = Box(modifier, contentAlignment = Alignment.Center) {
    CircularProgressIndicator(color = PrimaryBlue)
}

@Composable
private fun ErrorContent(message: String, onRetry: () -> Unit, modifier: Modifier) {
    Box(modifier.padding(24.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyLarge)
            Button(onRetry, colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)) { Text("Reintentar") }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PeerReviewHistoryPreview() {
    MentorlyTheme {
        PeerReviewHistoryBody(
            state = PeerReviewHistoryUiState(
                reviews = listOf(
                    PeerReview("review-1", "submission-1", "student-1", true, "La solución cumple los requisitos y tiene una estructura clara.", "2026-08-12"),
                    PeerReview("review-2", "submission-2", "student-1", false, "La arquitectura es correcta, pero faltan pruebas para los estados de error.", "2026-08-10")
                )
            ),
            onEvent = {},
            onBackClick = {}
        )
    }
}
