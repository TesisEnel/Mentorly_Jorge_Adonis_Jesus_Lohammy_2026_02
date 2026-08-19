package com.sagrd.mentorly.presentation.submission.detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.InsertDriveFile
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sagrd.mentorly.domain.model.content.ApprovalStrategy
import com.sagrd.mentorly.domain.model.submission.EvidenceType
import com.sagrd.mentorly.domain.model.submission.Submission
import com.sagrd.mentorly.domain.model.submission.SubmissionReview
import com.sagrd.mentorly.domain.model.submission.SubmissionStatus
import com.sagrd.mentorly.ui.theme.MentorlyTheme
import com.sagrd.mentorly.util.DateFormatter

private val PrimaryBlue = Color(0xFF1565C0)
private val CompletedGreen = Color(0xFF2E7D32)
private val CompletedGreenBg = Color(0xFFE8F5E9)
private val PendingBadgeBg = Color(0xFFFEF3C7)
private val PendingBadgeText = Color(0xFFB45309)
private val RejectedBadgeBg = Color(0xFFFFEBEE)
private val RejectedBadgeText = Color(0xFFC62828)
private val EscalatedBadgeBg = Color(0xFFF3E8FF)
private val EscalatedBadgeText = Color(0xFF7E22CE)

@Composable
fun SubmissionDetailScreen(
    submissionId: String,
    onBackClick: () -> Unit,
    onEditClick: (submissionId: String, enrollmentId: String, activityId: String) -> Unit,
    viewModel: SubmissionDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(submissionId) {
        viewModel.onEvent(SubmissionDetailUiEvent.Load(submissionId))
    }

    LifecycleResumeEffect(submissionId) {
        viewModel.onEvent(SubmissionDetailUiEvent.Refresh)
        onPauseOrDispose { }
    }

    SubmissionDetailBody(
        state = state,
        onBackClick = onBackClick,
        onEditClick = {
            state.submission?.let { submission ->
                onEditClick(submission.id, submission.enrollmentId, submission.activityId)
            }
        },
        onEscalateClick = { viewModel.onEvent(SubmissionDetailUiEvent.Escalate) },
        onDismissError = { viewModel.onEvent(SubmissionDetailUiEvent.DismissError) },
        onRetry = { viewModel.onEvent(SubmissionDetailUiEvent.Refresh) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubmissionDetailBody(
    state: SubmissionDetailUiState,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    onEscalateClick: () -> Unit,
    onDismissError: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Detalle de entrega",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver atrás"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onRetry) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Actualizar",
                            tint = PrimaryBlue
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            if (state.submission != null) {
                SubmissionBottomBar(
                    status = state.submission.status,
                    canEscalate = state.canEscalate,
                    isEscalating = state.isEscalating,
                    onBackClick = onBackClick,
                    onEditClick = onEditClick,
                    onEscalateClick = onEscalateClick
                )
            }
        }
    ) { innerPadding ->
        when {
            state.isLoading && state.submission == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PrimaryBlue)
                }
            }

            state.errorMessage != null && state.submission == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = state.errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                        Button(
                            onClick = onRetry,
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                        ) {
                            Text("Reintentar")
                        }
                    }
                }
            }

            state.submission != null -> {
                SubmissionDetailContentList(
                    state = state,
                    onEscalateClick = onEscalateClick,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                )
            }
        }
    }

    if (state.errorMessage != null && state.submission != null) {
        AlertDialog(
            onDismissRequest = onDismissError,
            title = { Text("Aviso") },
            text = { Text(state.errorMessage) },
            confirmButton = {
                TextButton(onClick = onDismissError) {
                    Text("Aceptar")
                }
            }
        )
    }
}

@Composable
private fun SubmissionDetailContentList(
    state: SubmissionDetailUiState,
    onEscalateClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val submission = state.submission ?: return

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SubmissionHeaderCard(
                submission = submission,
                approvalStrategyText = state.approvalStrategyText
            )
        }

        item {
            SubmissionTimelineStepperCard(
                submission = submission,
                reviews = state.reviews,
                requiredReviews = state.requiredReviewsCount,
                strategy = state.approvalStrategy
            )
        }

        if (submission.status == SubmissionStatus.ESCALATED) {
            item {
                EscalatedInfoCard()
            }
        }

        if (submission.status == SubmissionStatus.UNKNOWN) {
            item {
                UnknownStatusInfoCard()
            }
        }

        item {
            Text(
                text = "Evidencia entregada",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        item {
            EvidenceDeliveredCard(submission = submission)
        }

        when (state.approvalStrategy) {
            ApprovalStrategy.PEER_REVIEW -> {
                item {
                    Text(
                        text = "Revisiones de pares",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (state.reviews.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Aún no se han recibido revisiones de pares para esta entrega.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    items(state.reviews) { review ->
                        PeerReviewItemCard(review = review)
                    }
                }

                if (submission.status == SubmissionStatus.PENDING) {
                    item {
                        PendingPeerReviewEscalationSection(
                            canEscalate = state.canEscalate,
                            isEscalating = state.isEscalating,
                            onEscalateClick = onEscalateClick
                        )
                    }
                }
            }

            ApprovalStrategy.AUTO -> {
                item {
                    AutoApprovalInfoCard(status = submission.status)
                }
            }

            ApprovalStrategy.ADMIN -> {
                item {
                    AdminReviewInfoCard(status = submission.status)
                }
            }

            null -> {
                // Durante la carga no asumimos ninguna estrategia ni mostramos secciones incorrectas
            }
        }
    }
}

@Composable
private fun SubmissionHeaderCard(
    submission: Submission,
    approvalStrategyText: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = submission.activityTitle,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Entregada: ${DateFormatter.format(submission.submittedAt)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SubmissionStatusBadge(status = submission.status)

                VerticalDivider(
                    modifier = Modifier.height(24.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )

                Text(
                    text = "Evaluación: $approvalStrategyText",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SubmissionStatusBadge(status: SubmissionStatus) {
    val (bgColor, textColor, icon, label) = when (status) {
        SubmissionStatus.APPROVED -> Quadruple(
            CompletedGreenBg,
            CompletedGreen,
            Icons.Filled.CheckCircle,
            "Aprobada"
        )
        SubmissionStatus.PENDING -> Quadruple(
            PendingBadgeBg,
            PendingBadgeText,
            Icons.Filled.Schedule,
            "Pendiente"
        )
        SubmissionStatus.REJECTED -> Quadruple(
            RejectedBadgeBg,
            RejectedBadgeText,
            Icons.Filled.Close,
            "Rechazada"
        )
        SubmissionStatus.ESCALATED -> Quadruple(
            EscalatedBadgeBg,
            EscalatedBadgeText,
            Icons.Filled.Schedule,
            "Escalada"
        )
        SubmissionStatus.UNKNOWN -> Quadruple(
            Color(0xFFF1F5F9),
            Color(0xFF64748B),
            Icons.Filled.Schedule,
            "Estado desconocido"
        )
    }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = bgColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@Composable
private fun SubmissionTimelineStepperCard(
    submission: Submission,
    reviews: List<SubmissionReview>,
    requiredReviews: Int,
    strategy: ApprovalStrategy?
) {
    val positiveReviews = reviews.count { it.isApproved }

    val step2Title = when (strategy) {
        ApprovalStrategy.PEER_REVIEW -> "$positiveReviews de $requiredReviews revisiones positivas"
        ApprovalStrategy.AUTO -> "Aprobación automática"
        ApprovalStrategy.ADMIN -> "Revisión del instructor"
        null -> "Evaluación"
    }

    val step2Subtitle = when (strategy) {
        ApprovalStrategy.PEER_REVIEW -> if (positiveReviews >= requiredReviews) "Cuota completada" else "En espera de revisiones"
        ApprovalStrategy.AUTO -> "Validada por el sistema"
        ApprovalStrategy.ADMIN -> "Pendiente de decisión administrativa"
        null -> "Cargando información..."
    }

    val (step3Title, step3Subtitle, step3Color) = when (submission.status) {
        SubmissionStatus.APPROVED -> Triple(
            "Aprobada",
            "Recibió las validaciones necesarias",
            CompletedGreen
        )
        SubmissionStatus.ESCALATED -> Triple(
            "Escalada",
            "En espera de decisión administrativa",
            EscalatedBadgeText
        )
        SubmissionStatus.REJECTED -> Triple(
            "Rechazada",
            "No alcanzó los criterios de aprobación",
            RejectedBadgeText
        )
        SubmissionStatus.PENDING -> Triple(
            "En proceso",
            "En proceso de evaluación",
            MaterialTheme.colorScheme.onSurface
        )
        SubmissionStatus.UNKNOWN -> Triple(
            "Estado desconocido",
            "Sin información de estado",
            Color(0xFF64748B)
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            TimelineStepItem(
                icon = {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF1F5F9)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.InsertDriveFile,
                            contentDescription = null,
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                },
                title = "Entrega enviada",
                subtitle = DateFormatter.format(submission.submittedAt).take(12),
                showLine = true
            )

            TimelineStepItem(
                icon = {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(PrimaryBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (strategy) {
                                ApprovalStrategy.AUTO -> Icons.Filled.AutoAwesome
                                ApprovalStrategy.ADMIN -> Icons.Filled.Security
                                else -> Icons.Filled.Groups
                            },
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                },
                title = step2Title,
                subtitle = step2Subtitle,
                titleColor = PrimaryBlue,
                showLine = true
            )

            TimelineStepItem(
                icon = {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                when (submission.status) {
                                    SubmissionStatus.APPROVED -> CompletedGreenBg
                                    SubmissionStatus.ESCALATED -> EscalatedBadgeBg
                                    SubmissionStatus.REJECTED -> RejectedBadgeBg
                                    else -> Color(0xFFF1F5F9)
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (submission.status) {
                                SubmissionStatus.APPROVED -> Icons.Filled.CheckCircle
                                SubmissionStatus.ESCALATED -> Icons.Filled.Gavel
                                SubmissionStatus.REJECTED -> Icons.Filled.Close
                                else -> Icons.Filled.Schedule
                            },
                            contentDescription = null,
                            tint = step3Color,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                title = step3Title,
                subtitle = step3Subtitle,
                titleColor = step3Color,
                showLine = false
            )
        }
    }
}

@Composable
private fun TimelineStepItem(
    icon: @Composable () -> Unit,
    title: String,
    subtitle: String,
    titleColor: Color = Color.Unspecified,
    showLine: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            icon()
            if (showLine) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(26.dp)
                        .background(Color(0xFFE2E8F0))
                )
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.padding(top = 2.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (titleColor != Color.Unspecified) titleColor else MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EscalatedInfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = EscalatedBadgeBg.copy(alpha = 0.5f)),
        border = BorderStroke(1.dp, EscalatedBadgeText.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Security,
                contentDescription = null,
                tint = EscalatedBadgeText,
                modifier = Modifier.size(24.dp)
            )
            Column {
                Text(
                    text = "Tu entrega fue enviada a un administrador para auditoría.",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = EscalatedBadgeText
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Recibirás el resultado cuando se tome una decisión.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun UnknownStatusInfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Info,
                contentDescription = null,
                tint = Color(0xFF64748B),
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = "El estado actual de esta entrega no pudo determinarse. Por favor actualiza o consulta más tarde.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AutoApprovalInfoCard(status: SubmissionStatus) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (status == SubmissionStatus.APPROVED) CompletedGreenBg else Color(0xFFF8FAFC)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.AutoAwesome,
                contentDescription = null,
                tint = if (status == SubmissionStatus.APPROVED) CompletedGreen else PrimaryBlue,
                modifier = Modifier.size(24.dp)
            )
            Column {
                Text(
                    text = if (status == SubmissionStatus.APPROVED) "Aprobación automática completada" else "Aprobación automática en curso",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Esta actividad no requiere revisión entre pares.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun AdminReviewInfoCard(status: SubmissionStatus) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Security,
                contentDescription = null,
                tint = PrimaryBlue,
                modifier = Modifier.size(24.dp)
            )
            Column {
                Text(
                    text = if (status == SubmissionStatus.APPROVED) "Aprobado por el instructor" else "Pendiente de decisión administrativa",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Un instructor evaluará tu entrega y registrará la retroalimentación.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun EvidenceDeliveredCard(submission: Submission) {
    val uriHandler = LocalUriHandler.current
    val isUrl = submission.evidenceType == EvidenceType.URL
    val isGithub = submission.evidenceContent.contains("github.com", ignoreCase = true)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = isUrl && submission.evidenceContent.isNotBlank()) {
                runCatching { uriHandler.openUri(submission.evidenceContent) }
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF1F5F9)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isUrl) Icons.Outlined.Code else Icons.Outlined.Link,
                    contentDescription = null,
                    tint = Color(0xFF475569),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isGithub) "Repositorio de GitHub" else if (isUrl) "Enlace entregado" else "Respuesta textual",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = submission.evidenceContent,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isUrl) PrimaryBlue else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = if (isUrl) 1 else 6,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFF1F5F9)
                ) {
                    Text(
                        text = if (isUrl) "Evidencia: enlace externo" else "Evidencia: texto",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF64748B),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            if (isUrl) {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                    contentDescription = "Abrir enlace",
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun PeerReviewItemCard(review: SubmissionReview) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF1F5F9)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Person,
                            contentDescription = null,
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = "Revisor anónimo",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (review.isApproved) CompletedGreenBg else RejectedBadgeBg
                    ) {
                        Text(
                            text = if (review.isApproved) "Aprobada" else "No aprobada",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (review.isApproved) CompletedGreen else RejectedBadgeText,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    Text(
                        text = DateFormatter.format(review.reviewedAt).take(12),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (review.feedbackComment.isNotBlank()) {
                Text(
                    text = "\"${review.feedbackComment}\"",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
private fun PendingPeerReviewEscalationSection(
    canEscalate: Boolean,
    isEscalating: Boolean,
    onEscalateClick: () -> Unit
) {
    if (!canEscalate) {
        Text(
            text = "Podrás escalar esta entrega si permanece sin resolver durante 72 horas.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
        )
    } else {
        OutlinedButton(
            onClick = onEscalateClick,
            enabled = !isEscalating,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, EscalatedBadgeText),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isEscalating) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = EscalatedBadgeText,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "Escalar entrega",
                    color = EscalatedBadgeText,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun SubmissionBottomBar(
    status: SubmissionStatus,
    canEscalate: Boolean,
    isEscalating: Boolean,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    onEscalateClick: () -> Unit
) {
    val canEdit = status == SubmissionStatus.PENDING || status == SubmissionStatus.REJECTED

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (canEdit) {
                Button(
                    onClick = onEditClick,
                    enabled = !isEscalating,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Editar entrega",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            if (canEscalate && status != SubmissionStatus.APPROVED && status != SubmissionStatus.ESCALATED) {
                OutlinedButton(
                    onClick = onEscalateClick,
                    enabled = !isEscalating,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.5.dp, EscalatedBadgeText),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    if (isEscalating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = EscalatedBadgeText,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Filled.Gavel,
                            contentDescription = null,
                            tint = EscalatedBadgeText,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Escalar entrega",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = EscalatedBadgeText
                        )
                    }
                }
            }

            OutlinedButton(
                onClick = onBackClick,
                enabled = !isEscalating,
                shape = RoundedCornerShape(14.dp),
                border = if (!canEdit) BorderStroke(1.5.dp, PrimaryBlue) else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (!canEdit) 50.dp else 46.dp)
            ) {
                Text(
                    text = "VOLVER AL CURSO",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (!canEdit) PrimaryBlue else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview(name = "1. Entrega Pendiente Peer Review", showBackground = true, showSystemUi = true)
@Composable
private fun SubmissionDetailPendingPreview() {
    MentorlyTheme {
        SubmissionDetailBody(
            state = SubmissionDetailUiState(
                isLoading = false,
                requiredReviewsCount = 3,
                approvalStrategy = ApprovalStrategy.PEER_REVIEW,
                approvalStrategyText = "Revisión entre pares",
                canEscalate = false,
                submission = Submission(
                    id = "sub-1",
                    enrollmentId = "enr-1",
                    activityId = "act-1",
                    activityTitle = "Ejercicio: Layouts en Compose",
                    evidenceType = EvidenceType.URL,
                    evidenceContent = "https://github.com/usuario/proyecto-compose",
                    status = SubmissionStatus.PENDING,
                    submittedAt = "2026-08-16T14:30:00Z"
                ),
                reviews = listOf(
                    SubmissionReview(
                        id = "rev-1",
                        isApproved = true,
                        feedbackComment = "Buen progreso inicial en la estructura del código.",
                        reviewedAt = "2026-08-16T15:00:00Z"
                    )
                )
            ),
            onBackClick = {},
            onEditClick = {},
            onEscalateClick = {},
            onDismissError = {},
            onRetry = {}
        )
    }
}

@Preview(name = "2. Entrega Aprobada Peer Review", showBackground = true, showSystemUi = true)
@Composable
private fun SubmissionDetailApprovedPreview() {
    MentorlyTheme {
        SubmissionDetailBody(
            state = SubmissionDetailUiState(
                isLoading = false,
                requiredReviewsCount = 3,
                approvalStrategy = ApprovalStrategy.PEER_REVIEW,
                approvalStrategyText = "Revisión entre pares",
                canEscalate = false,
                submission = Submission(
                    id = "sub-2",
                    enrollmentId = "enr-1",
                    activityId = "act-1",
                    activityTitle = "Ejercicio: Layouts en Compose",
                    evidenceType = EvidenceType.URL,
                    evidenceContent = "https://github.com/usuario/proyecto-compose",
                    status = SubmissionStatus.APPROVED,
                    submittedAt = "2026-08-12T14:30:00Z"
                ),
                reviews = listOf(
                    SubmissionReview(
                        id = "rev-1",
                        isApproved = true,
                        feedbackComment = "Excelente implementación de los componentes de Material 3. El código es muy legible y sigue buenas prácticas de Compose.",
                        reviewedAt = "2026-08-12T15:00:00Z"
                    ),
                    SubmissionReview(
                        id = "rev-2",
                        isApproved = true,
                        feedbackComment = "Muy buen uso de LazyColumn. Como sugerencia, podrías extraer los estilos para facilitar el mantenimiento.",
                        reviewedAt = "2026-08-12T15:30:00Z"
                    ),
                    SubmissionReview(
                        id = "rev-3",
                        isApproved = true,
                        feedbackComment = "La solución cumple los criterios y maneja correctamente los estados de pantalla.",
                        reviewedAt = "2026-08-12T16:00:00Z"
                    )
                )
            ),
            onBackClick = {},
            onEditClick = {},
            onEscalateClick = {},
            onDismissError = {},
            onRetry = {}
        )
    }
}

@Preview(name = "3. Entrega Rechazada Peer Review", showBackground = true, showSystemUi = true)
@Composable
private fun SubmissionDetailRejectedPreview() {
    MentorlyTheme {
        SubmissionDetailBody(
            state = SubmissionDetailUiState(
                isLoading = false,
                requiredReviewsCount = 3,
                approvalStrategy = ApprovalStrategy.PEER_REVIEW,
                approvalStrategyText = "Revisión entre pares",
                canEscalate = true,
                submission = Submission(
                    id = "sub-3",
                    enrollmentId = "enr-1",
                    activityId = "act-1",
                    activityTitle = "Ejercicio: Layouts en Compose",
                    evidenceType = EvidenceType.URL,
                    evidenceContent = "https://github.com/usuario/proyecto-compose",
                    status = SubmissionStatus.REJECTED,
                    submittedAt = "2026-08-10T14:30:00Z"
                ),
                reviews = listOf(
                    SubmissionReview(
                        id = "rev-1",
                        isApproved = false,
                        feedbackComment = "Faltan implementar las columnas anidadas solicitadas en la consigna.",
                        reviewedAt = "2026-08-10T16:00:00Z"
                    )
                )
            ),
            onBackClick = {},
            onEditClick = {},
            onEscalateClick = {},
            onDismissError = {},
            onRetry = {}
        )
    }
}

@Preview(name = "4. Entrega Escalada Peer Review", showBackground = true, showSystemUi = true)
@Composable
private fun SubmissionDetailEscalatedPreview() {
    MentorlyTheme {
        SubmissionDetailBody(
            state = SubmissionDetailUiState(
                isLoading = false,
                requiredReviewsCount = 3,
                approvalStrategy = ApprovalStrategy.PEER_REVIEW,
                approvalStrategyText = "Revisión entre pares",
                canEscalate = false,
                submission = Submission(
                    id = "sub-4",
                    enrollmentId = "enr-1",
                    activityId = "act-1",
                    activityTitle = "Ejercicio: Layouts en Compose",
                    evidenceType = EvidenceType.URL,
                    evidenceContent = "https://github.com/usuario/proyecto-compose",
                    status = SubmissionStatus.ESCALATED,
                    submittedAt = "2026-08-08T14:30:00Z"
                ),
                reviews = emptyList()
            ),
            onBackClick = {},
            onEditClick = {},
            onEscalateClick = {},
            onDismissError = {},
            onRetry = {}
        )
    }
}

@Preview(name = "5. Entrega Automática Aprobada", showBackground = true, showSystemUi = true)
@Composable
private fun SubmissionDetailAutoPreview() {
    MentorlyTheme {
        SubmissionDetailBody(
            state = SubmissionDetailUiState(
                isLoading = false,
                requiredReviewsCount = 0,
                approvalStrategy = ApprovalStrategy.AUTO,
                approvalStrategyText = "Aprobación automática",
                canEscalate = false,
                submission = Submission(
                    id = "sub-5",
                    enrollmentId = "enr-1",
                    activityId = "act-2",
                    activityTitle = "Quiz: Fundamentos de Compose",
                    evidenceType = EvidenceType.TEXT,
                    evidenceContent = "100% de respuestas correctas.",
                    status = SubmissionStatus.APPROVED,
                    submittedAt = "2026-08-16T10:00:00Z"
                ),
                reviews = emptyList()
            ),
            onBackClick = {},
            onEditClick = {},
            onEscalateClick = {},
            onDismissError = {},
            onRetry = {}
        )
    }
}
