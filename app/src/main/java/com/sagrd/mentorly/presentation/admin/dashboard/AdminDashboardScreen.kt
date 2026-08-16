package com.sagrd.mentorly.presentation.admin.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
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
                title = {
                    Text(
                        "Admin Control Panel",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Menu, contentDescription = "Menú")
                    }
                },
                actions = {
                    IconButton(
                        onClick = onRefresh,
                        enabled = uiState.hasAdminAccess && !uiState.isRefreshing
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Actualizar información")
                    }
                    IconButton(onClick = onShowSignOutDialog) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar sesión")
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
        DashboardMetric("TOTAL ESTUDIANTES", overview.activeEnrollments.toString(), Icons.Default.Groups),
        DashboardMetric("CURSOS ACTIVOS", overview.courses.toString(), Icons.Default.Book),
        DashboardMetric("INSCRIPCIONES ACTIVAS", overview.activeEnrollments.toString(), Icons.Default.School),
        DashboardMetric("CURSOS COMPLETADOS", overview.completedEnrollments.toString(), Icons.Default.Assignment),
        DashboardMetric("INSCRIPCIONES EXPIRADAS", overview.expiredEnrollments.toString(), Icons.Default.Close, true),
        DashboardMetric("REVISIONES PENDIENTES", overview.pendingPeerReviewSubmissions.toString(), Icons.Default.Groups),
    )
    val actions = listOf(
        DashboardAction("Cursos", Icons.Default.School, onCoursesClick),
        DashboardAction("Estudiantes", Icons.Default.Groups, onStudentsClick),
        DashboardAction("Revisiones por pares", Icons.Default.Assignment, onPeerReviewsClick),
        DashboardAction("Auditorías", Icons.Default.Assignment, onEscalatedSubmissionsClick),
        DashboardAction("Analíticas", Icons.Default.Analytics, onAnalyticsClick),
    )

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            AdminStatusBanner(adminName)
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Métricas del sistema", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                TextButton(onClick = onRefresh, enabled = !isRefreshing) {
                    Text("Actualizar información")
                }
            }
        }

        item {
            MetricGrid(metrics)
        }

        item {
            Text("Gestión administrativa", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        item {
            ManagementGrid(actions)
        }
    }
}

@Composable
private fun MetricGrid(metrics: List<DashboardMetric>) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        metrics.chunked(2).forEach { rowMetrics ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                rowMetrics.forEach { metric -> MetricCard(metric, Modifier.weight(1f)) }
                if (rowMetrics.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun AdminStatusBanner(adminName: String) {
    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF6EF08A))) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Default.School, contentDescription = null, tint = Color(0xFF08752F))
            Spacer(Modifier.width(8.dp))
            Text(
                text = if (adminName.isBlank()) "Modo administrador activo" else "Modo administrador activo · $adminName",
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFF08752F),
            )
        }
    }
}

@Composable
private fun MetricCard(metric: DashboardMetric, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = if (metric.isAlert) Color(0xFFFFF1F2) else MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Icon(metric.icon, contentDescription = null, tint = if (metric.isAlert) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
            Text(metric.label, style = MaterialTheme.typography.labelSmall)
            Text(
                metric.value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = if (metric.isAlert) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun ManagementGrid(actions: List<DashboardAction>) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        actions.chunked(2).forEach { rowActions ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                rowActions.forEach { action ->
                    Card(
                        modifier = Modifier.weight(1f),
                        onClick = action.onClick,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 18.dp, horizontal = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(action.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text(action.label, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
                if (rowActions.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

private data class DashboardMetric(
    val label: String,
    val value: String,
    val icon: ImageVector,
    val isAlert: Boolean = false,
)

private data class DashboardAction(
    val label: String,
    val icon: ImageVector,
    val onClick: () -> Unit,
)

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
