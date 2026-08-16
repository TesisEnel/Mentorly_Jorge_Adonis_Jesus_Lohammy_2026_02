package com.sagrd.mentorly.presentation.submission.detail

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.sagrd.mentorly.domain.model.submission.Submission
import com.sagrd.mentorly.domain.model.submission.EvidenceType
import com.sagrd.mentorly.domain.model.submission.SubmissionReview
import com.sagrd.mentorly.domain.model.submission.SubmissionStatus
import com.sagrd.mentorly.ui.theme.MentorlyTheme
import com.sagrd.mentorly.util.DateFormatter

@Composable
fun SubmissionDetailScreen(
    submissionId: String,
    onBackClick: () -> Unit,
    onEditClick: (submissionId: String, enrollmentId: String, activityId: String) -> Unit,
    viewModel: SubmissionDetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(submissionId) {
        viewModel.onEvent(SubmissionDetailUiEvent.Load(submissionId))
    }

    SubmissionDetailContent(
        state = state,
        onEvent = viewModel::onEvent,
        onBackClick = onBackClick,
        onEditClick = {
            state.submission?.let { submission ->
                onEditClick(
                    submission.id,
                    submission.enrollmentId,
                    submission.activityId
                )
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SubmissionDetailContent(
    state: SubmissionDetailUiState,
    onEvent: (SubmissionDetailUiEvent) -> Unit,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Detalle de entrega",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { innerPadding ->
        when {
            state.isLoading && state.submission == null -> {
                LoadingContent(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                )
            }

            state.errorMessage != null && state.submission == null -> {
                ErrorContent(
                    message = state.errorMessage,
                    onRetry = { onEvent(SubmissionDetailUiEvent.Refresh) },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                )
            }

            state.submission != null -> {
                val submission = state.submission
                val canEscalate = submission.status == SubmissionStatus.PENDING ||
                        submission.status == SubmissionStatus.REJECTED

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Text("Evidencia", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                Spacer(Modifier.height(4.dp))
                                Text(submission.evidenceContent, style = MaterialTheme.typography.bodyMedium)
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    "Estado: ${submission.status.displayName()}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    "Enviado: ${DateFormatter.format(submission.submittedAt)}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }

                    item {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedButton(onClick = onEditClick, modifier = Modifier.weight(1f)) {
                                Text("Editar evidencia")
                            }
                            if (canEscalate) {
                                Button(
                                    onClick = { onEvent(SubmissionDetailUiEvent.Escalate) },
                                    enabled = !state.isEscalating,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    if (state.isEscalating) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(18.dp),
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Text("Escalar entrega")
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Text(
                            "Retroalimentación",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    if (state.reviews.isEmpty()) {
                        item {
                            Text(
                                "Aún no hay revisiones para esta entrega.",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    } else {
                        items(state.reviews, key = { it.id }) { review ->
                            SubmissionReviewItem(review)
                        }
                    }
                }
            }
        }
    }

    state.errorMessage?.let { message ->
        if (state.submission != null) {
            AlertDialog(
                onDismissRequest = { onEvent(SubmissionDetailUiEvent.DismissError) },
                title = { Text("Error") },
                text = { Text(message) },
                confirmButton = {
                    TextButton(onClick = { onEvent(SubmissionDetailUiEvent.DismissError) }) { Text("Aceptar") }
                }
            )
        }
    }
}

@Composable
private fun SubmissionReviewItem(review: SubmissionReview) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = if (review.isApproved) "Aprobado" else "Rechazado",
                fontWeight = FontWeight.SemiBold,
                color = if (review.isApproved) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                }
            )
            Spacer(Modifier.height(4.dp))
            Text(review.feedbackComment, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(4.dp))
            Text(DateFormatter.format(review.reviewedAt), style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun LoadingContent(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.padding(24.dp), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyLarge)
            Button(onClick = onRetry) { Text("Reintentar") }
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
private fun SubmissionDetailPreview() {
    MentorlyTheme {
        SubmissionDetailContent(
            state = SubmissionDetailUiState(
                isLoading = false,
                submission = Submission(
                    id = "1",
                    enrollmentId = "e1",
                    activityId = "activity-1",
                    activityTitle = "Crear consultas SQL para una base de datos",
                    evidenceType = EvidenceType.URL,
                    evidenceContent = "https://github.com/user/repo",
                    status = SubmissionStatus.PENDING,
                    submittedAt = "2026-08-01"
                ),
                reviews = listOf(
                    SubmissionReview(
                        id = "r1",
                        isApproved = false,
                        feedbackComment = "Falta documentación en el README.",
                        reviewedAt = "2026-08-05"
                    )
                )
            ),
            onEvent = {},
            onBackClick = {},
            onEditClick = {}
        )
    }
}
