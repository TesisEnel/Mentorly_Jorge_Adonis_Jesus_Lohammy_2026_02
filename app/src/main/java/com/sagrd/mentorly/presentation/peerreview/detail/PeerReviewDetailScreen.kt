package com.sagrd.mentorly.presentation.peerreview.detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sagrd.mentorly.domain.model.peerreview.PeerReviewResult
import com.sagrd.mentorly.domain.model.peerreview.PeerReviewRubricCriterion
import com.sagrd.mentorly.domain.model.submission.AnonymousSubmission
import com.sagrd.mentorly.domain.model.submission.EvidenceType
import com.sagrd.mentorly.domain.model.submission.SubmissionStatus
import com.sagrd.mentorly.ui.theme.MentorlyTheme
import com.sagrd.mentorly.util.DateFormatter

private val PrimaryBlue = Color(0xFF0D62D9)
private val AnonymousContainer = Color(0xFFEDE7F6)
private val AnonymousContent = Color(0xFF6D6478)
private val EvidenceContainerBg = Color(0xFFF7F6FB)
private val ErrorRed = Color(0xFFE53935)
private val ApprovedGreen = Color(0xFF2E7D32)
private val ApprovedGreenBg = Color(0xFFF0FDF4)
private val ChangesAmber = Color(0xFFD97706)
private val ChangesAmberBg = Color(0xFFFFFBEB)

@Composable
fun PeerReviewDetailScreen(
    submissionId: String,
    onBackClick: () -> Unit,
    onReviewCompleted: () -> Unit,
    viewModel: PeerReviewDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

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
                title = {
                    Text(
                        text = "Revisión anónima",
                        color = PrimaryBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
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
            if (uiState.submission != null && uiState.result == null) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 8.dp
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Button(
                            onClick = { onEvent(PeerReviewDetailUiEvent.Submit) },
                            enabled = !uiState.isSubmitting,
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            if (uiState.isSubmitting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Send,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Enviar revisión",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        when {
            uiState.isLoading && uiState.submission == null -> LoadingContent(
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )

            uiState.errorMessage != null && uiState.submission == null -> ErrorContent(
                message = uiState.errorMessage,
                onRetry = { onEvent(PeerReviewDetailUiEvent.Retry) },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )

            uiState.result != null -> ReviewResultContent(
                result = uiState.result,
                onReviewCompleted = onReviewCompleted,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )

            uiState.submission != null -> ReviewForm(
                uiState = uiState,
                onEvent = onEvent,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        }
    }
}

@Composable
private fun ReviewForm(
    uiState: PeerReviewDetailUiState,
    onEvent: (PeerReviewDetailUiEvent) -> Unit,
    modifier: Modifier
) {
    val submission = uiState.submission ?: return

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Submission Header Info Card
        item {
            SubmissionInfoCard(submission = submission)
        }

        // 2. Evaluation Section (Rubric Criteria)
        if (uiState.criteria.isNotEmpty()) {
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.FactCheck,
                        contentDescription = null,
                        tint = PrimaryBlue,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Tu evaluación",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            items(uiState.criteria, key = { it.id }) { criterion ->
                CriterionCard(
                    criterion = criterion,
                    selectedScore = uiState.criterionScores[criterion.id],
                    hasError = uiState.criterionErrors.contains(criterion.id),
                    onScoreSelected = { score ->
                        onEvent(PeerReviewDetailUiEvent.CriterionScoreChanged(criterion.id, score))
                    }
                )
            }
        }

        // 3. Final Decision Section
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.RateReview,
                    contentDescription = null,
                    tint = PrimaryBlue,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Decisión final",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        item {
            DecisionCard(
                uiState = uiState,
                onEvent = onEvent
            )
        }

        if (uiState.errorMessage != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ErrorOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            text = uiState.errorMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { onEvent(PeerReviewDetailUiEvent.ClearError) }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cerrar",
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SubmissionInfoCard(submission: AnonymousSubmission) {
    val uriHandler = LocalUriHandler.current
    val isUrl = submission.evidenceType == EvidenceType.URL

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            // Activity Title
            Text(
                text = submission.activityTitle,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 26.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Badges and Date Row
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .background(AnonymousContainer, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.VisibilityOff,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp),
                        tint = AnonymousContent
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "Entrega anónima",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = AnonymousContent
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "•", color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = if (isUrl) "Enlace" else "Texto (PDF)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "•", color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = DateFormatter.format(submission.submittedAtUtc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Evidence Inner Container
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = EvidenceContainerBg),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE2E8F0)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Link,
                                contentDescription = null,
                                tint = Color(0xFF475569),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = submission.evidenceContent,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Revisar commit: ${submission.submissionId.take(7)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (isUrl && submission.evidenceContent.isNotBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = {
                                runCatching { uriHandler.openUri(submission.evidenceContent) }
                            },
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.dp, Color(0xFF90CAF9)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "Abrir enlace",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = PrimaryBlue
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                                    contentDescription = null,
                                    tint = PrimaryBlue,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CriterionCard(
    criterion: PeerReviewRubricCriterion,
    selectedScore: Int?,
    hasError: Boolean,
    onScoreSelected: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(
            width = if (hasError) 1.5.dp else 1.dp,
            color = if (hasError) ErrorRed else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = criterion.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = criterion.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .background(Color(0xFFF1F5F9), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Máximo: ${criterion.maxScore} puntos",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF64748B),
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Score Numbers Row (0 to maxScore)
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (score in 0..criterion.maxScore) {
                    val isSelected = selectedScore == score
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) PrimaryBlue else Color.Transparent)
                            .border(
                                BorderStroke(
                                    width = 1.dp,
                                    color = if (isSelected) PrimaryBlue else Color(0xFFCBD5E1)
                                ),
                                CircleShape
                            )
                            .clickable { onScoreSelected(score) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = score.toString(),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color.White else Color(0xFF334155)
                        )
                    }
                }
            }

            if (hasError) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ErrorOutline,
                        contentDescription = null,
                        tint = ErrorRed,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Debes calificar este criterio.",
                        style = MaterialTheme.typography.bodySmall,
                        color = ErrorRed,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun DecisionCard(
    uiState: PeerReviewDetailUiState,
    onEvent: (PeerReviewDetailUiEvent) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            // Option 1: Aprobar entrega
            val isApprovedSelected = uiState.isApproved == true
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isApprovedSelected) ApprovedGreenBg else Color.Transparent)
                    .border(
                        BorderStroke(
                            width = if (isApprovedSelected) 2.dp else 1.dp,
                            color = if (isApprovedSelected) ApprovedGreen else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        ),
                        RoundedCornerShape(12.dp)
                    )
                    .clickable { onEvent(PeerReviewDetailUiEvent.DecisionChanged(true)) }
                    .padding(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isApprovedSelected) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircle,
                        contentDescription = null,
                        tint = if (isApprovedSelected) ApprovedGreen else Color(0xFF475569),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Aprobar entrega",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isApprovedSelected) Color(0xFF1E293B) else Color(0xFF334155)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Option 2: Solicitar cambios
            val isChangesSelected = uiState.isApproved == false
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isChangesSelected) ChangesAmberBg else Color.Transparent)
                    .border(
                        BorderStroke(
                            width = if (isChangesSelected) 2.dp else 1.dp,
                            color = if (isChangesSelected) ChangesAmber else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        ),
                        RoundedCornerShape(12.dp)
                    )
                    .clickable { onEvent(PeerReviewDetailUiEvent.DecisionChanged(false)) }
                    .padding(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.Assignment,
                        contentDescription = null,
                        tint = if (isChangesSelected) ChangesAmber else Color(0xFF475569),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Solicitar cambios",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isChangesSelected) Color(0xFF1E293B) else Color(0xFF334155)
                    )
                }
            }

            if (uiState.decisionError != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ErrorOutline,
                        contentDescription = null,
                        tint = ErrorRed,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = uiState.decisionError,
                        color = ErrorRed,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Feedback Comment
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Comentario de retroalimentación",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "*",
                    color = ErrorRed,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = uiState.feedbackComment,
                onValueChange = { onEvent(PeerReviewDetailUiEvent.FeedbackChanged(it)) },
                placeholder = { Text("Escribe tu retroalimentación...") },
                isError = uiState.feedbackError != null,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                minLines = 4,
                enabled = !uiState.isSubmitting
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Explica tu decisión de manera respetuosa y útil.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (uiState.feedbackError != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ErrorOutline,
                        contentDescription = null,
                        tint = ErrorRed,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = uiState.feedbackError,
                        color = ErrorRed,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun ReviewResultContent(
    result: PeerReviewResult,
    onReviewCompleted: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = ApprovedGreen,
                    modifier = Modifier.size(56.dp)
                )

                Text(
                    text = "Revisión enviada",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "Aprobaciones: ${result.positiveReviews} de ${result.requiredPositiveReviews}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "Estado de la entrega: ${result.submissionStatus.label()}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = PrimaryBlue
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onReviewCompleted,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(
                        text = "Volver a la cola",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
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
    Box(modifier, contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = PrimaryBlue)
    }
}

@Composable
private fun ErrorContent(message: String, onRetry: () -> Unit, modifier: Modifier) {
    Box(modifier.padding(24.dp), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyLarge
            )
            Button(
                onClick = onRetry,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text("Reintentar")
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PeerReviewDetailPreview() {
    MentorlyTheme {
        PeerReviewDetailContent(
            uiState = PeerReviewDetailUiState(
                submission = AnonymousSubmission(
                    submissionId = "submission-1",
                    activityId = "activity-1",
                    activityTitle = "Implementación de Algoritmo de Ordenamiento",
                    evidenceType = EvidenceType.URL,
                    evidenceContent = "github.com/anon-submission/sorting-algo",
                    submittedAtUtc = "2023-10-12"
                ),
                criteria = listOf(
                    PeerReviewRubricCriterion(
                        id = "crit-1",
                        activityId = "activity-1",
                        title = "Complejidad Técnica",
                        description = "Evalúa la eficiencia y el uso de estructuras de datos adecuadas.",
                        maxScore = 5,
                        orderIndex = 1
                    ),
                    PeerReviewRubricCriterion(
                        id = "crit-2",
                        activityId = "activity-1",
                        title = "Calidad del Código",
                        description = "Evalúa la legibilidad, modularidad y seguimiento de convenciones.",
                        maxScore = 5,
                        orderIndex = 2
                    )
                ),
                criterionScores = mapOf("crit-1" to 4),
                criterionErrors = setOf("crit-2"),
                feedbackError = "Este campo es obligatorio para enviar la revisión."
            ),
            onBackClick = {},
            onReviewCompleted = {},
            onEvent = {}
        )
    }
}
