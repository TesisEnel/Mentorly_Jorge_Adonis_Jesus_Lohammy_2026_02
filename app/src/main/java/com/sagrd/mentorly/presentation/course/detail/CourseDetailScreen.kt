package com.sagrd.mentorly.presentation.course.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import com.sagrd.mentorly.domain.model.content.Activity
import com.sagrd.mentorly.domain.model.content.ActivityType
import com.sagrd.mentorly.domain.model.content.ApprovalStrategy
import com.sagrd.mentorly.domain.model.content.CourseUnit
import com.sagrd.mentorly.domain.model.content.Theme
import com.sagrd.mentorly.domain.model.course.Course
import com.sagrd.mentorly.ui.theme.MentorlyTheme

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
    val state by viewModel.state.collectAsState()

    LaunchedEffect(courseId) {
        viewModel.onEvent(CourseDetailUiEvent.LoadCourseContent(courseId))
    }

    LaunchedEffect(state.createdEnrollmentId) {
        state.createdEnrollmentId?.let(onEnrollmentCreated)
    }

    CourseDetailContent(
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
private fun CourseDetailContent(
    state: CourseDetailUiState,
    onBackClick: () -> Unit,
    onEnroll: () -> Unit,
    onActiveEnrollmentClick: (String) -> Unit,
    onMembersClick: () -> Unit,
    onLeaderboardClick: () -> Unit,
    onDismissEnrollmentError: () -> Unit,
    onRetry: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("Detalle del curso")
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Outlined.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    if (state.course != null) {
                        IconButton(onClick = onMembersClick) {
                            Icon(
                                imageVector = Icons.Outlined.Group,
                                contentDescription = "Compañeros"
                            )
                        }
                        IconButton(onClick = onLeaderboardClick) {
                            Icon(
                                imageVector = Icons.Outlined.EmojiEvents,
                                contentDescription = "Ranking"
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        when {
            state.isLoading && state.course == null -> {
                LoadingContent(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                )
            }

            state.errorMessage != null && state.course == null -> {
                ErrorContent(
                    message = state.errorMessage,
                    onRetry = onRetry,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                )
            }

            state.course != null -> {
                CourseContent(
                    course = state.course,
                    isEnrolling = state.isEnrolling,
                    isCheckingActiveEnrollment = state.isCheckingActiveEnrollment,
                    activeEnrollmentId = state.activeEnrollmentId,
                    enrollmentErrorMessage = state.enrollmentErrorMessage,
                    onEnroll = onEnroll,
                    onActiveEnrollmentClick = onActiveEnrollmentClick,
                    onDismissEnrollmentError = onDismissEnrollmentError,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                )
            }
        }
    }
}

@Composable
private fun CourseContent(
    course: Course,
    isEnrolling: Boolean,
    isCheckingActiveEnrollment: Boolean,
    activeEnrollmentId: String?,
    enrollmentErrorMessage: String?,
    onEnroll: () -> Unit,
    onActiveEnrollmentClick: (String) -> Unit,
    onDismissEnrollmentError: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            CourseHeader(
                course = course,
                isEnrolling = isEnrolling,
                isCheckingActiveEnrollment = isCheckingActiveEnrollment,
                activeEnrollmentId = activeEnrollmentId,
                onActiveEnrollmentClick = onActiveEnrollmentClick,
                onEnroll = onEnroll
            )
        }

        enrollmentErrorMessage?.let { message ->
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text(message, color = MaterialTheme.colorScheme.error)
                        TextButton(onClick = onDismissEnrollmentError) {
                            Text("Aceptar")
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "Contenido del curso",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        if (course.units.isEmpty()) {
            item {
                Text(
                    text = "Este curso todavía no tiene contenido disponible.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            items(
                items = course.units.sortedBy { unit -> unit.orderIndex },
                key = { unit -> unit.id }
            ) { unit ->
                UnitCard(unit)
            }
        }
    }
}

@Composable
private fun CourseHeader(
    course: Course,
    isEnrolling: Boolean,
    isCheckingActiveEnrollment: Boolean,
    activeEnrollmentId: String?,
    onActiveEnrollmentClick: (String) -> Unit,
    onEnroll: () -> Unit
) {
    Column {
        CourseImage(
            imageUrl = course.imageUrl,
            title = course.title
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = course.title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = course.description,
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Requiere ${course.requiredPeerReviews} revisiones entre pares",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )

        if (course.isPublished) {
            Spacer(modifier = Modifier.height(16.dp))

            when {
                isCheckingActiveEnrollment -> {
                    Button(
                        onClick = {},
                        enabled = false,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Comprobando inscripción...")
                    }
                }

                activeEnrollmentId != null -> {
                    Button(
                        onClick = { onActiveEnrollmentClick(activeEnrollmentId) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Ir a mi curso")
                    }
                }

                else -> {
                    Button(
                        onClick = onEnroll,
                        enabled = !isEnrolling,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (isEnrolling) "Inscribiendo..." else "Inscribirme al curso")
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
                .height(190.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.MenuBook,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(64.dp)
            )
        }
    } else {
        AsyncImage(
            model = imageUrl,
            contentDescription = "Imagen del curso $title",
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp)
                .clip(RoundedCornerShape(16.dp)),
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
private fun UnitCard(unit: CourseUnit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Unidad ${unit.orderIndex}: ${unit.title}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            unit.themes
                .sortedBy { theme -> theme.orderIndex }
                .forEach { theme ->
                    ThemeContent(theme)
                }
        }
    }
}

@Composable
private fun ThemeContent(theme: Theme) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        HorizontalDivider()

        Text(
            text = "Tema ${theme.orderIndex}: ${theme.title}",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium
        )

        if (theme.contentText.isNotBlank()) {
            Text(
                text = theme.contentText,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (theme.activities.isEmpty()) {
            Text(
                text = "Sin actividades.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            theme.activities
                .sortedBy { activity -> activity.orderIndex }
                .forEach { activity ->
                    ActivityContent(activity)
                }
        }
    }
}

@Composable
private fun ActivityContent(activity: Activity) {
    Column(
        modifier = Modifier.padding(start = 8.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = "${activity.orderIndex}. ${activity.title}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )

        Text(
            text = "${activity.type.label()} · ${activity.requirementLabel()} · ${activity.approvalStrategy.label()}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun ActivityType.label(): String {
    return when (this) {
        ActivityType.EXERCISE -> "Ejercicio"
        ActivityType.QUIZ -> "Cuestionario"
    }
}

private fun Activity.requirementLabel(): String {
    return if (isMandatory) "Obligatoria" else "Opcional"
}

private fun ApprovalStrategy.label(): String {
    return when (this) {
        ApprovalStrategy.AUTO -> "Aprobación automática"
        ApprovalStrategy.PEER_REVIEW -> "Revisión por pares"
        ApprovalStrategy.ADMIN -> "Revisión administrativa"
    }
}

@Composable
private fun LoadingContent(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.material3.CircularProgressIndicator()
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyLarge
            )

            Button(onClick = onRetry) {
                Text("Reintentar")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CourseDetailPreview() {
    MentorlyTheme {
        CourseDetailContent(
            state = CourseDetailUiState(
                course = Course(
                    id = "course-1",
                    title = "Android con Jetpack Compose",
                    description = "Aprende a construir interfaces modernas y reactivas para Android.",
                    imageUrl = null,
                    isPublished = true,
                    requiredPeerReviews = 2,
                    units = listOf(
                        CourseUnit(
                            id = "unit-1",
                            courseId = "course-1",
                            title = "Fundamentos de Compose",
                            orderIndex = 1,
                            themes = listOf(
                                Theme(
                                    id = "theme-1",
                                    unitId = "unit-1",
                                    title = "Componentes composables",
                                    contentText = "Conoce cómo se construyen y reutilizan componentes en Jetpack Compose.",
                                    orderIndex = 1,
                                    activities = listOf(
                                        Activity(
                                            id = "activity-1",
                                            themeId = "theme-1",
                                            title = "Crear una pantalla básica",
                                            type = ActivityType.EXERCISE,
                                            isMandatory = true,
                                            approvalStrategy = ApprovalStrategy.PEER_REVIEW,
                                            orderIndex = 1
                                        )
                                    )
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
