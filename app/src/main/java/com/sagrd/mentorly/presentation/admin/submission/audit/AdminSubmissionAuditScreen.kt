package com.sagrd.mentorly.presentation.admin.submission.audit

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ShortText
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sagrd.mentorly.domain.model.submission.AdminPeerReviewAuditItem
import com.sagrd.mentorly.domain.model.submission.AdminSubmissionAudit
import com.sagrd.mentorly.domain.model.submission.EvidenceType
import com.sagrd.mentorly.domain.model.submission.SubmissionStatus
import com.sagrd.mentorly.ui.theme.MentorlyTheme
import com.sagrd.mentorly.util.DateFormatter

@Composable
fun AdminSubmissionAuditScreen(
    submissionId: String,
    onBackClick: () -> Unit,
    onDecisionCompleted: () -> Unit,
    viewModel: AdminSubmissionAuditViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(submissionId) {
        viewModel.setSubmissionId(submissionId)
    }

    LaunchedEffect(state.successMessage) {
        state.successMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.onEvent(AdminSubmissionAuditUiEvent.ClearSuccessMessage)
            onDecisionCompleted()
        }
    }

    AdminSubmissionAuditContent(
        state = state,
        onBackClick = onBackClick,
        onRetry = { viewModel.onEvent(AdminSubmissionAuditUiEvent.Retry) },
        onClearError = { viewModel.onEvent(AdminSubmissionAuditUiEvent.ClearError) },
        onApproveClick = { viewModel.onEvent(AdminSubmissionAuditUiEvent.RequestDecision(true)) },
        onRejectClick = { viewModel.onEvent(AdminSubmissionAuditUiEvent.RequestDecision(false)) }
    )

    if (state.pendingDecision != null) {
        val isApproved = state.pendingDecision!!
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
    state: AdminSubmissionAuditUiState,
    onBackClick: () -> Unit,
    onRetry: () -> Unit,
    onClearError: () -> Unit,
    onApproveClick: () -> Unit,
    onRejectClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Auditoría de entrega", fontWeight = FontWeight.Bold) },
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
                state.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                state.errorMessage != null -> {
                    ErrorView(
                        message = state.errorMessage,
                        onRetry = onRetry,
                        onDismiss = onClearError,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                state.audit == null -> {
                    Text(
                        text = "No se encontró la entrega solicitada.",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    AuditDetails(
                        audit = state.audit,
                        isDeciding = state.isDeciding,
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
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Confidential Header
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Auditoría administrativa",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Información confidencial. Solo para administradores del sistema.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Submission Information
        AuditSection(title = "Información de la entrega") {
            AuditField(label = "Curso", value = audit.courseTitle, isBold = true)
            AuditField(label = "Actividad", value = audit.activityTitle, isBold = true)
            AuditField(label = "Autor", value = audit.authorDisplayName, isBold = true)
            AuditField(label = "Correo electrónico", value = audit.authorEmail, valueColor = MaterialTheme.colorScheme.primary)
        }

        // Evidence
        AuditSection(title = "Evidencia enviada") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Tipo de entrega", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val icon = if (audit.evidenceType == EvidenceType.URL) Icons.Default.Link else Icons.AutoMirrored.Filled.ShortText
                        val label = if (audit.evidenceType == EvidenceType.URL) "Enlace externo (Figma)" else "Texto"
                        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = label, style = MaterialTheme.typography.bodySmall)
                    }
                }

                StatusBadge(status = audit.status)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = audit.evidenceContent,
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    textDecoration = if (audit.evidenceType == EvidenceType.URL) androidx.compose.ui.text.style.TextDecoration.Underline else null
                )
            }
        }

        // Peer Reviews
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                text = "Revisiones por pares",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            if (audit.peerReviews.isEmpty()) {
                Text("No hay revisiones registradas.", style = MaterialTheme.typography.bodyMedium)
            } else {
                audit.peerReviews.forEach { review ->
                    ReviewAuditCard(review = review)
                }
            }
        }

        // Admin Decision
        if (audit.status == SubmissionStatus.ESCALATED) {
            AuditSection(
                title = "Decisión administrativa",
                containerBorder = BorderStroke(1.dp, Color.Red.copy(alpha = 0.5f))
            ) {
                Text(
                    text = "Como administrador, debes resolver este conflicto de evaluación. Revisa la evidencia directamente y determina si la entrega cumple con los criterios mínimos para ser aprobada o si debe ser rechazada.",
                    style = MaterialTheme.typography.bodySmall,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onApproveClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B5E20)),
                    shape = RoundedCornerShape(24.dp),
                    enabled = !isDeciding
                ) {
                    if (isDeciding) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                    } else {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Aprobar entrega (Forzar)")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onRejectClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB71C1C)),
                    shape = RoundedCornerShape(24.dp),
                    enabled = !isDeciding
                ) {
                    if (isDeciding) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                    } else {
                        Icon(imageVector = Icons.Default.Cancel, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Rechazar entrega (Forzar)")
                    }
                }
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Text(
                    text = "Esta entrega no está disponible para decisión administrativa.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
private fun AuditSection(
    title: String,
    containerBorder: BorderStroke? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = containerBorder ?: BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                content()
            }
        }
    }
}

@Composable
private fun AuditField(
    label: String,
    value: String,
    isBold: Boolean = false,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = valueColor
        )
    }
}

@Composable
private fun ReviewAuditCard(review: AdminPeerReviewAuditItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = review.reviewerDisplayName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    Text(text = review.reviewerEmail, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Surface(
                    color = if (review.isApproved) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                    contentColor = if (review.isApproved) Color(0xFF2E7D32) else Color(0xFFC62828),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = if (review.isApproved) "Aprobada" else "Rechazada",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "\"${review.feedbackComment}\"",
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodySmall,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = DateFormatter.format(review.createdAtUtc),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun StatusBadge(status: SubmissionStatus) {
    val color = when (status) {
        SubmissionStatus.PENDING -> Color(0xFF9E9E9E)
        SubmissionStatus.APPROVED -> Color(0xFF4CAF50)
        SubmissionStatus.REJECTED -> Color(0xFFF44336)
        SubmissionStatus.ESCALATED -> Color(0xFFE91E63)
        SubmissionStatus.UNKNOWN -> Color(0xFF000000)
    }
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(10.dp), tint = color)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Estado: ${status.name}",
                style = MaterialTheme.typography.labelSmall,
                color = color,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ErrorView(
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
        Text(text = message, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
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
                submissionId = "sub-123",
                enrollmentId = "e1",
                authorStudentId = "s1",
                authorDisplayName = "Carlos Mendoza",
                authorEmail = "carlos.mendoza@student.edu",
                courseId = "c1",
                courseTitle = "Introducción a Diseño UI/UX",
                activityId = "a1",
                activityTitle = "Módulo 3: Proyecto Final de Wireframes",
                evidenceType = EvidenceType.URL,
                evidenceContent = "https://figma.com/file/xyz123/Proyecto-Final-UI?node-id=0:1",
                status = SubmissionStatus.ESCALATED,
                submittedAtUtc = "2023-10-12T14:30:00Z",
                reviewedAtUtc = null,
                peerReviews = listOf(
                    AdminPeerReviewAuditItem(
                        peerReviewId = "r1",
                        reviewerStudentId = "s2",
                        reviewerDisplayName = "Ana López",
                        reviewerEmail = "ana.lopez@student.edu",
                        isApproved = true,
                        feedbackComment = "Los wireframes cumplen con todos los requisitos funcionales solicitados en la rúbrica. Buena estructura de navegación.",
                        createdAtUtc = "2023-10-12T14:30:00Z"
                    ),
                    AdminPeerReviewAuditItem(
                        peerReviewId = "r2",
                        reviewerStudentId = "s3",
                        reviewerDisplayName = "Miguel Ramírez",
                        reviewerEmail = "m.ramirez@student.edu",
                        isApproved = false,
                        feedbackComment = "El enlace proporcionado no tiene permisos de visualización públicos. No pude acceder al archivo de Figma para evaluarlo.",
                        createdAtUtc = "2023-10-13T09:15:00Z"
                    )
                )
            ),
            isDeciding = false,
            onApproveClick = {},
            onRejectClick = {}
        )
    }
}
