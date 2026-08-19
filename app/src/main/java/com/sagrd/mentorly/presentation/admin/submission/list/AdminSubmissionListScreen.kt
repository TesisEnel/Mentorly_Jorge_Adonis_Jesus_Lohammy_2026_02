package com.sagrd.mentorly.presentation.admin.submission.list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShortText
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sagrd.mentorly.domain.model.submission.AdminEscalatedSubmission
import com.sagrd.mentorly.domain.model.submission.EvidenceType
import com.sagrd.mentorly.ui.theme.MentorlyTheme
import com.sagrd.mentorly.util.DateFormatter

@Composable
fun AdminSubmissionListScreen(
    onBackClick: () -> Unit,
    onSubmissionClick: (String) -> Unit,
    viewModel: AdminSubmissionListViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val filteredSubmissions by viewModel.filteredSubmissions.collectAsStateWithLifecycle()

    LifecycleResumeEffect(Unit) {
        viewModel.onEvent(AdminSubmissionListUiEvent.Refresh)
        onPauseOrDispose { }
    }

    AdminSubmissionListContent(
        state = state,
        submissions = filteredSubmissions,
        onBackClick = onBackClick,
        onSubmissionClick = onSubmissionClick,
        onSearchChanged = { viewModel.onEvent(AdminSubmissionListUiEvent.SearchChanged(it)) },
        onFilterChanged = { viewModel.onEvent(AdminSubmissionListUiEvent.FilterChanged(it)) },
        onRefresh = { viewModel.onEvent(AdminSubmissionListUiEvent.Refresh) },
        onClearError = { viewModel.onEvent(AdminSubmissionListUiEvent.ClearError) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminSubmissionListContent(
    state: AdminSubmissionListUiState,
    submissions: List<AdminEscalatedSubmission>,
    onBackClick: () -> Unit,
    onSubmissionClick: (String) -> Unit,
    onSearchChanged: (String) -> Unit,
    onFilterChanged: (EscalatedSubmissionFilter) -> Unit,
    onRefresh: () -> Unit,
    onClearError: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Entregas escaladas",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
                actions = {
                    IconButton(onClick = onRefresh) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Recargar")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Search Bar
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = onSearchChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Buscar por autor, curso o actividad...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = RoundedCornerShape(24.dp),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )

            // Filters
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                EscalatedSubmissionFilter.entries.forEach { filter ->
                    val isSelected = state.selectedFilter == filter
                    FilterChip(
                        selected = isSelected,
                        onClick = { onFilterChanged(filter) },
                        label = {
                            Text(
                                text = when (filter) {
                                    EscalatedSubmissionFilter.All -> "Todas"
                                    EscalatedSubmissionFilter.MostApproved -> "Más aprobadas"
                                    EscalatedSubmissionFilter.MostRejected -> "Más rechazadas"
                                }
                            )
                        },
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }

            if (state.isLoading && !state.isRefreshing && state.submissions.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (state.errorMessage != null && state.submissions.isEmpty()) {
                ErrorView(message = state.errorMessage, onRetry = onRefresh)
            } else if (submissions.isEmpty() && !state.isLoading) {
                EmptyView(message = "No hay entregas escaladas pendientes de decisión.")
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(submissions, key = { it.submissionId }) { submission ->
                        EscalatedSubmissionCard(
                            submission = submission,
                            onAuditClick = { onSubmissionClick(submission.submissionId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EscalatedSubmissionCard(
    submission: AdminEscalatedSubmission,
    onAuditClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Surface(
                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f),
                shape = RoundedCornerShape(4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Assignment,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "ENTREGA ESCALADA",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = submission.courseTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = submission.activityTitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Author Row
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    val initials = submission.authorDisplayName.split(" ")
                        .filter { it.isNotEmpty() }
                        .take(2)
                        .map { it[0] }
                        .joinToString("")
                        .uppercase()
                    Text(
                        text = initials,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = submission.authorDisplayName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val icon = if (submission.evidenceType == EvidenceType.URL) Icons.Default.Link else Icons.Default.ShortText
                    val label = if (submission.evidenceType == EvidenceType.URL) "Enlace" else "Texto"
                    Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Evidence Box
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = submission.evidenceContent,
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Dates
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                DateInfoRow(label = "Fecha de envío:", date = DateFormatter.format(submission.submittedAtUtc), color = MaterialTheme.colorScheme.onSurfaceVariant)
                DateInfoRow(label = "Fecha de escalamiento:", date = DateFormatter.format(submission.escalatedAtUtc), color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Stats and Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    StatSummary(count = submission.positiveReviews, label = "Aprobaciones", color = Color(0xFF2E7D32))
                    StatSummary(count = submission.rejectedReviews, label = "Rechazos", color = Color(0xFFC62828))
                }

                Button(
                    onClick = onAuditClick,
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp)
                ) {
                    Text("Auditar entrega")
                }
            }
        }
    }
}

@Composable
private fun DateInfoRow(label: String, date: String, color: Color) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = date, style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun StatSummary(count: Int, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = count.toString(), style = MaterialTheme.typography.titleMedium, color = color, fontWeight = FontWeight.Bold)
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ErrorView(message: String, onRetry: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = message, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRetry) { Text("Reintentar") }
        }
    }
}

@Composable
private fun EmptyView(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = message, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center, modifier = Modifier.padding(24.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun AdminSubmissionListScreenPreview() {
    MentorlyTheme {
        AdminSubmissionListContent(
            state = AdminSubmissionListUiState(),
            submissions = listOf(
                AdminEscalatedSubmission(
                    submissionId = "1",
                    enrollmentId = "e1",
                    authorStudentId = "s1",
                    authorDisplayName = "Elena Ramirez",
                    courseId = "c1",
                    courseTitle = "Diseño de Interfaces M3",
                    activityId = "a1",
                    activityTitle = "Práctica 3: Implementación de Tonalidades",
                    evidenceType = EvidenceType.URL,
                    evidenceContent = "figma.com/file/xyz123/M3-Practice...",
                    submittedAtUtc = "2026-10-12T14:30:00Z",
                    escalatedAtUtc = "2026-10-15T09:15:00Z",
                    positiveReviews = 2,
                    rejectedReviews = 3
                ),
                AdminEscalatedSubmission(
                    submissionId = "2",
                    enrollmentId = "e2",
                    authorStudentId = "s2",
                    authorDisplayName = "Carlos Gómez",
                    courseId = "c2",
                    courseTitle = "Fundamentos de Backend",
                    activityId = "a2",
                    activityTitle = "Proyecto Final: API RESTful",
                    evidenceType = EvidenceType.TEXT,
                    evidenceContent = "Adjunto el repositorio de GitHub con la estructura MVC requerida...",
                    submittedAtUtc = "2026-10-10T18:45:00Z",
                    escalatedAtUtc = "2026-10-14T11:30:00Z",
                    positiveReviews = 1,
                    rejectedReviews = 2
                )
            ),
            onBackClick = {},
            onSubmissionClick = {},
            onSearchChanged = {},
            onFilterChanged = {},
            onRefresh = {},
            onClearError = {}
        )
    }
}
