package com.sagrd.mentorly.presentation.home

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import com.sagrd.mentorly.domain.model.course.Course
import com.sagrd.mentorly.domain.model.enrollment.Enrollment
import com.sagrd.mentorly.domain.model.enrollment.EnrollmentStatus
import com.sagrd.mentorly.ui.theme.MentorlyTheme

private val PrimaryBlue = Color(0xFF1565C0)
private val DarkerBlue = Color(0xFF0D47A1)
private val LightBlueProgress = Color(0xFF1976D2)

@Composable
fun HomeScreen(
    onCourseClick: (String) -> Unit,
    onEnrollmentClick: (String) -> Unit,
    onProfileClick: () -> Unit,
    onSeeAllEnrollments: () -> Unit = {},
    onSeeAllCourses: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    HomeBody(
        state = state,
        onEvent = viewModel::onEvent,
        onCourseClick = onCourseClick,
        onEnrollmentClick = onEnrollmentClick,
        onProfileClick = onProfileClick,
        onSeeAllEnrollments = onSeeAllEnrollments,
        onSeeAllCourses = onSeeAllCourses
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeBody(
    state: HomeUiState,
    onEvent: (HomeUiEvent) -> Unit,
    onCourseClick: (String) -> Unit,
    onEnrollmentClick: (String) -> Unit,
    onProfileClick: () -> Unit,
    onSeeAllEnrollments: () -> Unit = {},
    onSeeAllCourses: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Mentorly",
                        color = PrimaryBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )
                },
                actions = {
                    IconButton(
                        onClick = { onEvent(HomeUiEvent.Refresh) },
                        enabled = !state.isLoading && !state.isRefreshing && state.hasSession
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Refresh,
                            contentDescription = "Actualizar",
                            tint = PrimaryBlue
                        )
                    }
                    IconButton(
                        onClick = onProfileClick
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Person,
                            contentDescription = "Abrir perfil",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
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
            state.isLoading -> LoadingContent(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )

            !state.hasSession -> ErrorContent(
                message = state.errorMessage ?: "No se encontró una sesión activa.",
                onRetry = { onEvent(HomeUiEvent.Load) },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )

            else -> HomeSections(
                state = state,
                onCourseClick = onCourseClick,
                onEnrollmentClick = onEnrollmentClick,
                onSeeAllEnrollments = onSeeAllEnrollments,
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
    state: HomeUiState,
    onCourseClick: (String) -> Unit,
    onEnrollmentClick: (String) -> Unit,
    onSeeAllEnrollments: () -> Unit,
    onEvent: (HomeUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            GreetingCard(
                studentName = state.studentName,
                userPhotoUrl = state.userPhotoUrl
            )
        }

        if (state.isRefreshing) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                }
            }
        }

        state.errorMessage?.let { message ->
            item {
                ErrorCard(
                    message = message,
                    onRetry = { onEvent(HomeUiEvent.Refresh) },
                    onDismiss = { onEvent(HomeUiEvent.ClearError) }
                )
            }
        }

        // Section: Mis cursos
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Mis cursos",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Ver todos",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = PrimaryBlue,
                    modifier = Modifier.clickable(onClick = onSeeAllEnrollments)
                )
            }
        }

        if (state.activeEnrollments.isEmpty()) {
            item {
                EmptyCard(
                    message = "Aún no tienes cursos en progreso."
                )
            }
        } else {
            // First Active Course (Featured Card)
            val firstEnrollment = state.activeEnrollments.first()
            val firstProgress = state.enrollmentProgressMap[firstEnrollment.id] ?: 0
            val firstCourseImageUrl = state.publishedCourses.find { it.id == firstEnrollment.courseId }?.imageUrl

            item {
                FeaturedEnrollmentCard(
                    enrollment = firstEnrollment,
                    imageUrl = firstCourseImageUrl,
                    progressPercentage = firstProgress,
                    onClick = { onEnrollmentClick(firstEnrollment.id) }
                )
            }

            // Secondary Active Courses (Compact Cards)
            if (state.activeEnrollments.size > 1) {
                items(
                    items = state.activeEnrollments.drop(1),
                    key = { enrollment -> enrollment.id }
                ) { enrollment ->
                    val progress = state.enrollmentProgressMap[enrollment.id] ?: 0
                    CompactEnrollmentCard(
                        enrollment = enrollment,
                        progressPercentage = progress,
                        onClick = { onEnrollmentClick(enrollment.id) }
                    )
                }
            }
        }

        // Section: Cursos disponibles
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Cursos disponibles",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        if (state.publishedCourses.isEmpty()) {
            item {
                EmptyCard(
                    message = "No hay cursos disponibles por ahora."
                )
            }
        } else {
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    items(
                        items = state.publishedCourses,
                        key = { course -> course.id }
                    ) { course ->
                        AvailableCourseCard(
                            course = course,
                            onClick = { onCourseClick(course.id) }
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun GreetingCard(
    studentName: String,
    userPhotoUrl: String?,
    modifier: Modifier = Modifier
) {
    val firstName = studentName.trim().split(" ").firstOrNull()?.ifBlank { "Estudiante" } ?: "Estudiante"

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Hola, $firstName \uD83D\uDC4B",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Continúa aprendiendo hoy",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (!userPhotoUrl.isNullOrBlank()) {
                AsyncImage(
                    model = userPhotoUrl,
                    contentDescription = "Foto de perfil",
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(PrimaryBlue.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AccountCircle,
                        contentDescription = "Avatar de usuario",
                        modifier = Modifier.size(34.dp),
                        tint = PrimaryBlue
                    )
                }
            }
        }
    }
}

@Composable
private fun FeaturedEnrollmentCard(
    enrollment: Enrollment,
    imageUrl: String?,
    progressPercentage: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(175.dp)
                    .background(PrimaryBlue.copy(alpha = 0.08f))
            ) {
                if (!imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = "Portada de ${enrollment.courseTitle}",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.MenuBook,
                            contentDescription = null,
                            tint = PrimaryBlue,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 14.dp, bottom = 12.dp)
                        .background(
                            color = LightBlueProgress,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "En progreso",
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column(
                modifier = Modifier.padding(18.dp)
            ) {
                Text(
                    text = enrollment.courseTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Progreso",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$progressPercentage% completado",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = LightBlueProgress
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { progressPercentage / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = LightBlueProgress,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onClick,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkerBlue),
                        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = "Continuar",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CompactEnrollmentCard(
    enrollment: Enrollment,
    progressPercentage: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Apps,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = enrollment.courseTitle,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LinearProgressIndicator(
                        progress = { progressPercentage / 100f },
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = Color(0xFF546E7A),
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "$progressPercentage%",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = "Continuar curso",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun AvailableCourseCard(
    course: Course,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(240.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(PrimaryBlue.copy(alpha = 0.08f))
            ) {
                if (!course.imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = course.imageUrl,
                        contentDescription = "Imagen de ${course.title}",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.MenuBook,
                            contentDescription = null,
                            tint = PrimaryBlue,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .background(
                            color = Color.White.copy(alpha = 0.95f),
                            shape = RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "NUEVO",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF37474F),
                        fontSize = 10.sp
                    )
                }
            }

            Column(
                modifier = Modifier.padding(14.dp)
            ) {
                Text(
                    text = course.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = course.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Ver detalles",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBlue
                )
            }
        }
    }
}

@Composable
private fun EmptyCard(
    message: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ErrorCard(
    message: String,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = message,
                color = MaterialTheme.colorScheme.onErrorContainer,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onRetry) {
                    Text("Reintentar", color = MaterialTheme.colorScheme.onErrorContainer)
                }
                TextButton(onClick = onDismiss) {
                    Text("Cerrar", color = MaterialTheme.colorScheme.onErrorContainer)
                }
            }
        }
    }
}

@Composable
private fun LoadingContent(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = PrimaryBlue)
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

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun HomeScreenPreview() {
    MentorlyTheme {
        HomeBody(
            state = HomeUiState(
                studentName = "Adonis",
                userPhotoUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200&auto=format&fit=crop&q=80",
                activeEnrollments = listOf(
                    Enrollment(
                        id = "enrollment-1",
                        studentId = "student-1",
                        courseId = "course-1",
                        courseTitle = "Desarrollo de Aplicaciones Android",
                        attemptNumber = 1,
                        startedAt = "2026-08-12",
                        expiresAt = "2026-11-12",
                        completedAt = null,
                        status = EnrollmentStatus.ACTIVE
                    ),
                    Enrollment(
                        id = "enrollment-2",
                        studentId = "student-1",
                        courseId = "course-2",
                        courseTitle = "Fundamentos de Bases de Datos",
                        attemptNumber = 1,
                        startedAt = "2026-08-10",
                        expiresAt = "2026-11-10",
                        completedAt = null,
                        status = EnrollmentStatus.ACTIVE
                    )
                ),
                enrollmentProgressMap = mapOf(
                    "enrollment-1" to 65,
                    "enrollment-2" to 20
                ),
                publishedCourses = listOf(
                    Course(
                        id = "course-1",
                        title = "Desarrollo de Aplicaciones Android",
                        description = "Aprende Jetpack Compose, arquitectura y desarrollo moderno en Android.",
                        imageUrl = "https://images.unsplash.com/photo-1517694712202-14dd9538aa97?w=600&auto=format&fit=crop&q=80",
                        isPublished = true,
                        requiredPeerReviews = 2
                    ),
                    Course(
                        id = "course-kotlin",
                        title = "Kotlin desde cero",
                        description = "Domina el lenguaje moderno para Android con fundamentos sólidos.",
                        imageUrl = "https://images.unsplash.com/photo-1555066931-4365d14bab8c?w=600&auto=format&fit=crop&q=80",
                        isPublished = true,
                        requiredPeerReviews = 2
                    ),
                    Course(
                        id = "course-sql",
                        title = "SQL para principiantes",
                        description = "Aprende a consultar y administrar bases de datos relacionales.",
                        imageUrl = "https://images.unsplash.com/photo-1544383835-bda2bc66a55d?w=600&auto=format&fit=crop&q=80",
                        isPublished = true,
                        requiredPeerReviews = 1
                    )
                )
            ),
            onCourseClick = {},
            onEnrollmentClick = {},
            onProfileClick = {},
            onSeeAllEnrollments = {},
            onSeeAllCourses = {},
            onEvent = {}
        )
    }
}
