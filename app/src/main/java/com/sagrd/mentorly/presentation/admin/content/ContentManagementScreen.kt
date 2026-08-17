package com.sagrd.mentorly.presentation.admin.content

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.sagrd.mentorly.domain.model.content.Activity
import com.sagrd.mentorly.domain.model.content.ActivityType
import com.sagrd.mentorly.domain.model.content.CourseUnit
import com.sagrd.mentorly.domain.model.content.Theme
import com.sagrd.mentorly.domain.model.course.Course
import com.sagrd.mentorly.ui.theme.MentorlyTheme

@Composable
fun ContentManagementScreen(
    courseId: String,
    onBackClick: () -> Unit,
    onCreateUnitClick: (String) -> Unit,
    onEditUnitClick: (String, String) -> Unit,
    onCreateThemeClick: (String) -> Unit,
    onEditThemeClick: (String, String) -> Unit,
    onCreateActivityClick: (String) -> Unit,
    onEditActivityClick: (String, String) -> Unit,
    onManageQuizQuestionsClick: (String) -> Unit,
    viewModel: ContentManagementViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(courseId) {
        viewModel.setCourseId(courseId)
    }

    ContentManagementContent(
        state = state,
        onEvent = viewModel::onEvent,
        onBackClick = onBackClick,
        onCreateUnitClick = onCreateUnitClick,
        onEditUnitClick = onEditUnitClick,
        onCreateThemeClick = onCreateThemeClick,
        onEditThemeClick = onEditThemeClick,
        onCreateActivityClick = onCreateActivityClick,
        onEditActivityClick = onEditActivityClick,
        onManageQuizQuestionsClick = onManageQuizQuestionsClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ContentManagementContent(
    state: ContentManagementUiState,
    onEvent: (ContentManagementUiEvent) -> Unit,
    onBackClick: () -> Unit,
    onCreateUnitClick: (String) -> Unit,
    onEditUnitClick: (String, String) -> Unit,
    onCreateThemeClick: (String) -> Unit,
    onEditThemeClick: (String, String) -> Unit,
    onCreateActivityClick: (String) -> Unit,
    onEditActivityClick: (String, String) -> Unit,
    onManageQuizQuestionsClick: (String) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle del curso") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { onEvent(ContentManagementUiEvent.Refresh) },
                        enabled = state.hasAdminAccess && !state.isRefreshing,
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Actualizar contenido")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(),
            )
        },
    ) { paddingValues ->
        when {
            !state.hasSession -> CenterMessage(
                "No se encontró una sesión activa.",
                Modifier.padding(paddingValues),
            )
            !state.hasAdminAccess -> CenterMessage(
                "No tienes permisos para administrar el contenido del curso.",
                Modifier.padding(paddingValues),
            )
            state.isLoading && state.courseContent == null -> ContentSkeleton(Modifier.padding(paddingValues))
            state.errorMessage != null && state.courseContent == null -> CenterMessage(
                state.errorMessage,
                Modifier.padding(paddingValues),
                onRetry = { onEvent(ContentManagementUiEvent.Refresh) },
            )
            state.courseContent == null -> CenterMessage(
                "No hay contenido disponible para este curso.",
                Modifier.padding(paddingValues),
            )
            else -> CourseContent(
                course = state.courseContent,
                state = state,
                onEvent = onEvent,
                onCreateUnitClick = onCreateUnitClick,
                onEditUnitClick = onEditUnitClick,
                onCreateThemeClick = onCreateThemeClick,
                onEditThemeClick = onEditThemeClick,
                onCreateActivityClick = onCreateActivityClick,
                onEditActivityClick = onEditActivityClick,
                onManageQuizQuestionsClick = onManageQuizQuestionsClick,
                modifier = Modifier.padding(paddingValues),
            )
        }
    }
}

@Composable
private fun CourseContent(
    course: Course,
    state: ContentManagementUiState,
    onEvent: (ContentManagementUiEvent) -> Unit,
    onCreateUnitClick: (String) -> Unit,
    onEditUnitClick: (String, String) -> Unit,
    onCreateThemeClick: (String) -> Unit,
    onEditThemeClick: (String, String) -> Unit,
    onCreateActivityClick: (String) -> Unit,
    onEditActivityClick: (String, String) -> Unit,
    onManageQuizQuestionsClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item { CourseHeader(course) }

        if (state.isRefreshing) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                    Text("Actualizando contenido...")
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
                        TextButton(onClick = { onEvent(ContentManagementUiEvent.ClearError) }) {
                            Text("Cerrar")
                        }
                    }
                }
            }
        }

        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Contenido del curso", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                FilledTonalButton(onClick = { onCreateUnitClick(course.id) }) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Agregar unidad")
                }
            }
        }

        if (course.units.isEmpty()) {
            item { Text("Aún no hay unidades creadas.") }
        } else {
            items(course.units.sortedBy { it.orderIndex }, key = { it.id }) { unit ->
                UnitCard(
                    unit = unit,
                    courseId = course.id,
                    state = state,
                    onEvent = onEvent,
                    onEditUnitClick = onEditUnitClick,
                    onCreateThemeClick = onCreateThemeClick,
                    onEditThemeClick = onEditThemeClick,
                    onCreateActivityClick = onCreateActivityClick,
                    onEditActivityClick = onEditActivityClick,
                    onManageQuizQuestionsClick = onManageQuizQuestionsClick,
                )
            }
        }
    }
}

@Composable
private fun CourseHeader(course: Course) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Card(shape = RoundedCornerShape(16.dp)) {
            if (course.imageUrl.isNullOrBlank()) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(170.dp).background(MaterialTheme.colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Default.MenuBook,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            } else {
                AsyncImage(
                    model = course.imageUrl,
                    contentDescription = "Imagen de ${course.title}",
                    modifier = Modifier.fillMaxWidth().height(170.dp).clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop,
                )
            }
        }
        SuggestionChip(onClick = {}, label = { Text(if (course.isPublished) "Publicado" else "Borrador") })
        Text(course.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(course.description, style = MaterialTheme.typography.bodySmall)
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Default.Groups, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text(
                    "Requiere ${course.requiredPeerReviews} revisiones por pares",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
private fun UnitCard(
    unit: CourseUnit,
    courseId: String,
    state: ContentManagementUiState,
    onEvent: (ContentManagementUiEvent) -> Unit,
    onEditUnitClick: (String, String) -> Unit,
    onCreateThemeClick: (String) -> Unit,
    onEditThemeClick: (String, String) -> Unit,
    onCreateActivityClick: (String) -> Unit,
    onEditActivityClick: (String, String) -> Unit,
    onManageQuizQuestionsClick: (String) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(26.dp).background(MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        (unit.orderIndex + 1).toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(unit.title, modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                IconButton(onClick = { onEditUnitClick(courseId, unit.id) }) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar unidad")
                }
                IconButton(
                    onClick = { onEvent(ContentManagementUiEvent.DeleteUnit(unit.id)) },
                    enabled = state.deletingItemId == null,
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Eliminar unidad",
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
            }

            unit.themes.sortedBy { it.orderIndex }.forEach { theme ->
                ThemeContent(
                    theme = theme,
                    state = state,
                    onEvent = onEvent,
                    onEditThemeClick = onEditThemeClick,
                    onCreateActivityClick = onCreateActivityClick,
                    onEditActivityClick = onEditActivityClick,
                    onManageQuizQuestionsClick = onManageQuizQuestionsClick,
                )
            }

            TextButton(onClick = { onCreateThemeClick(unit.id) }) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Text("Agregar tema")
            }
        }
    }
}

@Composable
private fun ThemeContent(
    theme: Theme,
    state: ContentManagementUiState,
    onEvent: (ContentManagementUiEvent) -> Unit,
    onEditThemeClick: (String, String) -> Unit,
    onCreateActivityClick: (String) -> Unit,
    onEditActivityClick: (String, String) -> Unit,
    onManageQuizQuestionsClick: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("• ${theme.title}", modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
            IconButton(onClick = { onEditThemeClick(theme.unitId, theme.id) }) {
                Icon(Icons.Default.Edit, contentDescription = "Editar tema", modifier = Modifier.size(18.dp))
            }
            IconButton(
                onClick = { onEvent(ContentManagementUiEvent.DeleteTheme(theme.id)) },
                enabled = state.deletingItemId == null,
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Eliminar tema",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        }
        if (theme.contentText.isNotBlank()) {
            Text(
                theme.contentText,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        theme.activities.sortedBy { it.orderIndex }.forEach { activity ->
            ActivityContent(
                activity = activity,
                state = state,
                onEvent = onEvent,
                onEditActivityClick = onEditActivityClick,
                onManageQuizQuestionsClick = onManageQuizQuestionsClick,
            )
        }
        TextButton(onClick = { onCreateActivityClick(theme.id) }) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
            Text("Agregar actividad")
        }
    }
}

@Composable
private fun ActivityContent(
    activity: Activity,
    state: ContentManagementUiState,
    onEvent: (ContentManagementUiEvent) -> Unit,
    onEditActivityClick: (String, String) -> Unit,
    onManageQuizQuestionsClick: (String) -> Unit,
) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                if (activity.type == ActivityType.QUIZ) Icons.Default.Quiz else Icons.Default.MenuBook,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(activity.title, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                Text(
                    if (activity.type == ActivityType.QUIZ) "Quiz" else "Actividad",
                    style = MaterialTheme.typography.labelSmall,
                )
            }
            IconButton(onClick = { onEditActivityClick(activity.themeId, activity.id) }) {
                Icon(Icons.Default.Edit, contentDescription = "Editar actividad", modifier = Modifier.size(18.dp))
            }
            IconButton(
                onClick = { onEvent(ContentManagementUiEvent.DeleteActivity(activity.id)) },
                enabled = state.deletingItemId == null,
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Eliminar actividad",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        }
        if (activity.type == ActivityType.QUIZ) {
            TextButton(onClick = { onManageQuizQuestionsClick(activity.id) }) {
                Text("Administrar preguntas")
            }
        }
    }
}

@Composable
private fun ContentSkeleton(modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item { SkeletonBlock(Modifier.fillMaxWidth().height(210.dp)) }
        items(2) { SkeletonBlock(Modifier.fillMaxWidth().height(260.dp)) }
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
    message: String,
    modifier: Modifier,
    onRetry: (() -> Unit)? = null,
) {
    Box(modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(message)
            onRetry?.let { retry -> Button(onClick = retry) { Text("Reintentar") } }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ContentManagementPreview() {
    MentorlyTheme {
        ContentManagementContent(
            state = ContentManagementUiState(
                courseContent = Course(
                    id = "course-1",
                    title = "Programación en Python",
                    description = "Un curso introductorio para aprender fundamentos de programación.",
                    imageUrl = null,
                    isPublished = true,
                    requiredPeerReviews = 3,
                    units = emptyList(),
                ),
            ),
            onEvent = {},
            onBackClick = {},
            onCreateUnitClick = {},
            onEditUnitClick = { _, _ -> },
            onCreateThemeClick = {},
            onEditThemeClick = { _, _ -> },
            onCreateActivityClick = {},
            onEditActivityClick = { _, _ -> },
            onManageQuizQuestionsClick = {},
        )
    }
}
