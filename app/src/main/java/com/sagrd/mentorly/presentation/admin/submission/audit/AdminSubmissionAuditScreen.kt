package com.sagrd.mentorly.presentation.admin.submission.audit

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sagrd.mentorly.domain.model.submission.AdminPeerReviewAuditItem
import com.sagrd.mentorly.domain.model.submission.AdminSubmissionAudit
import com.sagrd.mentorly.domain.model.submission.SubmissionStatus
import com.sagrd.mentorly.ui.theme.MentorlyTheme

@Composable
fun AdminSubmissionAuditScreen(
    submissionId: String,
    onBackClick: () -> Unit,
    onDecisionCompleted: () -> Unit,
    viewModel: AdminSubmissionAuditViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(submissionId) {
        viewModel.setSubmissionId(submissionId)
    }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.onEvent(AdminSubmissionAuditUiEvent.ClearSuccessMessage)
            onDecisionCompleted()
        }
    }

    AdminSubmissionAuditContent(
        uiState = uiState,
        onBackClick = onBackClick,
        onRetry = { viewModel.onEvent(AdminSubmissionAuditUiEvent.Retry) },
        onClearError = { viewModel.onEvent(AdminSubmissionAuditUiEvent.ClearError) },
        onApproveClick = { viewModel.onEvent(AdminSubmissionAuditUiEvent.RequestDecision(true)) },
        onRejectClick = { viewModel.onEvent(AdminSubmissionAuditUiEvent.RequestDecision(false)) }
    )

    if (uiState.pendingDecision != null) {
        val isApproved = uiState.pendingDecision!!
        AlertDialog(
            onDismissRequest = { viewModel.onEvent(AdminSubmissionAuditUiEvent.DismissDecisionDialog) },
            title = { Text(if (isApproved) "Confirmar aprobación" else "Confirmar rechazo") },
            text = { Text("¿Deseas ${if (isApproved) "aprobar" else "rechazar"} esta entrega administrativamente?") },
            confirmButton = {
                TextButton(onClick = { viewModel.onEvent(AdminSubmissionAuditUiEvent.ConfirmDecision) }) {
                    Text(if (isApproved) "Aprobar" else "Rechazar")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onEvent(AdminSubmissionAuditUiEvent.DismissDecisionDialog) }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminSubmissionAuditContent(
    uiState: AdminSubmissionAuditUiState,
    onBackClick: () -> Unit,
    onRetry: () -> Unit,
    onClearError: () -> Unit,
    onApproveClick: () -> Unit,
    onRejectClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Auditoría de entrega") },
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
                        text = "No se encontró la entrega solicitada.",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    AuditDetails(
                        audit = uiState.audit,
                        isDeciding = uiState.isDeciding,
                        onApproveClick = onApproveClick,
                        onRejectClick = onRejectClick
                    )
                }
            }
        }
    }
}

@Composable
private fun AuditDetails(
    audit: AdminSubmissionAudit,
    isDeciding: Boolean,
    onApproveClick: () -> Unit,
    onRejectClick: () -> Unit
) {
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

        AuditSection(title = "Curso y Actividad", icon = Icons.Default.MenuBook) {
            AuditField(label = "Curso", value = audit.courseTitle)
            AuditField(label = "Actividad", value = audit.activityTitle)
        }

        AuditSection(title = "Autor", icon = Icons.Default.Person) {
            AuditField(label = "Nombre", value = audit.authorDisplayName)
            AuditField(label = "Correo", value = audit.authorEmail)
            AuditField(label = "ID de Autor", value = audit.authorStudentId)
        }

        AuditSection(title = "Entrega", icon = Icons.Default.Assignment) {
            AuditField(label = "ID de Entrega", value = audit.submissionId)
            AuditField(label = "Estado Actual", value = audit.status.name)
            AuditField(label = "Fecha de Envío", value = audit.submittedAtUtc)
            AuditField(label = "Evidencia", value = audit.evidenceUrl)
        }

        AuditSection(title = "Revisiones por Pares", icon = Icons.Default.Group) {
            if (audit.peerReviews.isEmpty()) {
                Text("No hay revisiones registradas.", style = MaterialTheme.typography.bodySmall)
            } else {
                audit.peerReviews.forEach { review ->
                    PeerReviewAuditItemView(review = review)
                    if (review != audit.peerReviews.last()) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    }
                }
            }
        }

        if (audit.status == SubmissionStatus.ESCALATED) {
            AuditSection(title = "Decisión Administrativa", icon = Icons.Default.Gavel) {
                Text(
                    text = "Esta entrega ha sido escalada y requiere una decisión definitiva.",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onApproveClick,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        enabled = !isDeciding
                    ) {
                        if (isDeciding) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                        else Text("Aprobar")
                    }
                    Button(
                        onClick = onRejectClick,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                        enabled = !isDeciding
                    ) {
                        if (isDeciding) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                        else Text("Rechazar")
                    }
                }
            }
        } else {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Text(
                    text = "Esta entrega no está disponible para decisión administrativa.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun PeerReviewAuditItemView(review: AdminPeerReviewAuditItem) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = review.reviewerDisplayName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Text(text = review.reviewerEmail, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            }
            StatusLabel(isApproved = review.isApproved)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = review.feedbackComment, style = MaterialTheme.typography.bodySmall)
        Text(
            text = review.createdAtUtc,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.align(Alignment.End)
        )
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
private fun AdminSubmissionAuditScreenPreview() {
    MentorlyTheme {
        AuditDetails(
            audit = AdminSubmissionAudit(
                submissionId = "sub-1",
                enrollmentId = "e1",
                authorStudentId = "s1",
                authorDisplayName = "Juan Perez",
                authorEmail = "juan@example.com",
                courseId = "c1",
                courseTitle = "Android con Compose",
                activityId = "a1",
                activityTitle = "Laboratorio 1",
                evidenceUrl = "https://github.com/juan/repo",
                status = SubmissionStatus.ESCALATED,
                submittedAtUtc = "2026-08-14",
                reviewedAtUtc = null,
                peerReviews = listOf(
                    AdminPeerReviewAuditItem(
                        peerReviewId = "r1",
                        reviewerStudentId = "s2",
                        reviewerDisplayName = "Maria Lopez",
                        reviewerEmail = "maria@example.com",
                        isApproved = true,
                        feedbackComment = "Buen trabajo.",
                        createdAtUtc = "2026-08-14"
                    )
                )
            ),
            isDeciding = false,
            onApproveClick = {},
            onRejectClick = {}
        )
    }
}
