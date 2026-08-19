package com.sagrd.mentorly.presentation.admin.peerreview.audit

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Rule
import androidx.compose.material.icons.automirrored.filled.ShortText
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sagrd.mentorly.domain.model.peerreview.PeerReviewAudit
import com.sagrd.mentorly.domain.model.peerreview.PeerReviewCriterionScore
import com.sagrd.mentorly.domain.model.submission.EvidenceType
import com.sagrd.mentorly.ui.theme.MentorlyTheme
import com.sagrd.mentorly.util.formatDate

@Composable
fun PeerReviewAuditScreen(
    peerReviewId: String,
    onBackClick: () -> Unit,
    viewModel: PeerReviewAuditViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(peerReviewId) {
        viewModel.setPeerReviewId(peerReviewId)
    }

    PeerReviewAuditContent(
        state = state,
        onBackClick = onBackClick,
        onRetry = { viewModel.onEvent(PeerReviewAuditUiEvent.Retry) },
        onClearError = { viewModel.onEvent(PeerReviewAuditUiEvent.ClearError) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PeerReviewAuditContent(
    state: PeerReviewAuditUiState,
    onBackClick: () -> Unit,
    onRetry: () -> Unit,
    onClearError: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Auditoría de revisión", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
                actions = {
                    IconButton(onClick = { /* More options */ }) {
                        Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Opciones")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
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
                        text = "No se encontró la revisión solicitada.",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    AuditDetailsList(audit = state.audit)
                }
            }
        }
    }
}

@Composable
private fun AuditDetailsList(audit: PeerReviewAudit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Red Confidential Banner
        Surface(
            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Auditoría administrativa — información confidencial",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Context
        AuditSection(title = "Información de auditoría", icon = Icons.Default.Info) {
            AuditField(label = "ID DE REVISIÓN", value = audit.peerReviewId)
            AuditField(label = "ID DE ENTREGA", value = audit.submissionId)
            AuditField(label = "FECHA DE REVISIÓN", value = formatDate(audit.createdAt))
        }

        // Participants
        AuditSection(title = "Participantes (Confidencial)", icon = Icons.Default.Group) {
            AuditField(label = "ID AUTOR DE LA ENTREGA", value = audit.authorStudentId)
            Spacer(modifier = Modifier.height(8.dp))
            AuditField(label = "ID REVISOR", value = audit.reviewerStudentId)
        }

        // Decision
        AuditSection(title = "Decisión", icon = Icons.Default.Gavel) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                StatusBadge(isApproved = audit.isApproved)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "\"${audit.feedbackComment}\"",
                        style = MaterialTheme.typography.bodyMedium,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Registrado: ${formatDate(audit.createdAt)}",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Evidence
        AuditSection(title = "Evidencia de la entrega", icon = Icons.Default.Assignment) {
            val uriHandler = LocalUriHandler.current
            val isUrl = audit.evidenceType == EvidenceType.URL

            Column {
                Text(text = "CONTENIDO DE ENTREGA", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .padding(12.dp)
                            .then(if (isUrl) Modifier.clickable { uriHandler.openUri(audit.evidenceContent) } else Modifier),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isUrl) Icons.Default.Link else Icons.AutoMirrored.Filled.ShortText,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = audit.evidenceContent,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            textDecoration = if (isUrl) androidx.compose.ui.text.style.TextDecoration.Underline else null,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // Rubric Scores
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.AutoMirrored.Filled.Rule, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = "Puntuaciones de rúbrica", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }

            if (audit.criterionScores.isEmpty()) {
                Text("No hay puntuaciones registradas.", style = MaterialTheme.typography.bodyMedium)
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column {
                        audit.criterionScores.forEach { score ->
                            CriterionScoreItem(score = score)
                            if (score != audit.criterionScores.last()) {
                                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
private fun CriterionScoreItem(score: PeerReviewCriterionScore) {
    Column(modifier = Modifier.padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Criterio ID: ${score.rubricCriterionId.take(8)}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            Text(
                text = "Puntaje: ${score.score}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun AuditSection(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
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
    isBold: Boolean = false
) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun StatusBadge(isApproved: Boolean) {
    Surface(
        color = if (isApproved) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
        contentColor = if (isApproved) Color(0xFF2E7D32) else Color(0xFFC62828),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isApproved) Icons.Default.CheckCircle else Icons.Default.Cancel,
                contentDescription = null,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = if (isApproved) "APROBADA" else "RECHAZADA",
                style = MaterialTheme.typography.labelSmall,
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
fun PeerReviewAuditScreenPreview() {
    MentorlyTheme {
        AuditDetailsList(
            audit = PeerReviewAudit(
                peerReviewId = "rev-123",
                submissionId = "sub-456",
                authorStudentId = "s1",
                reviewerStudentId = "s2",
                isApproved = true,
                feedbackComment = "El análisis de los algoritmos es sólido y cubre todos los casos de borde requeridos en el enunciado.",
                createdAt = "2023-10-24T14:32:00Z",
                evidenceType = EvidenceType.URL,
                evidenceContent = "github.com/mentorly/ent-4091-cs/pull/12",
                criterionScores = listOf(
                    PeerReviewCriterionScore("c1", 4),
                    PeerReviewCriterionScore("c2", 5),
                    PeerReviewCriterionScore("c3", 3)
                )
            )
        )
    }
}

