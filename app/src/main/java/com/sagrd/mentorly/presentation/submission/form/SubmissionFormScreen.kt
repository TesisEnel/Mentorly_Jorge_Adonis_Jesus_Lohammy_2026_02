package com.sagrd.mentorly.presentation.submission.form

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sagrd.mentorly.domain.model.content.ApprovalStrategy
import com.sagrd.mentorly.domain.model.submission.EvidenceType
import com.sagrd.mentorly.ui.theme.MentorlyTheme

private val PrimaryBlue = Color(0xFF1565C0)
private val DarkerBlue = Color(0xFF0D47A1)
private val StrategyCardBg = Color(0xFFF8FAFC)
private val PendingBadgeBg = Color(0xFFFFEBEE)
private val PendingBadgeText = Color(0xFFC62828)
private val MandatoryBadgeBg = Color(0xFF1976D2)
private val OptionalBadgeBg = Color(0xFFE2E8F0)

@Composable
fun SubmissionFormScreen(
    enrollmentId: String,
    activityId: String,
    submissionId: String? = null,
    onBackClick: () -> Unit,
    onSaved: (String) -> Unit,
    viewModel: SubmissionFormViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(enrollmentId, activityId, submissionId) {
        viewModel.onEvent(SubmissionFormUiEvent.Load(enrollmentId, activityId, submissionId))
    }

    LaunchedEffect(state.savedSubmissionId) {
        state.savedSubmissionId?.let(onSaved)
    }

    SubmissionFormBody(
        state = state,
        onEvent = viewModel::onEvent,
        onBackClick = onBackClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubmissionFormBody(
    state: SubmissionFormUiState,
    onEvent: (SubmissionFormUiEvent) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = state.activityTitle,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = PrimaryBlue,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Button(
                        onClick = { onEvent(SubmissionFormUiEvent.Save) },
                        enabled = !state.isSaving && !state.isLoading,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkerBlue),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        if (state.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
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
                                    text = if (state.isEditing) "ACTUALIZAR ENTREGA" else "ENVIAR ENTREGA",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PrimaryBlue)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    BadgesRow(
                        isMandatory = state.isMandatory,
                        statusText = state.submissionStatus
                    )
                }

                if (state.activityDescription.isNotBlank()) {
                    item {
                        Text(
                            text = state.activityDescription,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 22.sp
                        )
                    }
                }

                item {
                    ApprovalStrategyCard(
                        strategy = state.approvalStrategy,
                        requiredPeerReviews = state.requiredPeerReviews
                    )
                }

                item {
                    when (state.evidenceType) {
                        EvidenceType.URL -> {
                            UrlEvidenceInputs(
                                url = state.urlContent,
                                comments = state.commentsContent,
                                error = state.evidenceContentError,
                                onUrlChange = { onEvent(SubmissionFormUiEvent.UrlContentChanged(it)) },
                                onCommentsChange = { onEvent(SubmissionFormUiEvent.CommentsContentChanged(it)) }
                            )
                        }

                        EvidenceType.TEXT -> {
                            TextEvidenceInput(
                                text = state.textContent,
                                error = state.evidenceContentError,
                                onTextChange = { onEvent(SubmissionFormUiEvent.TextContentChanged(it)) }
                            )
                        }
                    }
                }
            }
        }
    }

    state.errorMessage?.let { message ->
        AlertDialog(
            onDismissRequest = { onEvent(SubmissionFormUiEvent.DismissError) },
            title = { Text("Error al enviar") },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = { onEvent(SubmissionFormUiEvent.DismissError) }) {
                    Text("Aceptar")
                }
            }
        )
    }
}

@Composable
private fun BadgesRow(
    isMandatory: Boolean,
    statusText: String
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = if (isMandatory) MandatoryBadgeBg else OptionalBadgeBg
        ) {
            Text(
                text = if (isMandatory) "Obligatoria" else "Opcional",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (isMandatory) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = PendingBadgeBg
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Schedule,
                    contentDescription = null,
                    tint = PendingBadgeText,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = PendingBadgeText
                )
            }
        }
    }
}

@Composable
private fun ApprovalStrategyCard(
    strategy: ApprovalStrategy,
    requiredPeerReviews: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = StrategyCardBg),
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
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(PrimaryBlue),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (strategy) {
                        ApprovalStrategy.PEER_REVIEW -> Icons.Filled.Groups
                        ApprovalStrategy.AUTO -> Icons.Filled.AutoAwesome
                        ApprovalStrategy.ADMIN -> Icons.Filled.Security
                    },
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = when (strategy) {
                        ApprovalStrategy.PEER_REVIEW -> "Revisión entre pares"
                        ApprovalStrategy.AUTO -> "Aprobación automática"
                        ApprovalStrategy.ADMIN -> "Revisión del instructor"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = when (strategy) {
                        ApprovalStrategy.PEER_REVIEW -> "Al enviar, tu entrega quedará pendiente de revisión entre pares. Debes realizar $requiredPeerReviews revisiones para avanzar."
                        ApprovalStrategy.AUTO -> "Tu entrega se aprobará automáticamente al enviarla."
                        ApprovalStrategy.ADMIN -> "Un instructor evaluará tu entrega y asignará tu calificación."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
private fun UrlEvidenceInputs(
    url: String,
    comments: String,
    error: String?,
    onUrlChange: (String) -> Unit,
    onCommentsChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(
            text = "URL de tu entrega (GitHub / Drive)",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        OutlinedTextField(
            value = url,
            onValueChange = onUrlChange,
            placeholder = { Text("https://github.com/usuario/repo") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Link,
                    contentDescription = null,
                    tint = PrimaryBlue
                )
            },
            isError = error != null,
            supportingText = {
                if (error != null) {
                    Text(error, color = MaterialTheme.colorScheme.error)
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryBlue,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = "Comentarios adicionales",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        OutlinedTextField(
            value = comments,
            onValueChange = onCommentsChange,
            placeholder = { Text("Agrega cualquier detalle extra sobre tu solución...") },
            minLines = 4,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryBlue,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun TextEvidenceInput(
    text: String,
    error: String?,
    onTextChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(
            text = "Tu respuesta",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        OutlinedTextField(
            value = text,
            onValueChange = onTextChange,
            placeholder = { Text("Escribe tu respuesta aquí o pega tu código...") },
            isError = error != null,
            supportingText = {
                if (error != null) {
                    Text(error, color = MaterialTheme.colorScheme.error)
                }
            },
            minLines = 8,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryBlue,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(name = "Entrega por URL (Revisión Pares)", showBackground = true, showSystemUi = true)
@Composable
private fun SubmissionFormUrlPreview() {
    MentorlyTheme {
        SubmissionFormBody(
            state = SubmissionFormUiState(
                activityTitle = "Ejercicio: Layouts en Compose",
                activityDescription = "Implementa una pantalla con Column, Row y Box siguiendo el diseño de Figma.",
                isMandatory = true,
                submissionStatus = "Pendiente de envío",
                approvalStrategy = ApprovalStrategy.PEER_REVIEW,
                requiredPeerReviews = 3,
                evidenceType = EvidenceType.URL,
                urlContent = "https://github.com/usuario/compose-layouts",
                commentsContent = ""
            ),
            onEvent = {},
            onBackClick = {}
        )
    }
}

@Preview(name = "Respuesta Escrita (Auto Aprobación)", showBackground = true, showSystemUi = true)
@Composable
private fun SubmissionFormTextPreview() {
    MentorlyTheme {
        SubmissionFormBody(
            state = SubmissionFormUiState(
                activityTitle = "Ejercicio: Respuesta Escrita",
                activityDescription = "Describe detalladamente la arquitectura de tu solución y justifica el uso de los patrones de diseño elegidos.",
                isMandatory = true,
                submissionStatus = "Pendiente de envío",
                approvalStrategy = ApprovalStrategy.AUTO,
                requiredPeerReviews = 3,
                evidenceType = EvidenceType.TEXT,
                textContent = ""
            ),
            onEvent = {},
            onBackClick = {}
        )
    }
}
