package com.sagrd.mentorly.presentation.enrollment.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.sagrd.mentorly.domain.model.enrollment.Enrollment
import com.sagrd.mentorly.domain.model.enrollment.EnrollmentStatus
import com.sagrd.mentorly.ui.theme.MentorlyTheme
import com.sagrd.mentorly.util.DateFormatter

@Composable
fun EnrollmentListScreen(
    onEnrollmentClick: (String) -> Unit,
    onSubmissionsClick: () -> Unit,
    viewModel: EnrollmentListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    EnrollmentListContent(
        uiState = uiState,
        onEnrollmentClick = onEnrollmentClick,
        onSubmissionsClick = onSubmissionsClick,
        onRetry = { viewModel.onEvent(EnrollmentListUiEvent.Refresh) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EnrollmentListContent(
    uiState: EnrollmentListUiState,
    onEnrollmentClick: (String) -> Unit,
    onSubmissionsClick: () -> Unit,
    onRetry: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Mi aprendizaje", fontWeight = FontWeight.Bold) },
                actions = {
                    TextButton(onClick = onSubmissionsClick) {
                        Text("Entregas")
                    }
                }
            )
        }
    ) { innerPadding ->
        when {
            uiState.isLoading && uiState.enrollments.isEmpty() -> LoadingContent(
                Modifier.fillMaxSize().padding(innerPadding)
            )

            uiState.errorMessage != null && uiState.enrollments.isEmpty() -> ErrorContent(
                message = uiState.errorMessage,
                onRetry = onRetry,
                modifier = Modifier.fillMaxSize().padding(innerPadding)
            )

            uiState.enrollments.isEmpty() -> EmptyContent(
                Modifier.fillMaxSize().padding(innerPadding)
            )

            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    uiState.errorMessage?.let { message ->
                        Text(
                            text = message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                items(uiState.enrollments, key = { it.id }) { enrollment ->
                    EnrollmentCard(enrollment, onClick = { onEnrollmentClick(enrollment.id) })
                }
            }
        }
    }
}

@Composable
private fun EnrollmentCard(enrollment: Enrollment, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(enrollment.courseTitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Text("Intento ${enrollment.attemptNumber}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            EnrollmentStatusLabel(enrollment.status)
            Spacer(Modifier.height(12.dp))
            Text("Inicio: ${DateFormatter.format(enrollment.startedAt)}", style = MaterialTheme.typography.bodySmall)
            Text("Vence: ${DateFormatter.format(enrollment.expiresAt)}", style = MaterialTheme.typography.bodySmall)
            enrollment.completedAt?.let { Text("Completado: ${DateFormatter.format(it)}", style = MaterialTheme.typography.bodySmall) }
        }
    }
}

@Composable
private fun EnrollmentStatusLabel(status: EnrollmentStatus) {
    val text = when (status) {
        EnrollmentStatus.ACTIVE -> "Activa"
        EnrollmentStatus.COMPLETED -> "Completada"
        EnrollmentStatus.EXPIRED -> "Expirada"
    }
    Text(text, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
}

@Composable
private fun LoadingContent(modifier: Modifier) {
    Box(modifier, contentAlignment = Alignment.Center) { CircularProgressIndicator() }
}

@Composable
private fun EmptyContent(modifier: Modifier) {
    Box(modifier.padding(24.dp), contentAlignment = Alignment.Center) {
        Text("Aún no tienes inscripciones.", style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun ErrorContent(message: String, onRetry: () -> Unit, modifier: Modifier) {
    Box(modifier.padding(24.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(message, color = MaterialTheme.colorScheme.error)
            Button(onClick = onRetry) { Text("Reintentar") }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun EnrollmentListPreview() {
    MentorlyTheme {
        EnrollmentListContent(
            uiState = EnrollmentListUiState(
                enrollments = listOf(
                    Enrollment("enrollment-1", "student-1", "course-1", "Fundamentos de SQL", 1, "2026-08-12", "2026-11-12", null, EnrollmentStatus.ACTIVE),
                    Enrollment("enrollment-2", "student-1", "course-2", "Android con Compose", 2, "2026-04-12", "2026-07-12", "2026-06-20", EnrollmentStatus.COMPLETED)
                )
            ),
            onEnrollmentClick = {},
            onSubmissionsClick = {},
            onRetry = {}
        )
    }
}
