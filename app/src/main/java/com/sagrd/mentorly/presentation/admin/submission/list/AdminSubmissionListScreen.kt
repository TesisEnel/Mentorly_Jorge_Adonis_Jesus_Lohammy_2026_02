package com.sagrd.mentorly.presentation.admin.submission.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sagrd.mentorly.domain.model.submission.AdminEscalatedSubmission
import com.sagrd.mentorly.ui.theme.MentorlyTheme

@Composable
fun AdminSubmissionListScreen(
    onBackClick: () -> Unit,
    onSubmissionClick: (String) -> Unit,
    viewModel: AdminSubmissionListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val filteredSubmissions by viewModel.filteredSubmissions.collectAsStateWithLifecycle()

    AdminSubmissionListContent(
        uiState = uiState,
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
    uiState: AdminSubmissionListUiState,
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
                title = { Text("Entregas escaladas") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
                actions = {
                    IconButton(onClick = onRefresh) {
                        Icon(imageVector = Icons.Default.FilterList, contentDescription = "Recargar")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = onSearchChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Buscar por autor, curso o actividad...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true
            )

            ScrollableTabRow(
                selectedTabIndex = uiState.selectedFilter.ordinal,
                edgePadding = 16.dp,
                containerColor = Color.Transparent,
                divider = {}
            ) {
                EscalatedSubmissionFilter.entries.forEach { filter ->
                    Tab(
                        selected = uiState.selectedFilter == filter,
                        onClick = { onFilterChanged(filter) },
                        text = {
                            Text(
                                text = when (filter) {
                                    EscalatedSubmissionFilter.All -> "Todas"
                                    EscalatedSubmissionFilter.MostApproved -> "+ Aprobadas"
                                    EscalatedSubmissionFilter.MostRejected -> "+ Rechazadas"
                                }
                            )
                        }
                    )
                }
            }

            if (uiState.isLoading && !uiState.isRefreshing) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.errorMessage != null) {
                ErrorContent(
                    message = uiState.errorMessage,
                    onRetry = onRefresh,
                    onDismiss = onClearError
                )
            } else if (submissions.isEmpty()) {
                EmptyContent()
            } else {
                SubmissionList(
                    submissions = submissions,
                    onSubmissionClick = onSubmissionClick
                )
            }
        }
    }
}

@Composable
private fun SubmissionList(
    submissions: List<AdminEscalatedSubmission>,
    onSubmissionClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(submissions, key = { it.submissionId }) { submission ->
            EscalatedSubmissionItem(
                submission = submission,
                onClick = { onSubmissionClick(submission.submissionId) }
            )
        }
    }
}

@Composable
private fun EscalatedSubmissionItem(
    submission: AdminEscalatedSubmission,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = submission.courseTitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = submission.activityTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Autor: ${submission.authorDisplayName}",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ReviewStat(count = submission.positiveReviews, isPositive = true)
                    Spacer(modifier = Modifier.width(8.dp))
                    ReviewStat(count = submission.rejectedReviews, isPositive = false)
                }
                Text(
                    text = "Escalada: ${submission.escalatedAtUtc.take(10)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
private fun ReviewStat(count: Int, isPositive: Boolean) {
    Surface(
        color = if (isPositive) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
        contentColor = if (isPositive) Color(0xFF2E7D32) else Color(0xFFC62828),
        shape = CircleShape
    ) {
        Text(
            text = "${if (isPositive) "+" else "-"} $count",
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
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = message, color = MaterialTheme.colorScheme.error)
        Row(modifier = Modifier.padding(top = 16.dp)) {
            Button(onClick = onRetry) { Text("Reintentar") }
            Spacer(modifier = Modifier.width(8.dp))
            TextButton(onClick = onDismiss) { Text("Cerrar") }
        }
    }
}

@Composable
private fun EmptyContent() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = "No hay entregas escaladas pendientes de decisión.",
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(24.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AdminSubmissionListScreenPreview() {
    MentorlyTheme {
        AdminSubmissionListContent(
            uiState = AdminSubmissionListUiState(),
            submissions = listOf(
                AdminEscalatedSubmission(
                    submissionId = "1",
                    enrollmentId = "e1",
                    authorStudentId = "s1",
                    authorDisplayName = "Juan Perez",
                    courseId = "c1",
                    courseTitle = "Android con Compose",
                    activityId = "a1",
                    activityTitle = "Laboratorio 1",
                    evidenceUrl = "url",
                    submittedAtUtc = "2026-08-14",
                    escalatedAtUtc = "2026-08-14",
                    positiveReviews = 2,
                    rejectedReviews = 1
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
