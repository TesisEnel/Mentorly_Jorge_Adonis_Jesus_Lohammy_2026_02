package com.sagrd.mentorly.presentation.admin.content

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
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
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.sagrd.mentorly.domain.model.content.Activity
import com.sagrd.mentorly.domain.model.content.ActivityType
import com.sagrd.mentorly.domain.model.content.ApprovalStrategy
import com.sagrd.mentorly.domain.model.content.CourseUnit
import com.sagrd.mentorly.domain.model.content.Theme
import androidx.compose.material.icons.automirrored.filled.FactCheck
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
    onManageRubricClick: (String) -> Unit,
    viewModel: ContentManagementViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LifecycleResumeEffect(courseId) {
        viewModel.setCourseId(courseId)
        viewModel.onEvent(ContentManagementUiEvent.Refresh)
        onPauseOrDispose { }
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
        onManageRubricClick = onManageRubricClick,
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
    onManageRubricClick: (String) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle del curso") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
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
                onManageRubricClick = onManageRubricClick,
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
    onManageRubricClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "Contenido del curso",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                FilledTonalButton(
                    onClick = { onCreateUnitClick(course.id) },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.height(36.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "Agregar unidad",
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1,
                    )
                }
            }
        }

        if (course.units.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                ) {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Aún no hay unidades creadas en este curso.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
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
                    onManageRubricClick = onManageRubricClick,
                )
            }
        }
    }
}

@Composable
private fun CourseHeader(course: Course) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Card(
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            if (course.imageUrl.isNullOrBlank()) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(170.dp).background(MaterialTheme.colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.AutoMirrored.Outlined.MenuBook,
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
        if (course.description.isNotBlank()) {
            Text(course.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
        ) {
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
    onManageRubricClick: (String) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Unit Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primary,
                ) {
                    Text(
                        text = "U${unit.orderIndex + 1}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    )
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    text = unit.title,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                IconButton(onClick = { onEditUnitClick(courseId, unit.id) }) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Editar unidad",
                        tint = MaterialTheme.colorScheme.primary,
                    )
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

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                modifier = Modifier.padding(vertical = 2.dp),
            )

            // Themes List inside Unit
            if (unit.themes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Esta unidad no tiene temas registrados.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    )
                }
            } else {
                unit.themes.sortedBy { it.orderIndex }.forEach { theme ->
                    ThemeContainer(
                        theme = theme,
                        state = state,
                        onEvent = onEvent,
                        onEditThemeClick = onEditThemeClick,
                        onCreateActivityClick = onCreateActivityClick,
                        onEditActivityClick = onEditActivityClick,
                        onManageQuizQuestionsClick = onManageQuizQuestionsClick,
                        onManageRubricClick = onManageRubricClick,
                    )
                }
            }

            // Button to add theme
            FilledTonalButton(
                onClick = { onCreateThemeClick(unit.id) },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Agregar tema a esta unidad")
            }
        }
    }
}

@Composable
private fun ThemeContainer(
    theme: Theme,
    state: ContentManagementUiState,
    onEvent: (ContentManagementUiEvent) -> Unit,
    onEditThemeClick: (String, String) -> Unit,
    onCreateActivityClick: (String) -> Unit,
    onEditActivityClick: (String, String) -> Unit,
    onManageQuizQuestionsClick: (String) -> Unit,
    onManageRubricClick: (String) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // Theme Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                ) {
                    Text(
                        text = "TEMA ${theme.orderIndex + 1}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = theme.title,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
                IconButton(
                    onClick = { onEditThemeClick(theme.unitId, theme.id) },
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Editar tema",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
                IconButton(
                    onClick = { onEvent(ContentManagementUiEvent.DeleteTheme(theme.id)) },
                    enabled = state.deletingItemId == null,
                    modifier = Modifier.size(32.dp),
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
                    text = theme.contentText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            // Activities List inside Theme
            if (theme.activities.isEmpty()) {
                Text(
                    text = "Sin actividades registradas.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.padding(vertical = 2.dp),
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    theme.activities.sortedBy { it.orderIndex }.forEach { activity ->
                        ActivityCard(
                            activity = activity,
                            state = state,
                            onEvent = onEvent,
                            onEditActivityClick = onEditActivityClick,
                            onManageQuizQuestionsClick = onManageQuizQuestionsClick,
                            onManageRubricClick = onManageRubricClick,
                        )
                    }
                }
            }

            // Button to add activity
            OutlinedButton(
                onClick = { onCreateActivityClick(theme.id) },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Agregar actividad")
            }
        }
    }
}

@Composable
private fun ActivityCard(
    activity: Activity,
    state: ContentManagementUiState,
    onEvent: (ContentManagementUiEvent) -> Unit,
    onEditActivityClick: (String, String) -> Unit,
    onManageQuizQuestionsClick: (String) -> Unit,
    onManageRubricClick: (String) -> Unit,
) {
    val isQuiz = activity.type == ActivityType.QUIZ
    val iconBgColor = if (isQuiz) Color(0xFFF3E8FF) else Color(0xFFE3F2FD)
    val iconTintColor = if (isQuiz) Color(0xFF7E22CE) else Color(0xFF1565C0)
    val labelText = if (isQuiz) "Cuestionario" else "Actividad"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(iconBgColor),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = if (isQuiz) Icons.Outlined.Quiz else Icons.AutoMirrored.Outlined.MenuBook,
                        contentDescription = null,
                        tint = iconTintColor,
                        modifier = Modifier.size(18.dp),
                    )
                }

                Spacer(Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = activity.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(2.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = iconBgColor,
                    ) {
                        Text(
                            text = labelText,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = iconTintColor,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        )
                    }
                }

                IconButton(
                    onClick = { onEditActivityClick(activity.themeId, activity.id) },
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Editar actividad",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }

                IconButton(
                    onClick = { onEvent(ContentManagementUiEvent.DeleteActivity(activity.id)) },
                    enabled = state.deletingItemId == null,
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Eliminar actividad",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
            }

            if (isQuiz) {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f),
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
                )
                TextButton(
                    onClick = { onManageQuizQuestionsClick(activity.id) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(
                        Icons.AutoMirrored.Outlined.HelpOutline,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("Administrar preguntas")
                }
            } else if (activity.approvalStrategy == ApprovalStrategy.PEER_REVIEW) {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f),
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
                )
                TextButton(
                    onClick = { onManageRubricClick(activity.id) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.FactCheck,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("Administrar rúbrica")
                }
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

@Preview(name = "Gestión de Contenido - Con Datos", showBackground = true, showSystemUi = true)
@Composable
private fun ContentManagementPreview() {
    val sampleActivitiesTheme1 = listOf(
        Activity(
            id = "act-1",
            themeId = "theme-1",
            title = "Ejercicio: Funciones y Lambdas",
            type = ActivityType.EXERCISE,
            isMandatory = true,
            approvalStrategy = ApprovalStrategy.PEER_REVIEW,
            orderIndex = 1,
        ),
        Activity(
            id = "act-2",
            themeId = "theme-1",
            title = "Cuestionario: Conceptos Básicos de Kotlin",
            type = ActivityType.QUIZ,
            isMandatory = false,
            approvalStrategy = ApprovalStrategy.AUTO,
            orderIndex = 2,
        ),
    )

    val sampleActivitiesTheme2 = listOf(
        Activity(
            id = "act-3",
            themeId = "theme-2",
            title = "Ejercicio: Interfaz de Usuario con Modifiers",
            type = ActivityType.EXERCISE,
            isMandatory = true,
            approvalStrategy = ApprovalStrategy.ADMIN,
            orderIndex = 1,
        ),
    )

    val sampleThemesUnit1 = listOf(
        Theme(
            id = "theme-1",
            unitId = "unit-1",
            title = "Sintaxis Básica y Programación Funcional",
            contentText = "Introducción a Kotlin, variables, inmutabilidad y funciones de orden superior.",
            orderIndex = 1,
            activities = sampleActivitiesTheme1,
        ),
        Theme(
            id = "theme-2",
            unitId = "unit-1",
            title = "Layouts Básicos en Jetpack Compose",
            contentText = "Uso de Box, Column, Row y Modifiers para diseñar pantallas responsivas.",
            orderIndex = 2,
            activities = sampleActivitiesTheme2,
        ),
    )

    val sampleThemesUnit2 = listOf(
        Theme(
            id = "theme-3",
            unitId = "unit-2",
            title = "Arquitectura MVI y StateFlow",
            contentText = "Manejo de estado inmutable y eventos unidireccionales en Compose.",
            orderIndex = 1,
            activities = emptyList(),
        ),
    )

    val sampleUnits = listOf(
        CourseUnit(
            id = "unit-1",
            courseId = "course-1",
            title = "Unidad 1: Fundamentos de Android Moderno",
            orderIndex = 1,
            themes = sampleThemesUnit1,
        ),
        CourseUnit(
            id = "unit-2",
            courseId = "course-1",
            title = "Unidad 2: Arquitectura y Estado",
            orderIndex = 2,
            themes = sampleThemesUnit2,
        ),
    )

    val sampleCourse = Course(
        id = "course-1",
        title = "Desarrollo de Aplicaciones Android",
        description = "Aprende a construir aplicaciones profesionales con Jetpack Compose, Kotlin y Clean Architecture.",
        imageUrl = "https://images.unsplash.com/photo-1517694712202-14dd9538aa97?w=600&auto=format&fit=crop&q=80",
        isPublished = true,
        requiredPeerReviews = 3,
        units = sampleUnits,
    )

    MentorlyTheme {
        ContentManagementContent(
            state = ContentManagementUiState(
                isLoading = false,
                hasSession = true,
                hasAdminAccess = true,
                courseContent = sampleCourse,
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
            onManageRubricClick = {},
        )
    }
}
