package com.sagrd.mentorly.presentation.progress

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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.sagrd.mentorly.domain.model.content.ActivityType
import com.sagrd.mentorly.domain.model.progress.EnrollmentActivityProgress
import com.sagrd.mentorly.domain.model.progress.EnrollmentProgress
import com.sagrd.mentorly.domain.model.progress.EnrollmentThemeProgress
import com.sagrd.mentorly.domain.model.progress.EnrollmentUnitProgress
import com.sagrd.mentorly.ui.theme.MentorlyTheme

@Composable
fun EnrollmentProgressScreen(
    enrollmentId: String,
    onBackClick: () -> Unit,
    onActivityClick: (String) -> Unit,
    onQuizClick: (String) -> Unit = {},
    viewModel: EnrollmentProgressViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(enrollmentId) {
        viewModel.initialize(enrollmentId)
    }

    EnrollmentProgressContent(
        uiState = uiState,
        onBackClick = onBackClick,
        onActivityClick = onActivityClick,
        onQuizClick = onQuizClick,
        onCompleteTheme = { themeId ->
            viewModel.onEvent(EnrollmentProgressUiEvent.CompleteTheme(themeId))
        },
        onRetry = { viewModel.onEvent(EnrollmentProgressUiEvent.Refresh) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EnrollmentProgressContent(
    uiState: EnrollmentProgressUiState,
    onBackClick: () -> Unit,
    onActivityClick: (String) -> Unit,
    onQuizClick: (String) -> Unit,
    onCompleteTheme: (String) -> Unit,
    onRetry: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Progreso", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        when {
            uiState.isLoading && uiState.progress == null -> LoadingContent(
                modifier = Modifier.fillMaxSize().padding(innerPadding)
            )

            uiState.errorMessage != null && uiState.progress == null -> ErrorContent(
                message = uiState.errorMessage,
                onRetry = onRetry,
                modifier = Modifier.fillMaxSize().padding(innerPadding)
            )

            uiState.progress != null -> ProgressContent(
                progress = uiState.progress,
                isRefreshing = uiState.isRefreshing,
                completingThemeIds = uiState.completingThemeIds,
                errorMessage = uiState.errorMessage,
                onActivityClick = onActivityClick,
                onQuizClick = onQuizClick,
                onCompleteTheme = onCompleteTheme,
                onRetry = onRetry,
                modifier = Modifier.fillMaxSize().padding(innerPadding)
            )
        }
    }
}

@Composable
private fun ProgressContent(
    progress: EnrollmentProgress,
    isRefreshing: Boolean,
    completingThemeIds: Set<String>,
    errorMessage: String?,
    onActivityClick: (String) -> Unit,
    onQuizClick: (String) -> Unit,
    onCompleteTheme: (String) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { OverallProgressCard(progress, isRefreshing, onRetry) }

        progress.blockedReason?.let { reason -> item { BlockedReasonCard(reason) } }

        errorMessage?.let { message -> item { ErrorBanner(message, onRetry) } }

        item {
            Text(
                text = "Progreso por unidad",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        itemsIndexed(progress.units, key = { _, unit -> unit.unitId }) { index, unit ->
            UnitProgressCard(
                number = index + 1,
                unit = unit,
                completingThemeIds = completingThemeIds,
                onActivityClick = onActivityClick,
                onQuizClick = onQuizClick,
                onCompleteTheme = onCompleteTheme
            )
        }
    }
}

@Composable
private fun OverallProgressCard(
    progress: EnrollmentProgress,
    isRefreshing: Boolean,
    onRefresh: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Avance general", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text("${progress.percentage}%", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
            LinearProgressIndicator(
                progress = { progress.percentage.coerceIn(0, 100) / 100f },
                modifier = Modifier.fillMaxWidth()
            )
            Text("Temas completados: ${progress.completedThemes} de ${progress.totalThemes}")
            Text("Actividades obligatorias aprobadas: ${progress.approvedMandatoryActivities} de ${progress.totalMandatoryActivities}")
            Button(onClick = onRefresh, enabled = !isRefreshing, modifier = Modifier.fillMaxWidth()) {
                Text(if (isRefreshing) "Actualizando..." else "Actualizar progreso")
            }
        }
    }
}

@Composable
private fun UnitProgressCard(
    number: Int,
    unit: EnrollmentUnitProgress,
    completingThemeIds: Set<String>,
    onActivityClick: (String) -> Unit,
    onQuizClick: (String) -> Unit,
    onCompleteTheme: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                "Unidad $number: ${unit.title}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text("Temas: ${unit.completedThemes} de ${unit.totalThemes}")
            Text("Actividades obligatorias: ${unit.approvedMandatoryActivities} de ${unit.totalMandatoryActivities}")

            unit.themes.forEachIndexed { index, theme ->
                HorizontalDivider()
                ThemeProgressCard(
                    number = index + 1,
                    theme = theme,
                    isCompleting = theme.themeId in completingThemeIds,
                    onActivityClick = onActivityClick,
                    onQuizClick = onQuizClick,
                    onCompleteTheme = { onCompleteTheme(theme.themeId) }
                )
            }
        }
    }
}

@Composable
private fun ThemeProgressCard(
    number: Int,
    theme: EnrollmentThemeProgress,
    isCompleting: Boolean,
    onActivityClick: (String) -> Unit,
    onQuizClick: (String) -> Unit,
    onCompleteTheme: () -> Unit
) {
    var isContentVisible by remember(theme.themeId) { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Tema $number: ${theme.title}",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = if (theme.isCompleted) "Completado" else "Pendiente",
            color = if (theme.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall
        )
        OutlinedButton(
            onClick = { isContentVisible = !isContentVisible },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isContentVisible) "Ocultar contenido" else "Leer contenido")
        }

        if (isContentVisible) {
            Text(
                text = theme.contentText.ifBlank { "Este tema no tiene contenido disponible." },
                style = MaterialTheme.typography.bodyMedium
            )

            Button(
                onClick = onCompleteTheme,
                enabled = !theme.isCompleted && !isCompleting,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isCompleting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Completando...")
                } else {
                    Text(if (theme.isCompleted) "Completado" else "Marcar como completado")
                }
            }

            if (theme.activities.isNotEmpty()) {
                Text("Actividades", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
                theme.activities.forEachIndexed { index, activity ->
                    ActivityProgressRow(
                        number = index + 1,
                        activity = activity,
                        onClick = {
                            when (activity.type) {
                                ActivityType.EXERCISE -> onActivityClick(activity.activityId)
                                ActivityType.QUIZ -> onQuizClick(activity.activityId)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ActivityProgressRow(
    number: Int,
    activity: EnrollmentActivityProgress,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Actividad $number: ${activity.title}", fontWeight = FontWeight.Medium)
            Text(
                text = if (activity.isMandatory) "Obligatoria" else "Opcional",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = if (activity.isApproved) "Aprobada" else "Pendiente",
                style = MaterialTheme.typography.bodySmall,
                color = if (activity.isApproved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(onClick = onClick, modifier = Modifier.fillMaxWidth()) { Text("Ver actividad") }
        }
    }
}

@Composable
private fun LoadingContent(modifier: Modifier) {
    Box(modifier, contentAlignment = Alignment.Center) { CircularProgressIndicator() }
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

@Composable
private fun ErrorBanner(message: String, onRetry: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(message, color = MaterialTheme.colorScheme.error)
            Button(onClick = onRetry) { Text("Reintentar") }
        }
    }
}

@Composable
private fun BlockedReasonCard(reason: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text("Progreso bloqueado", fontWeight = FontWeight.SemiBold)
            Text(text = reason, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun EnrollmentProgressPreview() {
    MentorlyTheme {
        EnrollmentProgressContent(
            uiState = EnrollmentProgressUiState(
                progress = EnrollmentProgress(
                    enrollmentId = "enrollment-1",
                    percentage = 40,
                    completedThemes = 2,
                    totalThemes = 5,
                    approvedMandatoryActivities = 1,
                    totalMandatoryActivities = 3,
                    canSubmitNextUnit = false,
                    blockedReason = "Debes aprobar el ejercicio obligatorio de la unidad anterior.",
                    units = listOf(
                        EnrollmentUnitProgress(
                            unitId = "unit-1",
                            title = "Fundamentos",
                            completedThemes = 2,
                            totalThemes = 2,
                            approvedMandatoryActivities = 1,
                            totalMandatoryActivities = 1,
                            themes = listOf(
                                EnrollmentThemeProgress(
                                    themeId = "theme-1",
                                    title = "Introducción",
                                    contentText = "Lee los conceptos principales antes de comenzar.",
                                    orderIndex = 1,
                                    isCompleted = true,
                                    activities = listOf(
                                        EnrollmentActivityProgress(
                                            activityId = "activity-1",
                                            title = "Ejercicio inicial",
                                            isMandatory = true,
                                            isApproved = true
                                        )
                                    )
                                )
                            )
                        ),
                        EnrollmentUnitProgress(
                            unitId = "unit-2",
                            title = "Proyecto final",
                            completedThemes = 0,
                            totalThemes = 3,
                            approvedMandatoryActivities = 0,
                            totalMandatoryActivities = 2,
                            themes = listOf(
                                EnrollmentThemeProgress(
                                    themeId = "theme-2",
                                    title = "Preparación del proyecto",
                                    contentText = "Revisa los requisitos de la entrega final.",
                                    orderIndex = 1,
                                    isCompleted = false,
                                    activities = listOf(
                                        EnrollmentActivityProgress(
                                            activityId = "activity-2",
                                            title = "Cuestionario final",
                                            isMandatory = true,
                                            isApproved = false,
                                            type = ActivityType.QUIZ
                                        )
                                    )
                                )
                            )
                        )
                    )
                )
            ),
            onBackClick = {},
            onActivityClick = {},
            onQuizClick = {},
            onCompleteTheme = {},
            onRetry = {}
        )
    }
}
