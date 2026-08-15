package com.sagrd.mentorly.presentation.admin.peerreview.audit

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sagrd.mentorly.domain.model.peerreview.PeerReviewAudit
import com.sagrd.mentorly.ui.theme.MentorlyTheme

@Composable
fun PeerReviewAuditScreen(
    peerReviewId: String,
    onBackClick: () -> Unit,
    viewModel: PeerReviewAuditViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(peerReviewId) {
        viewModel.setPeerReviewId(peerReviewId)
    }

    PeerReviewAuditContent(
        uiState = uiState,
        onBackClick = onBackClick,
        onRetry = { viewModel.onEvent(PeerReviewAuditUiEvent.Retry) },
        onClearError = { viewModel.onEvent(PeerReviewAuditUiEvent.ClearError) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PeerReviewAuditContent(
    uiState: PeerReviewAuditUiState,
    onBackClick: () -> Unit,
    onRetry: () -> Unit,
    onClearError: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Auditoría de revisión") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                uiState.errorMessage != null -> {
                    ErrorContent(
                        message = uiState.errorMessage,
                        onRetry = onRetry,
                        onDismiss = onClearError,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                uiState.audit == null -> {
                    Text(
                        text = "No se encontró la revisión solicitada.",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    AuditDetails(audit = uiState.audit)
                }
            }
        }
    }
}

@Composable
private fun AuditDetails(audit: PeerReviewAudit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Auditoría administrativa — información confidencial",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        AuditSection(title = "Contexto", icon = Icons.Default.History) {
            AuditField(label = "ID de Revisión", value = audit.peerReviewId)
            AuditField(label = "Fecha", value = audit.createdAt)
        }

        AuditSection(title = "Entrega", icon = Icons.Default.Assignment) {
            AuditField(label = "ID de Entrega", value = audit.submissionId)
            AuditField(label = "Evidencia", value = audit.evidenceUrl)
        }

        AuditSection(title = "Participantes", icon = Icons.Default.Person) {
            AuditField(label = "Autor (ID)", value = audit.authorStudentId)
            AuditField(label = "Revisor (ID)", value = audit.reviewerStudentId)
        }

        AuditSection(title = "Decisión", icon = if (audit.isApproved) Icons.Default.Security else Icons.Default.Security) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Estado: ", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                StatusLabel(isApproved = audit.isApproved)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Comentario:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            Text(text = audit.feedbackComment, style = MaterialTheme.typography.bodyLarge)
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun AuditSection(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                content()
            }
        }
    }
}

@Composable
private fun AuditField(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun StatusLabel(isApproved: Boolean) {
    Surface(
        color = if (isApproved) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
        contentColor = if (isApproved) Color(0xFF2E7D32) else Color(0xFFC62828),
        shape = CircleShape
    ) {
        Text(
            text = if (isApproved) "Aprobada" else "Rechazada",
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = message, color = MaterialTheme.colorScheme.error)
        Row(modifier = Modifier.padding(top = 16.dp)) {
            Button(onClick = onRetry) { Text("Reintentar") }
            Spacer(modifier = Modifier.width(8.dp))
            TextButton(onClick = onDismiss) { Text("Cerrar") }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PeerReviewAuditScreenPreview() {
    MentorlyTheme {
        PeerReviewAuditContent(
            uiState = PeerReviewAuditUiState(
                audit = PeerReviewAudit(
                    peerReviewId = "rev-123",
                    submissionId = "sub-456",
                    authorStudentId = "student-author",
                    reviewerStudentId = "student-reviewer",
                    isApproved = true,
                    feedbackComment = "El repositorio de GitHub contiene todos los requerimientos solicitados para la Unidad 1.",
                    createdAt = "2026-08-14 10:30",
                    evidenceUrl = "https://github.com/example/repo"
                )
            ),
            onBackClick = {},
            onRetry = {},
            onClearError = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PeerReviewAuditErrorPreview() {
    MentorlyTheme {
        PeerReviewAuditContent(
            uiState = PeerReviewAuditUiState(errorMessage = "Error al conectar con el servidor."),
            onBackClick = {},
            onRetry = {},
            onClearError = {}
        )
    }
}
