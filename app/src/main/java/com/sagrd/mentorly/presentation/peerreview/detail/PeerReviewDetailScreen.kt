package com.sagrd.mentorly.presentation.peerreview.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.sagrd.mentorly.domain.model.peerreview.PeerReviewResult
import com.sagrd.mentorly.domain.model.submission.AnonymousSubmission
import com.sagrd.mentorly.domain.model.submission.SubmissionStatus
import com.sagrd.mentorly.ui.theme.MentorlyTheme

@Composable
fun PeerReviewDetailScreen(
    submissionId: String,
    onBackClick: () -> Unit,
    onReviewCompleted: () -> Unit,
    viewModel: PeerReviewDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(submissionId) { viewModel.initialize(submissionId) }

    PeerReviewDetailContent(
        uiState = uiState,
        onBackClick = onBackClick,
        onReviewCompleted = onReviewCompleted,
        onEvent = viewModel::onEvent
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PeerReviewDetailContent(
    uiState: PeerReviewDetailUiState,
    onBackClick: () -> Unit,
    onReviewCompleted: () -> Unit,
    onEvent: (PeerReviewDetailUiEvent) -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Revisión anónima", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { innerPadding ->
        when {
            uiState.isLoading && uiState.submission == null -> LoadingContent(
                Modifier.fillMaxSize().padding(innerPadding)
            )

            uiState.errorMessage != null && uiState.submission == null -> ErrorContent(
                message = uiState.errorMessage,
                onRetry = { onEvent(PeerReviewDetailUiEvent.Retry) },
                modifier = Modifier.fillMaxSize().padding(innerPadding)
            )

            uiState.submission != null -> ReviewForm(
                uiState = uiState,
                onReviewCompleted = onReviewCompleted,
                onEvent = onEvent,
                modifier = Modifier.fillMaxSize().padding(innerPadding)
            )
        }
    }
}

@Composable
private fun ReviewForm(
    uiState: PeerReviewDetailUiState,
    onReviewCompleted: () -> Unit,
    onEvent: (PeerReviewDetailUiEvent) -> Unit,
    modifier: Modifier
) {
    val submission = uiState.submission ?: return

    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (uiState.result != null) {
            ReviewResultCard(uiState.result, onReviewCompleted)
            return
        }

        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(submission.activityTitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text("Entrega anónima", color = MaterialTheme.colorScheme.primary)
                Text(submission.evidenceUrl, maxLines = 3, overflow = TextOverflow.Ellipsis)
                Text("Enviada: ${submission.submittedAtUtc}", style = MaterialTheme.typography.bodySmall)
            }
        }

        Text("Tu decisión", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        DecisionButtons(
            selected = uiState.isApproved,
            onDecisionChanged = { onEvent(PeerReviewDetailUiEvent.DecisionChanged(it)) }
        )
        uiState.decisionError?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }

        OutlinedTextField(
            value = uiState.feedbackComment,
            onValueChange = { onEvent(PeerReviewDetailUiEvent.FeedbackChanged(it)) },
            label = { Text("Comentario de feedback") },
            supportingText = { Text("Explica tu decisión de manera respetuosa y útil.") },
            isError = uiState.feedbackError != null,
            modifier = Modifier.fillMaxWidth(),
            minLines = 4,
            enabled = !uiState.isSubmitting
        )
        uiState.feedbackError?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }

        uiState.errorMessage?.let { ErrorBanner(it, onRetry = { onEvent(PeerReviewDetailUiEvent.ClearError) }) }

        Button(
            onClick = { onEvent(PeerReviewDetailUiEvent.Submit) },
            enabled = !uiState.isSubmitting,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (uiState.isSubmitting) "Enviando revisión..." else "Enviar revisión")
        }
    }
}

@Composable
private fun DecisionButtons(selected: Boolean?, onDecisionChanged: (Boolean) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(
            onClick = { onDecisionChanged(true) },
            modifier = Modifier.fillMaxWidth(),
            enabled = selected != true
        ) { Text(if (selected == true) "Aprobada" else "Aprobar entrega") }
        OutlinedButton(
            onClick = { onDecisionChanged(false) },
            modifier = Modifier.fillMaxWidth(),
            enabled = selected != false
        ) { Text(if (selected == false) "Cambios solicitados" else "Solicitar cambios") }
    }
}

@Composable
private fun ReviewResultCard(result: PeerReviewResult, onReviewCompleted: () -> Unit) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Revisión enviada", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("Aprobaciones: ${result.positiveReviews} de ${result.requiredPositiveReviews}")
            Text("Estado de la entrega: ${result.submissionStatus.label()}")
            Button(onClick = onReviewCompleted, modifier = Modifier.fillMaxWidth()) {
                Text("Volver a la cola")
            }
        }
    }
}

private fun SubmissionStatus.label(): String = when (this) {
    SubmissionStatus.PENDING -> "Pendiente"
    SubmissionStatus.APPROVED -> "Aprobada"
    SubmissionStatus.REJECTED -> "Rechazada"
    SubmissionStatus.ESCALATED -> "Escalada"
    SubmissionStatus.UNKNOWN -> "Desconocido"
}

@Composable
private fun LoadingContent(modifier: Modifier) {
    Box(modifier, contentAlignment = Alignment.Center) { CircularProgressIndicator() }
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
            Button(onClick = onRetry) { Text("Aceptar") }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PeerReviewDetailPreview() {
    MentorlyTheme {
        PeerReviewDetailContent(
            uiState = PeerReviewDetailUiState(
                submission = AnonymousSubmission("submission-1", "activity-1", "Repositorio de Compose", "https://github.com/example/repo", "2026-08-12"),
                isApproved = true,
                feedbackComment = "La estructura del proyecto está bien organizada y cumple los requisitos."
            ),
            onBackClick = {},
            onReviewCompleted = {},
            onEvent = {}
        )
    }
}
