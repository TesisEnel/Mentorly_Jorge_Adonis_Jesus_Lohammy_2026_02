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
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Lock
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
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.isSignedOut) {
        if (state.isSignedOut) {
            viewModel.onEvent(AdminDashboardUiEvent.SignOutHandled)
            onSignOutCompleted()
        }
    }

    AdminDashboardContent(
        state = state,
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
    state: AdminDashboardUiState,
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
                        "Panel administrativo",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    Icon(
                        Icons.Default.AccountCircle,
                        contentDescription = "Avatar de administrador",
                        modifier = Modifier.padding(start = 16.dp),
                    )
                },
                actions = {
                    IconButton(onClick = onShowSignOutDialog) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar sesión")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors()
            )
        }
    ) { innerPadding ->
        when {
            !state.hasSession -> MessageContent(
                message = "No se encontró una sesión activa.",
                modifier = Modifier.fillMaxSize().padding(innerPadding)
            )

            !state.hasAdminAccess -> RestrictedAccessContent(
                modifier = Modifier.fillMaxSize().padding(innerPadding)
            )

            state.isLoading && state.overview == null -> DashboardSkeleton(
                modifier = Modifier.fillMaxSize().padding(innerPadding)
            )

            state.errorMessage != null && state.overview == null -> ErrorContent(
                onRetry = onRefresh,
                modifier = Modifier.fillMaxSize().padding(innerPadding)
            )

            state.overview == null -> MessageContent(
                message = "No hay información administrativa disponible.",
                modifier = Modifier.fillMaxSize().padding(innerPadding)
            )

            else -> DashboardContent(
                adminName = state.adminName,
                overview = state.overview,
                isRefreshing = state.isRefreshing,
                errorMessage = state.errorMessage,
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

    if (state.isSignOutDialogVisible) {
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
        DashboardMetric("Cursos", overview.courses.toString(), Icons.Default.Book),
        DashboardMetric("Inscripciones activas", overview.activeEnrollments.toString(), Icons.Default.School),
        DashboardMetric("Cursos completados", overview.completedEnrollments.toString(), Icons.Default.Assignment),
        DashboardMetric("Inscripciones expiradas", overview.expiredEnrollments.toString(), Icons.Default.Close, true),
        DashboardMetric("Entregas pendientes de revisión", overview.pendingPeerReviewSubmissions.toString(), Icons.Default.Groups),
    )
    val actions = listOf(
        DashboardAction("Cursos", "Crear y administrar contenido académico", Icons.Default.School, onCoursesClick),
        DashboardAction("Estudiantes", "Consultar progreso y gestionar roles", Icons.Default.Groups, onStudentsClick),
        DashboardAction("Revisiones por pares", "Auditar revisiones y resolver casos", Icons.Default.Assignment, onPeerReviewsClick),
        DashboardAction("Entregas escaladas", "Revisar solicitudes pendientes de decisión", Icons.Default.Assignment, onEscalatedSubmissionsClick),
        DashboardAction("Analíticas", "Consultar abandono, tiempos y cuellos de botella", Icons.Default.Analytics, onAnalyticsClick),
    )

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            WelcomeHeader(adminName)
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
                Text("Resumen general", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
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
            Text("Gestión", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
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
                rowMetrics.forEach { metric ->
                    MetricCard(
                        metric,
                        if (rowMetrics.size == 1) Modifier.fillMaxWidth() else Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun WelcomeHeader(adminName: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = "Hola, ${adminName.ifBlank { "Adonis" }}",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "Resumen general de Mentorly",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
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
        actions.forEach { action ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                onClick = action.onClick,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(action.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(action.label, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Text(
                            action.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = "Abrir ${action.label}",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun DashboardSkeleton(modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item { SkeletonBlock(Modifier.fillMaxWidth().height(56.dp)) }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                repeat(3) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        SkeletonBlock(Modifier.weight(1f).height(100.dp))
                        SkeletonBlock(Modifier.weight(1f).height(100.dp))
                    }
                }
            }
        }
        item {
            repeat(5) {
                SkeletonBlock(Modifier.fillMaxWidth().height(84.dp).padding(bottom = 10.dp))
            }
        }
    }
}

@Composable
private fun SkeletonBlock(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {}
}

@Composable
private fun MessageContent(message: String, modifier: Modifier = Modifier) {
    Box(modifier.padding(24.dp), contentAlignment = Alignment.Center) {
        Text(message, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun RestrictedAccessContent(modifier: Modifier = Modifier) {
    Box(modifier.padding(24.dp), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                Icons.Default.Lock,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
            )
            Text(
                "No tienes permisos para acceder al panel administrativo",
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}

@Composable
private fun ErrorContent(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier.padding(24.dp), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "No se pudo cargar el resumen administrativo",
                color = MaterialTheme.colorScheme.error,
            )
            Button(onClick = onRetry) { Text("Reintentar") }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AdminDashboardPreview() {
    MentorlyTheme {
        DashboardContent(
            adminName = "Adonis",
            overview = AnalyticsOverview(
                courses = 8,
                activeEnrollments = 42,
                completedEnrollments = 18,
                expiredEnrollments = 5,
                pendingPeerReviewSubmissions = 7,
            ),
            isRefreshing = false,
            errorMessage = null,
            onRefresh = {},
            onClearError = {},
            onCoursesClick = {},
            onStudentsClick = {},
            onPeerReviewsClick = {},
            onEscalatedSubmissionsClick = {},
            onAnalyticsClick = {},
        )
    }
}
