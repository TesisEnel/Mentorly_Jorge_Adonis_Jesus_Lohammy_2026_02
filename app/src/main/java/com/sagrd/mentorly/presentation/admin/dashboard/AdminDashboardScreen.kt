package com.sagrd.mentorly.presentation.admin.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.sagrd.mentorly.domain.model.analytics.AnalyticsOverview
import com.sagrd.mentorly.ui.theme.MentorlyTheme

@Composable
fun AdminDashboardScreen(
    onCoursesClick: () -> Unit,
    onStudentsClick: () -> Unit,
    onPeerReviewsClick: () -> Unit,
    onEscalatedSubmissionsClick: () -> Unit,
    onAnalyticsClick: () -> Unit,
    onSignOutCompleted: () -> Unit,
    viewModel: AdminDashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isSignedOut) {
        if (uiState.isSignedOut) {
            viewModel.onEvent(AdminDashboardUiEvent.SignOutHandled)
            onSignOutCompleted()
        }
    }

    AdminDashboardContent(
        uiState = uiState,
        onRefresh = { viewModel.onEvent(AdminDashboardUiEvent.Refresh) },
        onClearError = { viewModel.onEvent(AdminDashboardUiEvent.ClearError) },
        onCoursesClick = onCoursesClick,
        onStudentsClick = onStudentsClick,
        onPeerReviewsClick = onPeerReviewsClick,
        onEscalatedSubmissionsClick = onEscalatedSubmissionsClick,
        onAnalyticsClick = onAnalyticsClick,
        onShowSignOutDialog = {
            viewModel.onEvent(AdminDashboardUiEvent.ShowSignOutDialog)
        },
        onDismissSignOutDialog = {
            viewModel.onEvent(AdminDashboardUiEvent.DismissSignOutDialog)
        },
        onConfirmSignOut = {
            viewModel.onEvent(AdminDashboardUiEvent.ConfirmSignOut)
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminDashboardContent(
    uiState: AdminDashboardUiState,
    onRefresh: () -> Unit,
    onClearError: () -> Unit,
    onCoursesClick: () -> Unit,
    onStudentsClick: () -> Unit,
    onPeerReviewsClick: () -> Unit,
    onEscalatedSubmissionsClick: () -> Unit,
    onAnalyticsClick: () -> Unit,
    onShowSignOutDialog: () -> Unit,
    onDismissSignOutDialog: () -> Unit,
    onConfirmSignOut: () -> Unit,
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Panel administrativo", fontWeight = FontWeight.Bold) },
                actions = {
                    TextButton(
                        onClick = onRefresh,
                        enabled = uiState.hasAdminAccess && !uiState.isRefreshing
                    ) {
                        Text("Recargar")
                    }
                    TextButton(onClick = onShowSignOutDialog) {
                        Text("Cerrar sesión")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors()
            )
        }
    ) { innerPadding ->
        when {
            !uiState.hasSession -> MessageContent(
                message = "No se encontró una sesión activa.",
                modifier = Modifier.fillMaxSize().padding(innerPadding)
            )

            !uiState.hasAdminAccess -> MessageContent(
                message = "No tienes permisos para acceder al panel administrativo.",
                modifier = Modifier.fillMaxSize().padding(innerPadding)
            )

            uiState.isLoading && uiState.overview == null -> LoadingContent(
                modifier = Modifier.fillMaxSize().padding(innerPadding)
            )

            uiState.errorMessage != null && uiState.overview == null -> ErrorContent(
                message = uiState.errorMessage,
                onRetry = onRefresh,
                onDismiss = onClearError,
                modifier = Modifier.fillMaxSize().padding(innerPadding)
            )

            uiState.overview == null -> MessageContent(
                message = "No hay información administrativa disponible.",
                modifier = Modifier.fillMaxSize().padding(innerPadding)
            )

            else -> DashboardContent(
                adminName = uiState.adminName,
                overview = uiState.overview,
                isRefreshing = uiState.isRefreshing,
                errorMessage = uiState.errorMessage,
                onRefresh = onRefresh,
                onClearError = onClearError,
                onCoursesClick = onCoursesClick,
                onStudentsClick = onStudentsClick,
                onPeerReviewsClick = onPeerReviewsClick,
                onEscalatedSubmissionsClick = onEscalatedSubmissionsClick,
                onAnalyticsClick = onAnalyticsClick,
                modifier = Modifier.fillMaxSize().padding(innerPadding)
            )
        }
    }

    if (uiState.isSignOutDialogVisible) {
        AlertDialog(
            onDismissRequest = onDismissSignOutDialog,
            title = { Text("Cerrar sesión") },
            text = { Text("¿Seguro que deseas cerrar sesión?") },
            confirmButton = {
                TextButton(onClick = onConfirmSignOut) {
                    Text("Cerrar sesión")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissSignOutDialog) {
                    Text("Cancelar")
                }
            },
        )
    }
}

@Composable
private fun DashboardContent(
    adminName: String,
    overview: AnalyticsOverview,
    isRefreshing: Boolean,
    errorMessage: String?,
    onRefresh: () -> Unit,
    onClearError: () -> Unit,
    onCoursesClick: () -> Unit,
    onStudentsClick: () -> Unit,
    onPeerReviewsClick: () -> Unit,
    onEscalatedSubmissionsClick: () -> Unit,
    onAnalyticsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val metrics = listOf(
        "Cursos" to overview.courses.toString(),
        "Inscripciones activas" to overview.activeEnrollments.toString(),
        "Cursos completados" to overview.completedEnrollments.toString(),
        "Inscripciones expiradas" to overview.expiredEnrollments.toString(),
        "Revisiones pendientes" to overview.pendingPeerReviewSubmissions.toString()
    )
    val actions = listOf(
        "Cursos" to onCoursesClick,
        "Estudiantes" to onStudentsClick,
        "Revisiones" to onPeerReviewsClick,
        "Entregas escaladas" to onEscalatedSubmissionsClick,
        "Analíticas" to onAnalyticsClick
    )

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = if (adminName.isBlank()) "Bienvenido" else "Bienvenido, $adminName",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Resumen general de Mentorly",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        if (isRefreshing) {
            item {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }

        errorMessage?.let { message ->
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Column(Modifier.padding(16.dp)) {
                        Text(message, color = MaterialTheme.colorScheme.onErrorContainer)
                        TextButton(onClick = onClearError) { Text("Cerrar") }
                    }
                }
            }
        }

        item {
            Text("Resumen", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        }

        item {
            MetricGrid(metrics)
        }

        item {
            Text("Gestión", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        }

        items(actions, key = { it.first }) { action ->
            OutlinedButton(
                onClick = action.second,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(action.first)
            }
        }

        item {
            Button(
                onClick = onRefresh,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isRefreshing
            ) {
                Text("Actualizar información")
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MetricGrid(metrics: List<Pair<String, String>>) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        metrics.forEach { metric ->
            Card(
                modifier = Modifier.width(160.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(metric.second, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text(metric.first, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
private fun LoadingContent(modifier: Modifier = Modifier) {
    Box(modifier, contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun MessageContent(message: String, modifier: Modifier = Modifier) {
    Box(modifier.padding(24.dp), contentAlignment = Alignment.Center) {
        Text(message, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier.padding(24.dp), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(message, color = MaterialTheme.colorScheme.error)
            Button(onClick = onRetry) { Text("Reintentar") }
            TextButton(onClick = onDismiss) { Text("Cerrar") }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AdminDashboardPreview() {
    MentorlyTheme {
        AdminDashboardContent(
            uiState = AdminDashboardUiState(
                adminName = "Alex",
                overview = AnalyticsOverview(
                    courses = 8,
                    activeEnrollments = 42,
                    completedEnrollments = 18,
                    expiredEnrollments = 5,
                    pendingPeerReviewSubmissions = 7
                )
            ),
            onRefresh = {},
            onClearError = {},
            onCoursesClick = {},
            onStudentsClick = {},
            onPeerReviewsClick = {},
            onEscalatedSubmissionsClick = {},
            onAnalyticsClick = {},
            onShowSignOutDialog = {},
            onDismissSignOutDialog = {},
            onConfirmSignOut = {},
        )
    }
}
