package com.sagrd.mentorly.presentation.course.list

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Search
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
private val LightBlueAction = Color(0xFFE3F2FD)
private val AmberStar = Color(0xFFFFA000)

@Composable
fun CourseListScreen(
    onCourseClick: (String) -> Unit,
    onEnrollmentClick: (String) -> Unit = onCourseClick,
    onSubmissionsClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    viewModel: CourseListViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    CourseListBody(
        state = state,
        onEvent = viewModel::onEvent,
        onCourseClick = onCourseClick,
        onEnrollmentClick = onEnrollmentClick,
        onSubmissionsClick = onSubmissionsClick,
        onProfileClick = onProfileClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseListBody(
    state: CourseListUiState,
    onEvent: (CourseListUiEvent) -> Unit,
    onCourseClick: (String) -> Unit,
    onEnrollmentClick: (String) -> Unit = onCourseClick,
    onSubmissionsClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
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
                    IconButton(onClick = onSubmissionsClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.Assignment,
                            contentDescription = "Mis entregas",
                            tint = PrimaryBlue
                        )
                    }
                    IconButton(
                        onClick = { onEvent(CourseListUiEvent.Refresh) },
                        enabled = !state.isLoading && !state.isRefreshing && state.hasSession
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Refresh,
                            contentDescription = "Actualizar",
                            tint = PrimaryBlue
                        )
                    }
                    IconButton(onClick = onProfileClick) {
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
            state.isLoading && state.courses.isEmpty() && state.activeEnrollments.isEmpty() -> {
                LoadingContent(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                )
            }

            !state.hasSession -> {
                ErrorContent(
                    message = state.errorMessage ?: "No se encontró una sesión activa.",
                    onRetry = { onEvent(CourseListUiEvent.Refresh) },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                )
            }

            else -> {
                CourseListSections(
                    state = state,
                    onEvent = onEvent,
                    onCourseClick = onCourseClick,
                    onEnrollmentClick = onEnrollmentClick,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                )
            }
        }
    }
}

@Composable
private fun CourseListSections(
    state: CourseListUiState,
    onEvent: (CourseListUiEvent) -> Unit,
    onCourseClick: (String) -> Unit,
    onEnrollmentClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Search Bar
        item {
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { onEvent(CourseListUiEvent.SearchQueryChanged(it)) },
                placeholder = {
                    Text(
                        text = "Buscar cursos...",
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = "Buscar",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    if (state.searchQuery.isNotBlank()) {
                        IconButton(onClick = { onEvent(CourseListUiEvent.SearchQueryChanged("")) }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Limpiar búsqueda",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryBlue,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
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
                    onRetry = { onEvent(CourseListUiEvent.Refresh) },
                    onDismiss = { onEvent(CourseListUiEvent.ClearError) }
                )
            }
        }

        // Section: Mis Cursos
        if (state.filteredEnrollments.isNotEmpty()) {
            item {
                Text(
                    text = "Mis Cursos",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            items(
                items = state.filteredEnrollments,
                key = { enrollment -> enrollment.id }
            ) { enrollment ->
                val progress = state.enrollmentProgressMap[enrollment.id] ?: 0
                val matchingCourse = state.courses.find { it.id == enrollment.courseId }
                val imageUrl = matchingCourse?.imageUrl

                ActiveCourseCard(
                    enrollment = enrollment,
                    imageUrl = imageUrl,
                    progressPercentage = progress,
                    onClick = { onEnrollmentClick(enrollment.id) }
                )
            }
        }

        // Section: Explorar Catálogo
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Explorar Catálogo",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        if (state.filteredCourses.isEmpty()) {
            item {
                EmptyCard(
                    message = if (state.searchQuery.isNotBlank()) {
                        "No se encontraron cursos que coincidan con \"${state.searchQuery}\"."
                    } else {
                        "Aún no hay cursos publicados en el catálogo."
                    }
                )
            }
        } else {
            items(
                items = state.filteredCourses,
                key = { course -> course.id }
            ) { course ->
                CatalogCourseCard(
                    course = course,
                    onClick = { onCourseClick(course.id) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ActiveCourseCard(
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
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(PrimaryBlue.copy(alpha = 0.08f))
            ) {
                if (!imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = "Portada de ${enrollment.courseTitle}",
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
                            modifier = Modifier.size(44.dp)
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = enrollment.courseTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(12.dp))

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
                        text = "$progressPercentage%",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                LinearProgressIndicator(
                    progress = { progressPercentage / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = PrimaryBlue,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkerBlue),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    Text(
                        text = "Continuar",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun CatalogCourseCard(
    course: Course,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
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
                    .height(140.dp)
                    .background(PrimaryBlue.copy(alpha = 0.08f))
            ) {
                if (!course.imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = course.imageUrl,
                        contentDescription = "Portada de ${course.title}",
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
                            modifier = Modifier.size(44.dp)
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = course.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = course.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onClick,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LightBlueAction,
                            contentColor = PrimaryBlue
                        ),
                        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "Inscribirse",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
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
private fun CourseListPreview() {
    val sampleEnrollments = listOf(
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
    )

    val sampleCourses = listOf(
        Course(
            id = "course-1",
            title = "Desarrollo de Aplicaciones Android",
            description = "Aprende Jetpack Compose, arquitectura y desarrollo moderno en Android.",
            imageUrl = "https://images.unsplash.com/photo-1517694712202-14dd9538aa97?w=600&auto=format&fit=crop&q=80",
            isPublished = true,
            requiredPeerReviews = 2
        ),
        Course(
            id = "course-2",
            title = "Fundamentos de Bases de Datos",
            description = "Domina modelado de datos, SQL relacional y normalización de esquemas.",
            imageUrl = "https://images.unsplash.com/photo-1544383835-bda2bc66a55d?w=600&auto=format&fit=crop&q=80",
            isPublished = true,
            requiredPeerReviews = 1
        ),
        Course(
            id = "course-3",
            title = "Kotlin desde cero",
            description = "Aprende los fundamentos de Kotlin, el lenguaje oficial para el desarrollo en Android. Ideal para principiantes.",
            imageUrl = "https://images.unsplash.com/photo-1555066931-4365d14bab8c?w=600&auto=format&fit=crop&q=80",
            isPublished = true,
            requiredPeerReviews = 2
        ),
        Course(
            id = "course-4",
            title = "SQL para principiantes",
            description = "Domina las consultas y la gestión de datos estructurados. Un curso esencial para cualquier desarrollador.",
            imageUrl = "https://images.unsplash.com/photo-1460925895917-afdab827c52f?w=600&auto=format&fit=crop&q=80",
            isPublished = true,
            requiredPeerReviews = 1
        ),
        Course(
            id = "course-5",
            title = "UI Design Principles",
            description = "Crea interfaces hermosas y funcionales. Aprende sobre color, tipografía, jerarquía visual y accesibilidad.",
            imageUrl = "https://images.unsplash.com/photo-1581291518857-4e27b48ff24e?w=600&auto=format&fit=crop&q=80",
            isPublished = true,
            requiredPeerReviews = 2
        )
    )

    MentorlyTheme {
        CourseListBody(
            state = CourseListUiState(
                studentName = "Adonis",
                userPhotoUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200&auto=format&fit=crop&q=80",
                activeEnrollments = sampleEnrollments,
                filteredEnrollments = sampleEnrollments,
                enrollmentProgressMap = mapOf(
                    "enrollment-1" to 65,
                    "enrollment-2" to 20
                ),
                courses = sampleCourses,
                filteredCourses = sampleCourses
            ),
            onCourseClick = {},
            onEnrollmentClick = {},
            onProfileClick = {},
            onEvent = {}
        )
    }
}
