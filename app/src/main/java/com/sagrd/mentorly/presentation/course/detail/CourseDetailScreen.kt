package com.sagrd.mentorly.presentation.course.detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.material.icons.outlined.RateReview
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.sagrd.mentorly.domain.model.content.Activity
import com.sagrd.mentorly.domain.model.content.ActivityType
import com.sagrd.mentorly.domain.model.content.ApprovalStrategy
import com.sagrd.mentorly.domain.model.content.CourseUnit
import com.sagrd.mentorly.domain.model.content.Theme
import com.sagrd.mentorly.domain.model.course.Course
import com.sagrd.mentorly.ui.theme.MentorlyTheme

private val PrimaryBlue = Color(0xFF1565C0)
private val DarkerBlue = Color(0xFF0D47A1)
private val LightBlueCheckBg = Color(0xFFE1F5FE)
private val CheckmarkTeal = Color(0xFF0288D1)
private val ActiveBlueBg = Color(0xFFE3F2FD)
private val ChipBg = Color(0xFFF1F5F9)

@Composable
fun CourseDetailScreen(
    courseId: String,
    onBackClick: () -> Unit,
    onEnrollmentCreated: (String) -> Unit,
    onActiveEnrollmentClick: (String) -> Unit,
    onMembersClick: (String) -> Unit,
    onLeaderboardClick: (String) -> Unit,
    viewModel: CourseDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(courseId) {
        viewModel.onEvent(CourseDetailUiEvent.LoadCourseContent(courseId))
    }

    LaunchedEffect(state.createdEnrollmentId) {
        state.createdEnrollmentId?.let(onEnrollmentCreated)
    }

    CourseDetailBody(
        state = state,
        onBackClick = onBackClick,
        onEnroll = { viewModel.onEvent(CourseDetailUiEvent.Enroll) },
        onActiveEnrollmentClick = onActiveEnrollmentClick,
        onMembersClick = { onMembersClick(courseId) },
        onLeaderboardClick = { onLeaderboardClick(courseId) },
        onDismissEnrollmentError = {
            viewModel.onEvent(CourseDetailUiEvent.ClearEnrollmentError)
        },
        onRetry = {
            viewModel.onEvent(CourseDetailUiEvent.Retry)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseDetailBody(
    state: CourseDetailUiState,
    onBackClick: () -> Unit,
    onEnroll: () -> Unit,
    onActiveEnrollmentClick: (String) -> Unit,
    onMembersClick: () -> Unit,
    onLeaderboardClick: () -> Unit,
    onDismissEnrollmentError: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = state.course?.title ?: "Detalle del Curso",
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
                actions = {
                    if (state.course != null) {
                        IconButton(onClick = onMembersClick) {
                            Icon(
                                imageVector = Icons.Outlined.Group,
                                contentDescription = "Compañeros",
                                tint = PrimaryBlue
                            )
                        }
                        IconButton(onClick = onLeaderboardClick) {
                            Icon(
                                imageVector = Icons.Outlined.EmojiEvents,
                                contentDescription = "Ranking",
                                tint = PrimaryBlue
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            state.course?.let {
                CourseBottomBar(
                    isEnrolled = state.activeEnrollmentId != null,
                    isEnrolling = state.isEnrolling,
                    isChecking = state.isCheckingActiveEnrollment,
                    onEnroll = onEnroll,
                    onContinue = {
                        state.activeEnrollmentId?.let(onActiveEnrollmentClick)
                    }
                )
            }
        }
    ) { innerPadding ->
        when {
            state.isLoading && state.course == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PrimaryBlue)
                }
            }

            state.errorMessage != null && state.course == null -> {
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
                            text = state.errorMessage,
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

            state.course != null -> {
                CourseDetailScrollableContent(
                    course = state.course,
                    activeEnrollmentId = state.activeEnrollmentId,
                    progressPercentage = state.progressPercentage,
                    enrollmentErrorMessage = state.enrollmentErrorMessage,
                    onDismissEnrollmentError = onDismissEnrollmentError,
                    onActiveEnrollmentClick = onActiveEnrollmentClick,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                )
            }
        }
    }
}

@Composable
private fun CourseDetailScrollableContent(
    course: Course,
    activeEnrollmentId: String?,
    progressPercentage: Int,
    enrollmentErrorMessage: String?,
    onDismissEnrollmentError: () -> Unit,
    onActiveEnrollmentClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isEnrolled = activeEnrollmentId != null

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            CourseImage(
                imageUrl = course.imageUrl,
                title = course.title
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = course.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (course.description.isNotBlank()) {
                    Text(
                        text = course.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                CourseMetadataChips(course = course)
            }
        }

        enrollmentErrorMessage?.let { message ->
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
                        TextButton(onClick = onDismissEnrollmentError) {
                            Text("Aceptar", color = MaterialTheme.colorScheme.onErrorContainer)
                        }
                    }
                }
            }
        }

        if (isEnrolled) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Tu Progreso",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "$progressPercentage% completado",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlue
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        LinearProgressIndicator(
                            progress = { progressPercentage / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = PrimaryBlue,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }
            }
        }

        if (course.units.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = "Este curso todavía no tiene unidades publicadas.",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            val sortedUnits = course.units.sortedBy { it.orderIndex }
            sortedUnits.forEachIndexed { unitIndex, unit ->
                item {
                    Text(
                        text = "Unidad ${unit.orderIndex}: ${unit.title}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                val sortedThemes = unit.themes.sortedBy { it.orderIndex }
                items(sortedThemes, key = { "theme_${it.id}" }) { theme ->
                    val isFirstItem = unitIndex == 0 && theme == sortedThemes.firstOrNull()
                    val isCompleted = isEnrolled && progressPercentage > 50

                    ContentCard(
                        icon = Icons.AutoMirrored.Outlined.Article,
                        categoryLabel = if (isEnrolled && isFirstItem) "TEMA ACTUAL" else "TEMA",
                        title = theme.title,
                        isEnrolled = isEnrolled,
                        isCompleted = isCompleted,
                        isCurrent = isEnrolled && isFirstItem,
                        onClick = {
                            activeEnrollmentId?.let(onActiveEnrollmentClick)
                        }
                    )
                }

                val allActivities = sortedThemes.flatMap { it.activities }.sortedBy { it.orderIndex }
                items(allActivities, key = { "act_${it.id}" }) { activity ->
                    val isCompleted = isEnrolled && progressPercentage == 100

                    ContentCard(
                        icon = if (activity.type == ActivityType.QUIZ) Icons.Outlined.Quiz else Icons.AutoMirrored.Outlined.Assignment,
                        categoryLabel = if (activity.type == ActivityType.QUIZ) "QUIZ" else "ACTIVIDAD",
                        title = activity.title,
                        isEnrolled = isEnrolled,
                        isCompleted = isCompleted,
                        isCurrent = false,
                        onClick = {
                            activeEnrollmentId?.let(onActiveEnrollmentClick)
                        }
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CourseMetadataChips(course: Course) {
    val totalThemes = course.units.sumOf { it.themes.size }
    val totalActivities = course.units.sumOf { unit -> unit.themes.sumOf { it.activities.size } }

    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        MetadataChip(
            icon = Icons.Outlined.RateReview,
            text = "Mínimo ${course.requiredPeerReviews} revisiones entre pares"
        )
        MetadataChip(
            icon = Icons.AutoMirrored.Outlined.MenuBook,
            text = "${course.units.size} Unidades"
        )
        if (totalThemes > 0) {
            MetadataChip(
                icon = Icons.AutoMirrored.Outlined.Article,
                text = "$totalThemes Temas"
            )
        }
        if (totalActivities > 0) {
            MetadataChip(
                icon = Icons.AutoMirrored.Outlined.Assignment,
                text = "$totalActivities Actividades"
            )
        }
        MetadataChip(
            icon = Icons.Outlined.WorkspacePremium,
            text = "Certificado digital al 100%"
        )
    }
}

@Composable
private fun MetadataChip(
    icon: ImageVector,
    text: String
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = ChipBg
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = PrimaryBlue
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ContentCard(
    icon: ImageVector,
    categoryLabel: String,
    title: String,
    isEnrolled: Boolean,
    isCompleted: Boolean,
    isCurrent: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = isEnrolled, onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrent) ActiveBlueBg else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(
            1.dp,
            if (isCurrent) PrimaryBlue else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isCurrent) PrimaryBlue else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isCurrent) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = categoryLabel,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isCurrent) PrimaryBlue else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (!isEnrolled) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            when {
                !isEnrolled -> {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = "Bloqueado",
                        tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp)
                    )
                }

                isCurrent -> {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(PrimaryBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = "Continuar tema",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                isCompleted -> {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(LightBlueCheckBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = "Completado",
                            tint = CheckmarkTeal,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                else -> {
                    Icon(
                        imageVector = Icons.Outlined.CheckCircle,
                        contentDescription = "Pendiente",
                        tint = MaterialTheme.colorScheme.outlineVariant,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CourseBottomBar(
    isEnrolled: Boolean,
    isEnrolling: Boolean,
    isChecking: Boolean,
    onEnroll: () -> Unit,
    onContinue: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            when {
                isChecking -> {
                    Button(
                        onClick = {},
                        enabled = false,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = PrimaryBlue,
                            strokeWidth = 2.dp
                        )
                    }
                }

                isEnrolled -> {
                    Button(
                        onClick = onContinue,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkerBlue),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text(
                            text = "Continuar Aprendiendo",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                else -> {
                    Button(
                        onClick = onEnroll,
                        enabled = !isEnrolling,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkerBlue),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        if (isEnrolling) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Inscribirse",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CourseImage(
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
            contentDescription = "Portada del curso $title",
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(20.dp)),
            contentScale = ContentScale.Crop
        )
    }
}

@Preview(name = "Inscrito (En progreso)", showBackground = true, showSystemUi = true)
@Composable
private fun CourseDetailEnrolledPreview() {
    MentorlyTheme {
        CourseDetailBody(
            state = CourseDetailUiState(
                isLoading = false,
                activeEnrollmentId = "enrollment-1",
                progressPercentage = 65,
                course = Course(
                    id = "course-1",
                    title = "Desarrollo de Aplicaciones Android",
                    description = "Aprende Jetpack Compose, arquitectura MVI y desarrollo moderno en Android con Kotlin.",
                    imageUrl = "https://images.unsplash.com/photo-1517694712202-14dd9538aa97?w=600&auto=format&fit=crop&q=80",
                    isPublished = true,
                    requiredPeerReviews = 2,
                    units = listOf(
                        CourseUnit(
                            id = "unit-1",
                            courseId = "course-1",
                            title = "Fundamentos de Android",
                            orderIndex = 1,
                            themes = listOf(
                                Theme(
                                    id = "theme-1",
                                    unitId = "unit-1",
                                    title = "Introducción a Kotlin",
                                    contentText = "Sintaxis básica, funciones y tipos.",
                                    orderIndex = 1,
                                    activities = emptyList()
                                ),
                                Theme(
                                    id = "theme-2",
                                    unitId = "unit-1",
                                    title = "Mi Primera App",
                                    contentText = "Estructura del proyecto y ciclo de vida.",
                                    orderIndex = 2,
                                    activities = listOf(
                                        Activity(
                                            id = "act-1",
                                            themeId = "theme-2",
                                            title = "Mi Primera App",
                                            type = ActivityType.EXERCISE,
                                            isMandatory = true,
                                            approvalStrategy = ApprovalStrategy.PEER_REVIEW,
                                            orderIndex = 1
                                        )
                                    )
                                )
                            )
                        ),
                        CourseUnit(
                            id = "unit-2",
                            courseId = "course-1",
                            title = "Interfaz de Usuario",
                            orderIndex = 2,
                            themes = listOf(
                                Theme(
                                    id = "theme-3",
                                    unitId = "unit-2",
                                    title = "Layouts y Vistas",
                                    contentText = "Filas, Columnas y Modificadores en Compose.",
                                    orderIndex = 1,
                                    activities = emptyList()
                                )
                            )
                        )
                    )
                )
            ),
            onBackClick = {},
            onEnroll = {},
            onActiveEnrollmentClick = {},
            onMembersClick = {},
            onLeaderboardClick = {},
            onDismissEnrollmentError = {},
            onRetry = {}
        )
    }
}

@Preview(name = "No Inscrito (Bloqueado)", showBackground = true, showSystemUi = true)
@Composable
private fun CourseDetailUnenrolledPreview() {
    MentorlyTheme {
        CourseDetailBody(
            state = CourseDetailUiState(
                isLoading = false,
                activeEnrollmentId = null,
                progressPercentage = 0,
                course = Course(
                    id = "course-1",
                    title = "Desarrollo de Aplicaciones Android",
                    description = "Aprende Jetpack Compose, arquitectura MVI y desarrollo moderno en Android con Kotlin.",
                    imageUrl = "https://images.unsplash.com/photo-1517694712202-14dd9538aa97?w=600&auto=format&fit=crop&q=80",
                    isPublished = true,
                    requiredPeerReviews = 2,
                    units = listOf(
                        CourseUnit(
                            id = "unit-1",
                            courseId = "course-1",
                            title = "Fundamentos de Android",
                            orderIndex = 1,
                            themes = listOf(
                                Theme(
                                    id = "theme-1",
                                    unitId = "unit-1",
                                    title = "Introducción a Kotlin",
                                    contentText = "Sintaxis básica, funciones y tipos.",
                                    orderIndex = 1,
                                    activities = emptyList()
                                )
                            )
                        )
                    )
                )
            ),
            onBackClick = {},
            onEnroll = {},
            onActiveEnrollmentClick = {},
            onMembersClick = {},
            onLeaderboardClick = {},
            onDismissEnrollmentError = {},
            onRetry = {}
        )
    }
}
