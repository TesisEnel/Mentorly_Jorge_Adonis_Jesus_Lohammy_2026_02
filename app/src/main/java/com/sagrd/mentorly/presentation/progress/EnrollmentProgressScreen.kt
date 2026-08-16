package com.sagrd.mentorly.presentation.progress

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.material.icons.outlined.WorkspacePremium
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.sagrd.mentorly.domain.model.content.ActivityType
import com.sagrd.mentorly.domain.model.enrollment.Enrollment
import com.sagrd.mentorly.domain.model.enrollment.EnrollmentStatus
import com.sagrd.mentorly.domain.model.progress.EnrollmentActivityProgress
import com.sagrd.mentorly.domain.model.progress.EnrollmentProgress
import com.sagrd.mentorly.domain.model.progress.EnrollmentThemeProgress
import com.sagrd.mentorly.domain.model.progress.EnrollmentUnitProgress
import com.sagrd.mentorly.ui.theme.MentorlyTheme

private val PrimaryBlue = Color(0xFF1565C0)
private val CompletedGreen = Color(0xFF2E7D32)
private val CompletedGreenBg = Color(0xFFE8F5E9)
private val TimeRemainingBg = Color(0xFFFFEBEE)
private val TimeRemainingText = Color(0xFFC62828)
private val LockedBg = Color(0xFFF1F5F9)
private val ActiveBlueBg = Color(0xFFEFF6FF)

@Composable
fun EnrollmentProgressScreen(
    enrollmentId: String,
    onBackClick: () -> Unit,
    onActivityClick: (String) -> Unit,
    onQuizClick: (String) -> Unit = {},
    onThemeClick: (String) -> Unit = {},
    viewModel: EnrollmentProgressViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(enrollmentId) {
        viewModel.initialize(enrollmentId)
    }

    EnrollmentProgressContent(
        uiState = uiState,
        onBackClick = onBackClick,
        onActivityClick = onActivityClick,
        onQuizClick = onQuizClick,
        onThemeClick = onThemeClick,
        onToggleUnitExpansion = { unitId ->
            viewModel.onEvent(EnrollmentProgressUiEvent.ToggleUnitExpansion(unitId))
        },
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
    onThemeClick: (String) -> Unit,
    onToggleUnitExpansion: (String) -> Unit,
    onCompleteTheme: (String) -> Unit,
    onRetry: () -> Unit
) {
    val courseTitle = uiState.enrollment?.courseTitle ?: "Progreso del Curso"

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = courseTitle,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
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
        }
    ) { innerPadding ->
        when {
            uiState.isLoading && uiState.progress == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PrimaryBlue)
                }
            }

            uiState.errorMessage != null && uiState.progress == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = uiState.errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge
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

            uiState.progress != null -> {
                ProgressScrollableList(
                    progress = uiState.progress,
                    courseImageUrl = uiState.courseImageUrl,
                    courseTitle = courseTitle,
                    daysRemaining = uiState.daysRemaining,
                    expandedUnitIds = uiState.expandedUnitIds,
                    completingThemeIds = uiState.completingThemeIds,
                    errorMessage = uiState.errorMessage,
                    onToggleUnitExpansion = onToggleUnitExpansion,
                    onThemeClick = onThemeClick,
                    onActivityClick = onActivityClick,
                    onQuizClick = onQuizClick,
                    onCompleteTheme = onCompleteTheme,
                    onRetry = onRetry,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                )
            }
        }
    }
}

@Composable
private fun ProgressScrollableList(
    progress: EnrollmentProgress,
    courseImageUrl: String?,
    courseTitle: String,
    daysRemaining: Long?,
    expandedUnitIds: Set<String>,
    completingThemeIds: Set<String>,
    errorMessage: String?,
    onToggleUnitExpansion: (String) -> Unit,
    onThemeClick: (String) -> Unit,
    onActivityClick: (String) -> Unit,
    onQuizClick: (String) -> Unit,
    onCompleteTheme: (String) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            CourseCoverImage(
                imageUrl = courseImageUrl,
                title = courseTitle
            )
        }

        item {
            OverallProgressCard(
                progress = progress,
                daysRemaining = daysRemaining
            )
        }

        progress.blockedReason?.let { reason ->
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                    border = BorderStroke(1.dp, Color(0xFFFDE68A))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = null,
                            tint = Color(0xFFD97706)
                        )
                        Text(
                            text = reason,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF92400E)
                        )
                    }
                }
            }
        }

        if (progress.percentage == 100) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CompletedGreenBg),
                    border = BorderStroke(1.dp, CompletedGreen.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(CompletedGreen),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.WorkspacePremium,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "¡Curso Completado!",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = CompletedGreen
                            )
                            Text(
                                text = "Has alcanzado el 100% de los requisitos y actividades.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        errorMessage?.let { message ->
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = message,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = onRetry) {
                            Text("Reintentar", color = MaterialTheme.colorScheme.onErrorContainer)
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "Plan de Estudio",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        itemsIndexed(progress.units, key = { _, unit -> unit.unitId }) { index, unit ->
            val isExpanded = unit.unitId in expandedUnitIds
            val isCompleted = unit.completedThemes == unit.totalThemes && unit.totalThemes > 0
            val isInProgress = !isCompleted && unit.completedThemes > 0 || (index == 0 && unit.completedThemes == 0)
            val isLocked = !isCompleted && !isInProgress

            UnitCurriculumCard(
                unitIndex = index + 1,
                unit = unit,
                isCompleted = isCompleted,
                isInProgress = isInProgress,
                isLocked = isLocked,
                isExpanded = isExpanded,
                completingThemeIds = completingThemeIds,
                onToggleExpand = { onToggleUnitExpansion(unit.unitId) },
                onThemeClick = onThemeClick,
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
    daysRemaining: Long?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "PROGRESO GENERAL",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${progress.percentage}%",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue,
                        fontSize = 32.sp
                    )
                    Text(
                        text = if (progress.percentage == 100) "Completado" else "En progreso",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue
                    )
                }

                if (daysRemaining != null) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = TimeRemainingBg
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Schedule,
                                contentDescription = null,
                                tint = TimeRemainingText,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = when {
                                    daysRemaining > 1 -> "Tiempo restante: $daysRemaining días"
                                    daysRemaining == 1L -> "Tiempo restante: 1 día"
                                    daysRemaining == 0L -> "Vence hoy"
                                    else -> "Plazo vencido"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = TimeRemainingText
                            )
                        }
                    }
                }
            }

            LinearProgressIndicator(
                progress = { progress.percentage.coerceIn(0, 100) / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp)),
                color = PrimaryBlue,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${progress.completedThemes} de ${progress.totalThemes} temas completados",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (progress.totalMandatoryActivities > 0) {
                    Text(
                        text = "${progress.approvedMandatoryActivities}/${progress.totalMandatoryActivities} actividades",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun UnitCurriculumCard(
    unitIndex: Int,
    unit: EnrollmentUnitProgress,
    isCompleted: Boolean,
    isInProgress: Boolean,
    isLocked: Boolean,
    isExpanded: Boolean,
    completingThemeIds: Set<String>,
    onToggleExpand: () -> Unit,
    onThemeClick: (String) -> Unit,
    onActivityClick: (String) -> Unit,
    onQuizClick: (String) -> Unit,
    onCompleteTheme: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isLocked, onClick = onToggleExpand),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isInProgress -> ActiveBlueBg
                isLocked -> LockedBg
                else -> MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isInProgress) 2.dp else 1.dp),
        border = BorderStroke(
            1.dp,
            when {
                isInProgress -> PrimaryBlue
                else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            }
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            when {
                                isCompleted -> CompletedGreen
                                isInProgress -> PrimaryBlue
                                else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when {
                            isCompleted -> Icons.Filled.Check
                            isInProgress -> Icons.Filled.MoreHoriz
                            else -> Icons.Filled.Lock
                        },
                        contentDescription = null,
                        tint = when {
                            isCompleted || isInProgress -> Color.White
                            else -> MaterialTheme.colorScheme.outline
                        },
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Unidad $unitIndex: ${unit.title}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isLocked) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = when {
                            isCompleted -> "${unit.completedThemes}/${unit.totalThemes} Temas • Completada"
                            isInProgress -> "${unit.completedThemes}/${unit.totalThemes} Temas • En progreso"
                            else -> "${unit.completedThemes}/${unit.totalThemes} Temas • Bloqueada"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = when {
                            isInProgress -> PrimaryBlue
                            isCompleted -> CompletedGreen
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        fontWeight = if (isInProgress || isCompleted) FontWeight.SemiBold else FontWeight.Normal
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                if (!isLocked) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Filled.KeyboardArrowDown else Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = if (isExpanded) "Colapsar" else "Expandir",
                        tint = if (isInProgress) PrimaryBlue else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            AnimatedVisibility(
                visible = isExpanded && !isLocked,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier.padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    unit.themes.forEach { theme ->
                        val isThemeCompleting = theme.themeId in completingThemeIds

                        ThemeItemRow(
                            theme = theme,
                            isCompleting = isThemeCompleting,
                            onThemeClick = { onThemeClick(theme.themeId) },
                            onCompleteTheme = { onCompleteTheme(theme.themeId) }
                        )

                        theme.activities.forEach { activity ->
                            ActivityItemRow(
                                activity = activity,
                                onActivityClick = { onActivityClick(activity.activityId) },
                                onQuizClick = { onQuizClick(activity.activityId) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ThemeItemRow(
    theme: EnrollmentThemeProgress,
    isCompleting: Boolean,
    onThemeClick: () -> Unit,
    onCompleteTheme: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onThemeClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(PrimaryBlue.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Article,
                    contentDescription = null,
                    tint = PrimaryBlue,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "TEMA ${theme.orderIndex}",
                    style = MaterialTheme.typography.labelSmall,
                    color = PrimaryBlue,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = theme.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            when {
                theme.isCompleted -> {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = CompletedGreenBg
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                tint = CompletedGreen,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Visto",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = CompletedGreen
                            )
                        }
                    }
                }

                isCompleting -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = PrimaryBlue
                    )
                }

                else -> {
                    OutlinedButton(
                        onClick = onCompleteTheme,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Completar",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ActivityItemRow(
    activity: EnrollmentActivityProgress,
    onActivityClick: () -> Unit,
    onQuizClick: () -> Unit
) {
    val isQuiz = activity.type == ActivityType.QUIZ

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 12.dp)
            .clickable {
                if (isQuiz) onQuizClick() else onActivityClick()
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (isQuiz) Color(0xFFF3E8FF) else Color(0xFFFEF3C7)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isQuiz) Icons.Outlined.Quiz else Icons.AutoMirrored.Outlined.Assignment,
                    contentDescription = null,
                    tint = if (isQuiz) Color(0xFF7E22CE) else Color(0xFFD97706),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isQuiz) "QUIZ" else "ACTIVIDAD",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isQuiz) Color(0xFF7E22CE) else Color(0xFFD97706),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = activity.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            if (activity.isApproved) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = CompletedGreenBg
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CheckCircle,
                            contentDescription = null,
                            tint = CompletedGreen,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Aprobada",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = CompletedGreen
                        )
                    }
                }
            } else {
                Button(
                    onClick = if (isQuiz) onQuizClick else onActivityClick,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isQuiz) "Iniciar Quiz" else "Entregar",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun CourseCoverImage(
    imageUrl: String?,
    title: String
) {
    if (imageUrl.isNullOrBlank()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(PrimaryBlue.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.MenuBook,
                contentDescription = null,
                tint = PrimaryBlue,
                modifier = Modifier.size(54.dp)
            )
        }
    } else {
        AsyncImage(
            model = imageUrl,
            contentDescription = "Portada de $title",
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(20.dp)),
            contentScale = ContentScale.Crop
        )
    }
}

@Preview(name = "Progreso del Curso", showBackground = true, showSystemUi = true)
@Composable
private fun EnrollmentProgressPreview() {
    MentorlyTheme {
        EnrollmentProgressContent(
            uiState = EnrollmentProgressUiState(
                isLoading = false,
                daysRemaining = 14,
                courseImageUrl = "https://images.unsplash.com/photo-1517694712202-14dd9538aa97?w=600&auto=format&fit=crop&q=80",
                enrollment = Enrollment(
                    id = "enrollment-1",
                    studentId = "student-1",
                    courseId = "course-1",
                    courseTitle = "Android with Jetpack",
                    attemptNumber = 1,
                    startedAt = "2026-08-01T10:00:00Z",
                    expiresAt = "2026-08-30T10:00:00Z",
                    completedAt = null,
                    status = EnrollmentStatus.ACTIVE
                ),
                expandedUnitIds = setOf("unit-2"),
                progress = EnrollmentProgress(
                    enrollmentId = "enrollment-1",
                    percentage = 45,
                    completedThemes = 9,
                    totalThemes = 20,
                    approvedMandatoryActivities = 4,
                    totalMandatoryActivities = 10,
                    canSubmitNextUnit = true,
                    blockedReason = null,
                    units = listOf(
                        EnrollmentUnitProgress(
                            unitId = "unit-1",
                            title = "Fundamentals",
                            completedThemes = 4,
                            totalThemes = 4,
                            approvedMandatoryActivities = 2,
                            totalMandatoryActivities = 2,
                            themes = emptyList()
                        ),
                        EnrollmentUnitProgress(
                            unitId = "unit-2",
                            title = "State Management",
                            completedThemes = 1,
                            totalThemes = 4,
                            approvedMandatoryActivities = 0,
                            totalMandatoryActivities = 2,
                            themes = listOf(
                                EnrollmentThemeProgress(
                                    themeId = "theme-1",
                                    title = "Introducción a State en Compose",
                                    contentText = "Conceptos clave de State Hoisting y Remember.",
                                    orderIndex = 1,
                                    isCompleted = true,
                                    activities = listOf(
                                        EnrollmentActivityProgress(
                                            activityId = "act-1",
                                            title = "Ejercicio de Contador",
                                            type = ActivityType.EXERCISE,
                                            isMandatory = true,
                                            isApproved = true
                                        )
                                    )
                                ),
                                EnrollmentThemeProgress(
                                    themeId = "theme-2",
                                    title = "ViewModel y StateFlow",
                                    contentText = "Integración de arquitecturas modernas.",
                                    orderIndex = 2,
                                    isCompleted = false,
                                    activities = listOf(
                                        EnrollmentActivityProgress(
                                            activityId = "act-2",
                                            title = "Quiz de Arquitectura",
                                            type = ActivityType.QUIZ,
                                            isMandatory = true,
                                            isApproved = false
                                        )
                                    )
                                )
                            )
                        ),
                        EnrollmentUnitProgress(
                            unitId = "unit-3",
                            title = "Advanced Architecture",
                            completedThemes = 0,
                            totalThemes = 5,
                            approvedMandatoryActivities = 0,
                            totalMandatoryActivities = 3,
                            themes = emptyList()
                        )
                    )
                )
            ),
            onBackClick = {},
            onActivityClick = {},
            onQuizClick = {},
            onThemeClick = {},
            onToggleUnitExpansion = {},
            onCompleteTheme = {},
            onRetry = {}
        )
    }
}
