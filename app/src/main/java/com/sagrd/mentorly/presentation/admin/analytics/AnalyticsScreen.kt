package com.sagrd.mentorly.presentation.admin.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sagrd.mentorly.domain.model.analytics.*
import com.sagrd.mentorly.domain.model.course.Course
import com.sagrd.mentorly.ui.theme.MentorlyTheme

@Composable
fun AnalyticsScreen(
    onBackClick: () -> Unit,
    viewModel: AnalyticsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AnalyticsContent(
        uiState = uiState,
        onBackClick = onBackClick,
        onCourseSelected = { viewModel.onEvent(AnalyticsUiEvent.CourseSelected(it)) },
        onRefresh = { viewModel.onEvent(AnalyticsUiEvent.Refresh) },
        onRetryOverview = { viewModel.onEvent(AnalyticsUiEvent.RetryOverview) },
        onRetryCourseAnalytics = { viewModel.onEvent(AnalyticsUiEvent.RetryCourseAnalytics) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AnalyticsContent(
    uiState: AnalyticsUiState,
    onBackClick: () -> Unit,
    onCourseSelected: (String) -> Unit,
    onRefresh: () -> Unit,
    onRetryOverview: () -> Unit,
    onRetryCourseAnalytics: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Analíticas", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
                actions = {
                    IconButton(onClick = onRefresh) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refrescar")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            if (!uiState.hasSession || !uiState.hasAdminAccess) {
                Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                    Text(
                        text = uiState.overviewErrorMessage ?: "Acceso restringido.",
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                }
                return@Scaffold
            }

            SectionHeader(title = "Resumen General", icon = Icons.Default.Assessment)
            
            if (uiState.isLoadingOverview && uiState.overview == null) {
                Box(Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.overviewErrorMessage != null && uiState.overview == null) {
                ErrorCard(message = uiState.overviewErrorMessage, onRetry = onRetryOverview)
            } else if (uiState.overview != null) {
                OverviewGrid(overview = uiState.overview)
            }

            Spacer(modifier = Modifier.height(24.dp))

            SectionHeader(title = "Analíticas por Curso", icon = Icons.Default.School)
            
            CourseSelector(
                courses = uiState.courses,
                selectedCourseId = uiState.selectedCourseId,
                isLoading = uiState.isLoadingCourses,
                onCourseSelected = onCourseSelected
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.selectedCourseId == null) {
                Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    Text("Selecciona un curso para ver analíticas detalladas.")
                }
            } else if (uiState.isLoadingCourseAnalytics && uiState.dropOff.isEmpty() && uiState.completionTime == null) {
                Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                CourseAnalyticsContent(
                    uiState = uiState,
                    onRetry = onRetryCourseAnalytics
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SectionHeader(title: String, icon: ImageVector) {
    Row(
        modifier = Modifier.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun OverviewGrid(overview: AnalyticsOverview) {
    val items = listOf(
        "Cursos" to overview.courses.toString(),
        "Inscripciones" to overview.activeEnrollments.toString(),
        "Completados" to overview.completedEnrollments.toString(),
        "Expirados" to overview.expiredEnrollments.toString(),
        "Pendientes" to overview.pendingPeerReviewSubmissions.toString()
    )

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            MetricCard(title = items[0].first, value = items[0].second, modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.width(12.dp))
            MetricCard(title = items[1].first, value = items[1].second, modifier = Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            MetricCard(title = items[2].first, value = items[2].second, modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.width(12.dp))
            MetricCard(title = items[3].first, value = items[3].second, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun MetricCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(text = value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
            Text(text = title, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CourseSelector(
    courses: List<Course>,
    selectedCourseId: String?,
    isLoading: Boolean,
    onCourseSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedCourse = courses.find { it.id == selectedCourseId }

    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { if (!isLoading) expanded = it }
        ) {
            OutlinedTextField(
                value = selectedCourse?.title ?: "Seleccionar curso...",
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor(),
                shape = RoundedCornerShape(12.dp),
                enabled = !isLoading
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                courses.forEach { course ->
                    DropdownMenuItem(
                        text = { Text(course.title) },
                        onClick = {
                            onCourseSelected(course.id)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun CourseAnalyticsContent(
    uiState: AnalyticsUiState,
    onRetry: () -> Unit
) {
    if (uiState.courseAnalyticsErrorMessage != null && uiState.dropOff.isEmpty()) {
        ErrorCard(message = uiState.courseAnalyticsErrorMessage, onRetry = onRetry)
        return
    }

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        AnalyticsSubSection(title = "Abandono por Tema (Drop-off)") {
            if (uiState.dropOff.isEmpty()) {
                Text("No hay datos de abandono disponibles.", style = MaterialTheme.typography.bodyMedium)
            } else {
                uiState.dropOff.forEach { item ->
                    DropOffBar(item = item)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        AnalyticsSubSection(title = "Tiempos de Finalización") {
            uiState.completionTime?.let { report ->
                CompletionTimeSummary(report = report)
            } ?: Text("Cargando tiempos...", style = MaterialTheme.typography.bodySmall)
        }

        AnalyticsSubSection(title = "Cuellos de Botella (Peer Review)") {
            if (uiState.peerReviewBottlenecks.isEmpty()) {
                Text("No se detectaron cuellos de botella.", style = MaterialTheme.typography.bodyMedium)
            } else {
                uiState.peerReviewBottlenecks.forEach { bottleneck ->
                    BottleneckItem(bottleneck = bottleneck)
                }
            }
        }

        AnalyticsSubSection(title = "Historial Reciente de Inscripciones") {
            if (uiState.enrollmentHistory.isEmpty()) {
                Text("No hay historial disponible.", style = MaterialTheme.typography.bodyMedium)
            } else {
                uiState.enrollmentHistory.take(5).forEach { history ->
                    HistoryItem(history = history)
                }
            }
        }
    }
}

@Composable
private fun AnalyticsSubSection(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun DropOffBar(item: DropOff) {
    Column {
        Text(text = item.themeTitle, style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .weight(0.85f)
                    .height(8.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(item.completionRate.toFloat() / 100f)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp))
                )
            }
            Text(
                text = "${item.completionRate.toInt()}%",
                modifier = Modifier.weight(0.15f),
                textAlign = TextAlign.End,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
private fun CompletionTimeSummary(report: CompletionTimeReport) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
        TimeStat(label = "Promedio Curso", value = report.courseAverageDays?.let { "${it.toInt()}d" } ?: "N/A")
    }
}

@Composable
private fun TimeStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(text = label, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun BottleneckItem(bottleneck: PeerReviewBottleneck) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp).fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = Color(0xFFFBC02D), modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = bottleneck.activityTitle, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
        Text(text = "${bottleneck.pendingSubmissions} pen.", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun HistoryItem(history: EnrollmentHistory) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp).fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = "ID: ${history.studentId.take(8)}", style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
        Text(text = "Estado: ${history.status}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
    }
}

@Composable
private fun ErrorCard(message: String, onRetry: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = message, color = MaterialTheme.colorScheme.onErrorContainer)
            TextButton(onClick = onRetry) { Text("Reintentar") }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AnalyticsScreenPreview() {
    MentorlyTheme {
        AnalyticsContent(
            uiState = AnalyticsUiState(
                overview = AnalyticsOverview(5, 120, 80, 10, 5),
                courses = listOf(Course("1", "Android", "Desc", null, true, 2))
            ),
            onBackClick = {},
            onCourseSelected = {},
            onRefresh = {},
            onRetryOverview = {},
            onRetryCourseAnalytics = {}
        )
    }
}
