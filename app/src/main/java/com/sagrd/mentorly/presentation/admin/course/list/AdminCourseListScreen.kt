package com.sagrd.mentorly.presentation.admin.course.list

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.sagrd.mentorly.domain.model.course.Course
import com.sagrd.mentorly.ui.theme.MentorlyTheme

@Composable
fun AdminCourseListScreen(
    onCreateCourseClick: () -> Unit,
    onEditCourseClick: (String) -> Unit,
    onManageContentClick: (String) -> Unit,
    onBackClick: () -> Unit,
    viewModel: AdminCourseListViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    AdminCourseListContent(
        state = state,
        onEvent = viewModel::onEvent,
        onCreateCourseClick = onCreateCourseClick,
        onEditCourseClick = onEditCourseClick,
        onManageContentClick = onManageContentClick,
        onBackClick = onBackClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminCourseListContent(
    state: AdminCourseListUiState,
    onEvent: (AdminCourseListUiEvent) -> Unit,
    onCreateCourseClick: () -> Unit,
    onEditCourseClick: (String) -> Unit,
    onManageContentClick: (String) -> Unit,
    onBackClick: () -> Unit,
) {
    val filteredCourses = state.courses.filter { course ->
        course.title.contains(state.searchQuery, ignoreCase = true) ||
            course.description.contains(state.searchQuery, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de cursos", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { onEvent(AdminCourseListUiEvent.Refresh) },
                        enabled = state.hasAdminAccess && !state.isRefreshing,
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Actualizar cursos")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(),
            )
        },
        floatingActionButton = {
            if (state.hasAdminAccess) {
                FloatingActionButton(onClick = onCreateCourseClick) {
                    Icon(Icons.Default.Add, contentDescription = "Crear curso")
                }
            }
        },
    ) { paddingValues ->
        when {
            !state.hasSession -> CenterMessage(
                message = "No se encontró una sesión activa.",
                modifier = Modifier.padding(paddingValues),
            )
            !state.hasAdminAccess -> CenterMessage(
                message = "No tienes permisos para administrar cursos.",
                modifier = Modifier.padding(paddingValues),
            )
            state.isLoading && state.courses.isEmpty() -> CourseListSkeleton(
                modifier = Modifier.padding(paddingValues),
            )
            state.errorMessage != null && state.courses.isEmpty() -> CenterMessage(
                message = state.errorMessage,
                modifier = Modifier.padding(paddingValues),
                onRetry = { onEvent(AdminCourseListUiEvent.Refresh) },
            )
            else -> CourseList(
                courses = filteredCourses,
                state = state,
                onEvent = onEvent,
                onEditCourseClick = onEditCourseClick,
                onManageContentClick = onManageContentClick,
                onDeleteCourseClick = {
                    onEvent(AdminCourseListUiEvent.DeleteCourse(it.id))
                },
                modifier = Modifier.padding(paddingValues),
            )
        }
    }
}

@Composable
private fun CourseList(
    courses: List<Course>,
    state: AdminCourseListUiState,
    onEvent: (AdminCourseListUiEvent) -> Unit,
    onEditCourseClick: (String) -> Unit,
    onManageContentClick: (String) -> Unit,
    onDeleteCourseClick: (Course) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { onEvent(AdminCourseListUiEvent.SearchQueryChanged(it)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("Buscar cursos...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            )
        }

        if (state.isRefreshing) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                    Text("Actualizando cursos...")
                }
            }
        }

        state.errorMessage?.let { message ->
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(message, modifier = Modifier.weight(1f))
                        TextButton(onClick = { onEvent(AdminCourseListUiEvent.ClearError) }) {
                            Text("Cerrar")
                        }
                    }
                }
            }
        }

        if (courses.isEmpty()) {
            item {
                Text(
                    "No se encontraron cursos.",
                    modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                )
            }
        } else {
            items(courses, key = { it.id }) { course ->
                CourseCard(
                    course = course,
                    isPublishing = state.publishingCourseId == course.id,
                    isDeleting = state.deletingCourseId == course.id,
                    onEditClick = { onEditCourseClick(course.id) },
                    onManageContentClick = { onManageContentClick(course.id) },
                    onTogglePublicationClick = {
                        onEvent(AdminCourseListUiEvent.TogglePublication(course.id))
                    },
                    onDeleteClick = { onDeleteCourseClick(course) },
                )
            }
        }
    }
}

@Composable
private fun CourseCard(
    course: Course,
    isPublishing: Boolean,
    isDeleting: Boolean,
    onEditClick: () -> Unit,
    onManageContentClick: () -> Unit,
    onTogglePublicationClick: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column {
            Box(
                modifier = Modifier.fillMaxWidth().height(156.dp),
                contentAlignment = Alignment.TopStart,
            ) {
                if (course.imageUrl.isNullOrBlank()) {
                    Box(
                        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.secondaryContainer),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Default.MenuBook,
                            contentDescription = null,
                            modifier = Modifier.size(42.dp),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                } else {
                    AsyncImage(
                        model = course.imageUrl,
                        contentDescription = "Imagen de ${course.title}",
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                        contentScale = ContentScale.Crop,
                    )
                }

                SuggestionChip(
                    onClick = {},
                    label = { Text(if (course.isPublished) "Publicado" else "Borrador") },
                    modifier = Modifier.padding(10.dp),
                )
            }

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(course.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    course.description,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.People, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "Requiere ${course.requiredPeerReviews} revisiones por pares",
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(onClick = onEditClick) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Editar")
                    }
                    TextButton(onClick = onManageContentClick) { Text("Contenido") }
                    TextButton(
                        onClick = onTogglePublicationClick,
                        enabled = !isPublishing,
                    ) {
                        Text(if (isPublishing) "Guardando..." else if (course.isPublished) "Despublicar" else "Publicar")
                    }
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = onDeleteClick, enabled = !isDeleting) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Eliminar curso",
                            tint = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CourseListSkeleton(modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item { SkeletonBlock(Modifier.fillMaxWidth().height(56.dp)) }
        items(2) { SkeletonBlock(Modifier.fillMaxWidth().height(310.dp)) }
    }
}

@Composable
private fun SkeletonBlock(modifier: Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {}
}

@Composable
private fun CenterMessage(
    message: String?,
    modifier: Modifier,
    onRetry: (() -> Unit)? = null,
) {
    Box(modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(message.orEmpty())
            onRetry?.let { retry -> Button(onClick = retry) { Text("Reintentar") } }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AdminCoursesPreview() {
    MentorlyTheme {
        AdminCourseListContent(
            state = AdminCourseListUiState(
                courses = listOf(
                    Course("1", "Programación en Python", "Aprende las bases del lenguaje más versátil.", null, true, 3),
                    Course("2", "Diseño de interfaces", "Fundamentos de UX y accesibilidad.", null, false, 2),
                ),
            ),
            onEvent = {},
            onCreateCourseClick = {},
            onEditCourseClick = {},
            onManageContentClick = {},
            onBackClick = {},
        )
    }
}
