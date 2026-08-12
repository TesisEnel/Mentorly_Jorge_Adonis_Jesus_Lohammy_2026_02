package com.sagrd.mentorly.presentation.home

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import com.sagrd.mentorly.domain.model.course.Course
import com.sagrd.mentorly.domain.model.enrollment.Enrollment
import com.sagrd.mentorly.domain.model.enrollment.EnrollmentStatus
import com.sagrd.mentorly.ui.theme.MentorlyTheme

@Composable
fun HomeScreen(
    onCourseClick: (String) -> Unit,
    onEnrollmentClick: (String) -> Unit,
    onProfileClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    HomeContent(
        uiState = uiState,
        onCourseClick = onCourseClick,
        onEnrollmentClick = onEnrollmentClick,
        onProfileClick = onProfileClick,
        onEvent = viewModel::onEvent
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeContent(
    uiState: HomeUiState,
    onCourseClick: (String) -> Unit,
    onEnrollmentClick: (String) -> Unit,
    onProfileClick: () -> Unit,
    onEvent: (HomeUiEvent) -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Inicio",
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(
                        onClick = { onEvent(HomeUiEvent.Refresh) },
                        enabled = !uiState.isLoading && !uiState.isRefreshing && uiState.hasSession
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Refresh,
                            contentDescription = "Actualizar"
                        )
                    }
                    IconButton(onClick = onProfileClick) {
                        Icon(
                            imageVector = Icons.Outlined.AccountCircle,
                            contentDescription = "Abrir perfil"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        when {
            uiState.isLoading -> LoadingContent(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )

            !uiState.hasSession -> ErrorContent(
                message = uiState.errorMessage ?: "No se encontró una sesión activa.",
                onRetry = { onEvent(HomeUiEvent.Load) },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )

            else -> HomeSections(
                uiState = uiState,
                onCourseClick = onCourseClick,
                onEnrollmentClick = onEnrollmentClick,
                onEvent = onEvent,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        }
    }
}

@Composable
private fun HomeSections(
    uiState: HomeUiState,
    onCourseClick: (String) -> Unit,
    onEnrollmentClick: (String) -> Unit,
    onEvent: (HomeUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Hola, ${uiState.studentName}",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        if (uiState.isRefreshing) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                }
            }
        }

        uiState.errorMessage?.let { message ->
            item {
                ErrorCard(
                    message = message,
                    onRetry = { onEvent(HomeUiEvent.Refresh) },
                    onDismiss = { onEvent(HomeUiEvent.ClearError) }
                )
            }
        }

        item {
            SectionTitle("Continúa aprendiendo")
        }

        if (uiState.activeEnrollments.isEmpty()) {
            item {
                EmptySection("Aún no tienes cursos en progreso.")
            }
        } else {
            items(
                items = uiState.activeEnrollments,
                key = { enrollment -> enrollment.id }
            ) { enrollment ->
                EnrollmentCard(
                    enrollment = enrollment,
                    courseTitle = uiState.publishedCourses
                        .firstOrNull { course -> course.id == enrollment.courseId }
                        ?.title
                        ?: "Curso en progreso",
                    onClick = { onEnrollmentClick(enrollment.id) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(4.dp))
            SectionTitle("Explora cursos")
        }

        if (uiState.publishedCourses.isEmpty()) {
            item {
                EmptySection("No hay cursos disponibles por ahora.")
            }
        } else {
            items(
                items = uiState.publishedCourses,
                key = { course -> course.id }
            ) { course ->
                CourseCard(
                    course = course,
                    onClick = { onCourseClick(course.id) }
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
private fun EnrollmentCard(
    enrollment: Enrollment,
    courseTitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = courseTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = enrollment.status.label(),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelLarge
            )
            Text(
                text = "Intento ${enrollment.attemptNumber}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "Disponible hasta ${enrollment.expiresAt}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun CourseCard(
    course: Course,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row {
            CourseImage(
                imageUrl = course.imageUrl,
                title = course.title
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp)
            ) {
                Text(
                    text = course.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = course.description,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
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
                .size(112.dp)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.MenuBook,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    } else {
        AsyncImage(
            model = imageUrl,
            contentDescription = "Imagen del curso $title",
            modifier = Modifier
                .size(112.dp)
                .clip(RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)),
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
private fun EmptySection(message: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = message,
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun ErrorCard(
    message: String,
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error
            )
            Row {
                TextButton(onClick = onRetry) {
                    Text("Reintentar")
                }
                TextButton(onClick = onDismiss) {
                    Text("Cerrar")
                }
            }
        }
    }
}

@Composable
private fun LoadingContent(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.padding(24.dp), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error
            )
            Button(onClick = onRetry) {
                Text("Reintentar")
            }
        }
    }
}

private fun EnrollmentStatus.label(): String = when (this) {
    EnrollmentStatus.ACTIVE -> "En progreso"
    EnrollmentStatus.COMPLETED -> "Completada"
    EnrollmentStatus.EXPIRED -> "Expirada"
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun HomeScreenPreview() {
    MentorlyTheme {
        HomeContent(
            uiState = HomeUiState(
                studentName = "Adonis",
                activeEnrollments = listOf(
                    Enrollment(
                        id = "enrollment-1",
                        studentId = "student-1",
                        courseId = "course-1",
                        attemptNumber = 1,
                        startedAt = "2026-08-12",
                        expiresAt = "2026-11-12",
                        completedAt = null,
                        status = EnrollmentStatus.ACTIVE
                    )
                ),
                publishedCourses = listOf(
                    Course(
                        id = "course-1",
                        title = "Android con Jetpack Compose",
                        description = "Construye aplicaciones modernas con interfaces declarativas.",
                        imageUrl = null,
                        isPublished = true,
                        requiredPeerReviews = 2
                    ),
                    Course(
                        id = "course-2",
                        title = "Fundamentos de Git",
                        description = "Aprende control de versiones y colaboración en equipo.",
                        imageUrl = null,
                        isPublished = true,
                        requiredPeerReviews = 1
                    )
                )
            ),
            onCourseClick = {},
            onEnrollmentClick = {},
            onProfileClick = {},
            onEvent = {}
        )
    }
}
