package com.sagrd.mentorly.presentation.admin.student.detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.sagrd.mentorly.domain.model.content.ActivityType
import com.sagrd.mentorly.domain.model.enrollment.Enrollment
import com.sagrd.mentorly.domain.model.enrollment.EnrollmentStatus
import com.sagrd.mentorly.domain.model.progress.EnrollmentActivityProgress
import com.sagrd.mentorly.domain.model.progress.EnrollmentProgress
import com.sagrd.mentorly.domain.model.progress.EnrollmentThemeProgress
import com.sagrd.mentorly.domain.model.progress.EnrollmentUnitProgress
import com.sagrd.mentorly.domain.model.student.Student
import com.sagrd.mentorly.domain.model.student.StudentRole
import com.sagrd.mentorly.ui.theme.MentorlyTheme

@Composable
fun AdminStudentDetailScreen(
    studentId: String,
    onBackClick: () -> Unit,
    viewModel: AdminStudentDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(studentId) {
        viewModel.setStudentId(studentId)
    }

    AdminStudentDetailContent(
        uiState = uiState,
        onBackClick = onBackClick,
        onEvent = viewModel::onEvent
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminStudentDetailContent(
    uiState: AdminStudentDetailUiState,
    onBackClick: () -> Unit,
    onEvent: (AdminStudentDetailUiEvent) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle del estudiante", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.isLoading && uiState.student == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (uiState.errorMessage != null && uiState.student == null) {
            ErrorView(message = uiState.errorMessage, onRetry = { onEvent(AdminStudentDetailUiEvent.Load) })
        } else if (uiState.student != null) {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Profile Header
                item {
                    StudentProfileHeader(student = uiState.student)
                }

                // Enrollments Section
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Inscripciones (${uiState.enrollments.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        TextButton(onClick = { /* Ver todas if implemented */ }) {
                            Text("Ver todas")
                        }
                    }
                }

                if (uiState.enrollments.isEmpty()) {
                    item {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .padding(24.dp), contentAlignment = Alignment.Center
                        ) {
                            Text("Sin inscripciones activas.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                } else {
                    items(uiState.enrollments, key = { it.id }) { enrollment ->
                        val progress = uiState.enrollmentProgress[enrollment.id]
                        val isExpanded = uiState.expandedEnrollmentIds.contains(enrollment.id)

                        EnrollmentCard(
                            enrollment = enrollment,
                            progress = progress,
                            isExpanded = isExpanded,
                            onToggleExpansion = { onEvent(AdminStudentDetailUiEvent.ToggleEnrollmentExpansion(enrollment.id)) }
                        )
                    }
                }
                
                item { Spacer(modifier = Modifier.height(32.dp)) }
            }
        }
    }
}

@Composable
private fun StudentProfileHeader(student: Student) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(contentAlignment = Alignment.BottomEnd) {
                // Circle Avatar
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    val initials = student.displayName.split(" ").filter { it.isNotEmpty() }.take(2).map { it[0] }.joinToString("").uppercase()
                    Text(text = initials, style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
                
                // Star badge
                Surface(
                    modifier = Modifier.size(32.dp).border(2.dp, Color.White, CircleShape),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = Color.White, modifier = Modifier.padding(6.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = student.displayName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(text = student.email ?: "Sin correo", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(modifier = Modifier.height(20.dp))

            // Badges section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BadgeChip(
                        icon = Icons.Default.School,
                        text = if (student.role == StudentRole.ADMIN) "Administrador" else "Estudiante Activa",
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    BadgeChip(
                        icon = Icons.Default.Stars,
                        text = "${"%,d".format(student.totalPoints)} Puntos",
                        containerColor = Color(0xFF6750A4).copy(alpha = 0.1f),
                        contentColor = Color(0xFF6750A4)
                    )
                }

                BadgeChip(
                    icon = if (student.isLeaderboardPublic) Icons.Default.Public else Icons.Default.VisibilityOff,
                    text = if (student.isLeaderboardPublic) "Perfil Público" else "Perfil Privado",
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun BadgeChip(
    icon: ImageVector,
    text: String,
    containerColor: Color,
    contentColor: Color
) {
    Surface(
        color = containerColor,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = contentColor)
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = text, style = MaterialTheme.typography.labelMedium, color = contentColor, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun EnrollmentCard(
    enrollment: Enrollment,
    progress: EnrollmentProgress?,
    isExpanded: Boolean,
    onToggleExpansion: () -> Unit
) {
    val rotation by animateFloatAsState(targetValue = if (isExpanded) 180f else 0f)

    Card(
        modifier = Modifier.fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f), RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = enrollment.courseTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                
                StatusBadge(status = enrollment.status)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Intento ${enrollment.attemptNumber}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(4.dp))
                    // Simple date display
                    val dateRange = "${enrollment.startedAt.take(10)} - ${enrollment.completedAt?.take(10) ?: "Presente"}"
                    Text(dateRange, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
                onClick = onToggleExpansion,
                modifier = Modifier.align(Alignment.End),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(if (isExpanded) "Ocultar progreso" else "Ver progreso", style = MaterialTheme.typography.labelLarge)
                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = null,
                    modifier = Modifier.graphicsLayer { rotationZ = rotation }
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    if (progress == null) {
                        Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        }
                    } else {
                        ProgressContent(progress = progress)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(status: EnrollmentStatus) {
    val color = when (status) {
        EnrollmentStatus.ACTIVE -> Color(0xFF0066FF)
        EnrollmentStatus.COMPLETED -> Color(0xFF4CAF50)
        EnrollmentStatus.EXPIRED -> Color(0xFFF44336)
    }
    val label = when (status) {
        EnrollmentStatus.ACTIVE -> "Activa"
        EnrollmentStatus.COMPLETED -> "Completada"
        EnrollmentStatus.EXPIRED -> "Expirada"
    }
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ProgressContent(progress: EnrollmentProgress) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Progreso General", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("${progress.percentage}%", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        }
        
        LinearProgressIndicator(
            progress = progress.percentage.toFloat() / 100f,
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            Column {
                Text("Temas: ${progress.completedThemes}/${progress.totalThemes}", style = MaterialTheme.typography.labelMedium)
            }
            Column {
                Text("Actividades: ${progress.approvedMandatoryActivities}/${progress.totalMandatoryActivities}", style = MaterialTheme.typography.labelMedium)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
        Spacer(modifier = Modifier.height(8.dp))

        // Units
        progress.units.forEach { unit ->
            UnitProgressItem(unit = unit)
        }
    }
}

@Composable
private fun UnitProgressItem(unit: EnrollmentUnitProgress) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.width(4.dp).height(40.dp).background(MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp)))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = "Unidad ${unit.unitId}: ${unit.title}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                val unitPercentage = if (unit.totalThemes > 0) (unit.completedThemes * 100) / unit.totalThemes else 0
                Text(text = "Progreso $unitPercentage%", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))

        // Nested themes
        Column(modifier = Modifier.padding(start = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            unit.themes.forEach { theme ->
                ThemeProgressItem(theme = theme)
            }
        }
    }
}

@Composable
private fun ThemeProgressItem(theme: EnrollmentThemeProgress) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (theme.isCompleted) Icons.Default.CheckCircle else Icons.Outlined.Circle,
                    contentDescription = null,
                    tint = if (theme.isCompleted) Color(0xFF0066FF) else MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = theme.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            }
            
            if (theme.contentText.isNotBlank()) {
                Text(
                    text = theme.contentText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 28.dp, top = 4.dp),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Activities
            if (theme.activities.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Column(modifier = Modifier.padding(start = 28.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    theme.activities.forEach { activity ->
                        ActivityProgressRow(activity = activity)
                    }
                }
            }
        }
    }
}

@Composable
private fun ActivityProgressRow(activity: EnrollmentActivityProgress) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(8.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (activity.type == ActivityType.QUIZ) Icons.Default.Quiz else Icons.Default.Assignment,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = activity.title, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    if (activity.isMandatory) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f), shape = RoundedCornerShape(4.dp)) {
                            Text(
                                "OBLIGATORIA",
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
                Text(
                    text = "Tipo: ${if (activity.type == ActivityType.QUIZ) "Cuestionario" else "Ejercicio"} • ${if (activity.isApproved) "Aprobada" else "Pendiente"}",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (activity.isApproved) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (activity.isApproved) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(16.dp))
            }
        }
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

@Preview(showBackground = true)
@Composable
fun AdminStudentDetailScreenPreview() {
    MentorlyTheme {
        AdminStudentDetailContent(
            uiState = AdminStudentDetailUiState(
                student = Student("1", "elena.rodriguez@university.edu", "Elena Rodríguez", StudentRole.STUDENT, true, 1250),
                enrollments = listOf(
                    Enrollment("e1", "1", "c1", "Introducción al Diseño UX/UI", 1, "2026-10-12", "2027-01-12", null, EnrollmentStatus.ACTIVE),
                    Enrollment("e2", "1", "c2", "Desarrollo Web Full-Stack", 2, "2026-01-10", "2026-03-15", "2026-03-15", EnrollmentStatus.COMPLETED)
                ),
                expandedEnrollmentIds = setOf("e2"),
                enrollmentProgress = mapOf(
                    "e2" to EnrollmentProgress(
                        enrollmentId = "e2",
                        percentage = 100,
                        completedThemes = 8,
                        totalThemes = 8,
                        approvedMandatoryActivities = 4,
                        totalMandatoryActivities = 4,
                        canSubmitNextUnit = true,
                        blockedReason = null,
                        units = listOf(
                            EnrollmentUnitProgress(
                                unitId = "1",
                                title = "Frontend Basics",
                                completedThemes = 1,
                                totalThemes = 1,
                                approvedMandatoryActivities = 1,
                                totalMandatoryActivities = 1,
                                themes = listOf(
                                    EnrollmentThemeProgress(
                                        themeId = "t1",
                                        title = "HTML y Semántica",
                                        contentText = "Estructuración correcta de documentos web.",
                                        orderIndex = 1,
                                        isCompleted = true,
                                        activities = listOf(
                                            EnrollmentActivityProgress(
                                                activityId = "a1",
                                                title = "Proyecto de Maquetación",
                                                isMandatory = true,
                                                isApproved = true,
                                                type = ActivityType.EXERCISE
                                            )
                                        )
                                    )
                                )
                            )
                        )
                    )
                )
            ),
            onBackClick = {},
            onEvent = {}
        )
    }
}
