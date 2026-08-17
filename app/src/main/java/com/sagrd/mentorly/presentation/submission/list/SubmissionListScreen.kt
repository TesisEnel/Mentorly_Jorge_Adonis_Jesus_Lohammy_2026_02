package com.sagrd.mentorly.presentation.submission.list

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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.InsertDriveFile
import androidx.compose.material.icons.filled.Balance
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.RateReview
import androidx.compose.material.icons.outlined.Search
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sagrd.mentorly.domain.model.submission.EvidenceType
import com.sagrd.mentorly.domain.model.submission.Submission
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
private val RejectedCardBorder = Color(0xFFFFCDD2)
private val EscalatedBadgeBg = Color(0xFFF3E8FF)
private val EscalatedBadgeText = Color(0xFF7E22CE)

@Composable
fun SubmissionListScreen(
    onBackClick: () -> Unit,
    onSubmissionClick: (String) -> Unit,
    viewModel: SubmissionListViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    SubmissionListContent(
        state = state,
        onBackClick = onBackClick,
        onSubmissionClick = onSubmissionClick,
        onSearchQueryChanged = { query -> viewModel.onEvent(SubmissionListUiEvent.OnSearchQueryChanged(query)) },
        onClearSearch = { viewModel.onEvent(SubmissionListUiEvent.ClearSearch) },
        onRetry = { viewModel.onEvent(SubmissionListUiEvent.Refresh) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SubmissionListContent(
    state: SubmissionListUiState,
    onBackClick: () -> Unit,
    onSubmissionClick: (String) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onClearSearch: () -> Unit,
    onRetry: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Mis entregas",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = PrimaryBlue
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
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            SubmissionSearchBar(
                query = state.searchQuery,
                onQueryChange = onSearchQueryChanged,
                onClearQuery = onClearSearch,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            when {
                state.isLoading && state.submissions.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = PrimaryBlue)
                    }
                }

                state.errorMessage != null && state.submissions.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
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

                state.filteredSubmissions.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (state.searchQuery.isNotBlank()) {
                                "No se encontraron entregas que coincidan con \"${state.searchQuery}\"."
                            } else {
                                "Aún no tienes entregas registradas."
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(
                            items = state.filteredSubmissions,
                            key = { item -> item.submission.id }
                        ) { item ->
                            SubmissionItemCard(
                                item = item,
                                onClick = { onSubmissionClick(item.submission.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SubmissionSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClearQuery: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier,
        placeholder = {
            Text(
                text = "Buscar por actividad o curso",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = "Buscar",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = onClearQuery) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Limpiar búsqueda",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PrimaryBlue,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
private fun SubmissionItemCard(
    item: SubmissionItemUiState,
    onClick: () -> Unit
) {
    val submission = item.submission
    val isRejected = submission.status == SubmissionStatus.REJECTED
    val borderStroke = if (isRejected) {
        BorderStroke(1.dp, RejectedCardBorder)
    } else {
        BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = borderStroke
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (item.courseTitle.isNotBlank()) {
                            "CURSO: ${item.courseTitle.uppercase()}"
                        } else {
                            "CURSO"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B),
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(3.dp))

                    Text(
                        text = submission.activityTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 22.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF1F5F9)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Ver detalle",
                        tint = Color(0xFF475569),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            SubmissionItemStatusBadge(status = submission.status)

            if (isRejected) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFF1F5F9)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = null,
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Puedes editar o escalar desde el detalle",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF475569)
                        )
                    }
                }
            }

            if (submission.status != SubmissionStatus.ESCALATED && (item.hasReviewsInfo || submission.status == SubmissionStatus.PENDING || submission.status == SubmissionStatus.APPROVED)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.RateReview,
                        contentDescription = null,
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = "${item.positiveReviewsCount} de ${item.requiredReviewsCount} revisiones positivas",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF64748B)
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = if (submission.evidenceType == EvidenceType.URL) {
                        Icons.Outlined.Link
                    } else {
                        Icons.AutoMirrored.Outlined.InsertDriveFile
                    },
                    contentDescription = null,
                    tint = PrimaryBlue,
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = if (submission.evidenceType == EvidenceType.URL) {
                        "Enlace externo"
                    } else {
                        "Respuesta textual"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = PrimaryBlue
                )
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                modifier = Modifier.padding(top = 2.dp)
            )

            Text(
                text = "Enviada el ${DateFormatter.format(submission.submittedAt).substringBefore(",")}",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF64748B)
            )
        }
    }
}

@Composable
private fun SubmissionItemStatusBadge(status: SubmissionStatus) {
    val (bgColor, textColor, icon, label) = when (status) {
        SubmissionStatus.APPROVED -> Quadruple(
            CompletedGreenBg,
            CompletedGreen,
            Icons.Filled.CheckCircle,
            "Requisitos completados"
        )
        SubmissionStatus.PENDING -> Quadruple(
            PendingBadgeBg,
            PendingBadgeText,
            Icons.Filled.Schedule,
            "Esperando revisiones"
        )
        SubmissionStatus.REJECTED -> Quadruple(
            RejectedBadgeBg,
            RejectedBadgeText,
            Icons.Filled.Error,
            "Requiere ajustes"
        )
        SubmissionStatus.ESCALATED -> Quadruple(
            EscalatedBadgeBg,
            EscalatedBadgeText,
            Icons.Filled.Balance,
            "En espera de decisión administrativa"
        )
        SubmissionStatus.UNKNOWN -> Quadruple(
            Color(0xFFF1F5F9),
            Color(0xFF64748B),
            Icons.Filled.Schedule,
            "Estado desconocido"
        )
    }

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = bgColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
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

@Preview(name = "Lista de Entregas", showBackground = true, showSystemUi = true)
@Composable
private fun SubmissionListMockPreview() {
    val sampleItems = listOf(
        SubmissionItemUiState(
            submission = Submission(
                id = "sub-1",
                enrollmentId = "enr-1",
                activityId = "act-1",
                activityTitle = "Ejercicio: Layouts en Compose",
                evidenceType = EvidenceType.URL,
                evidenceContent = "https://github.com/usuario/proyecto-compose",
                status = SubmissionStatus.APPROVED,
                submittedAt = "2026-08-12T14:30:00Z"
            ),
            courseTitle = "Desarrollo Android con Kotlin",
            positiveReviewsCount = 3,
            requiredReviewsCount = 3,
            hasReviewsInfo = true
        ),
        SubmissionItemUiState(
            submission = Submission(
                id = "sub-2",
                enrollmentId = "enr-2",
                activityId = "act-2",
                activityTitle = "Proyecto: Consultas SQL",
                evidenceType = EvidenceType.TEXT,
                evidenceContent = "SELECT * FROM Users WHERE active = 1;",
                status = SubmissionStatus.PENDING,
                submittedAt = "2026-08-15T14:30:00Z"
            ),
            courseTitle = "Bases de Datos Avanzadas",
            positiveReviewsCount = 1,
            requiredReviewsCount = 3,
            hasReviewsInfo = true
        ),
        SubmissionItemUiState(
            submission = Submission(
                id = "sub-3",
                enrollmentId = "enr-3",
                activityId = "act-3",
                activityTitle = "App de Clima",
                evidenceType = EvidenceType.URL,
                evidenceContent = "https://github.com/usuario/app-clima",
                status = SubmissionStatus.REJECTED,
                submittedAt = "2026-08-10T14:30:00Z"
            ),
            courseTitle = "Integración de APIs",
            positiveReviewsCount = 0,
            requiredReviewsCount = 3,
            hasReviewsInfo = true
        ),
        SubmissionItemUiState(
            submission = Submission(
                id = "sub-4",
                enrollmentId = "enr-4",
                activityId = "act-4",
                activityTitle = "Seguridad en Redes",
                evidenceType = EvidenceType.URL,
                evidenceContent = "https://github.com/usuario/seguridad-redes",
                status = SubmissionStatus.ESCALATED,
                submittedAt = "2026-08-08T14:30:00Z"
            ),
            courseTitle = "Ciberseguridad Pro",
            positiveReviewsCount = 0,
            requiredReviewsCount = 3,
            hasReviewsInfo = false
        )
    )

    MentorlyTheme {
        SubmissionListContent(
            state = SubmissionListUiState(
                isLoading = false,
                submissions = sampleItems,
                filteredSubmissions = sampleItems
            ),
            onBackClick = {},
            onSubmissionClick = {},
            onSearchQueryChanged = {},
            onClearSearch = {},
            onRetry = {}
        )
    }
}

@Preview(name = "Lista de Entregas - Vacía", showBackground = true)
@Composable
private fun SubmissionListEmptyPreview() {
    MentorlyTheme {
        SubmissionListContent(
            state = SubmissionListUiState(
                isLoading = false,
                submissions = emptyList(),
                filteredSubmissions = emptyList()
            ),
            onBackClick = {},
            onSubmissionClick = {},
            onSearchQueryChanged = {},
            onClearSearch = {},
            onRetry = {}
        )
    }
}
