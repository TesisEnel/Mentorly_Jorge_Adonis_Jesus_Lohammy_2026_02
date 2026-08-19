package com.sagrd.mentorly.presentation.admin.content.activity

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sagrd.mentorly.domain.model.content.ActivityType
import com.sagrd.mentorly.domain.model.content.ApprovalStrategy
import com.sagrd.mentorly.ui.theme.MentorlyTheme

@Composable
fun ActivityFormScreen(
    themeId: String,
    activityId: String? = null,
    onBackClick: () -> Unit,
    onSaved: () -> Unit,
    viewModel: ActivityFormViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(themeId, activityId) {
        viewModel.onEvent(ActivityFormUiEvent.Load(themeId, activityId))
    }
    LaunchedEffect(state.isSaved) {
        if (state.isSaved) {
            viewModel.onEvent(ActivityFormUiEvent.SavedHandled)
            onSaved()
        }
    }
    LaunchedEffect(state.isDeleted) {
        if (state.isDeleted) {
            viewModel.onEvent(ActivityFormUiEvent.DeletedHandled)
            onSaved()
        }
    }

    ActivityFormContent(
        state = state,
        onEvent = viewModel::onEvent,
        onBackClick = onBackClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActivityFormContent(
    state: ActivityFormUiState,
    onEvent: (ActivityFormUiEvent) -> Unit,
    onBackClick: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.isEditMode) "Editar actividad" else "Crear actividad") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
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
            state.isLoading -> LoadingContent(Modifier.padding(paddingValues))
            else -> ActivityForm(
                state = state,
                onEvent = onEvent,
                onBackClick = onBackClick,
                modifier = Modifier.padding(paddingValues),
            )
        }
    }
}

@Composable
private fun ActivityForm(
    state: ActivityFormUiState,
    onEvent: (ActivityFormUiEvent) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val inputsEnabled = !state.isSaving && !state.isDeleting

    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            if (state.isEditMode) {
                "Modifica los parámetros de evaluación y entrega de esta actividad."
            } else {
                "Agrega una actividad al contenido del tema."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        OutlinedTextField(
            value = state.title,
            onValueChange = { onEvent(ActivityFormUiEvent.TitleChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Título de la actividad") },
            isError = state.fieldErrors.containsKey("title"),
            supportingText = {
                state.fieldErrors["title"]?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            },
            enabled = inputsEnabled,
            singleLine = true,
        )

        OutlinedTextField(
            value = state.description,
            onValueChange = { onEvent(ActivityFormUiEvent.DescriptionChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Descripción de la actividad") },
            placeholder = { Text("Instrucciones, objetivos y detalles de entrega...") },
            isError = state.fieldErrors.containsKey("description"),
            supportingText = {
                state.fieldErrors["description"]?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            },
            enabled = inputsEnabled,
            minLines = 3,
            maxLines = 6,
        )

        Text("Tipo de actividad", style = MaterialTheme.typography.titleSmall)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ActivityTypeOption(
                label = "Ejercicio",
                icon = Icons.Default.Assignment,
                selected = state.type == ActivityType.EXERCISE,
                enabled = inputsEnabled,
                onClick = { onEvent(ActivityFormUiEvent.TypeChanged(ActivityType.EXERCISE)) },
                modifier = Modifier.weight(1f),
            )
            ActivityTypeOption(
                label = "Quiz",
                icon = Icons.Default.School,
                selected = state.type == ActivityType.QUIZ,
                enabled = inputsEnabled,
                onClick = { onEvent(ActivityFormUiEvent.TypeChanged(ActivityType.QUIZ)) },
                modifier = Modifier.weight(1f),
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text("¿Es obligatoria?", style = MaterialTheme.typography.titleSmall)
                Text(
                    "Requerida para avanzar al siguiente módulo.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(
                checked = state.isMandatory,
                onCheckedChange = { onEvent(ActivityFormUiEvent.MandatoryChanged(it)) },
                enabled = inputsEnabled,
            )
        }

        OutlinedTextField(
            value = state.orderIndex,
            onValueChange = { onEvent(ActivityFormUiEvent.OrderChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Posición en el tema") },
            isError = state.fieldErrors.containsKey("order"),
            supportingText = {
                state.fieldErrors["order"]?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            },
            enabled = inputsEnabled,
            singleLine = true,
        )

        EvaluationStrategySection(
            type = state.type,
            selectedStrategy = state.approvalStrategy,
            enabled = inputsEnabled,
            onStrategySelected = { onEvent(ActivityFormUiEvent.StrategyChanged(it)) },
        )

        state.errorMessage?.let { message ->
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                Column(Modifier.padding(12.dp)) {
                    Text(message, color = MaterialTheme.colorScheme.onErrorContainer)
                    TextButton(onClick = { onEvent(ActivityFormUiEvent.ClearError) }) {
                        Text("Cerrar")
                    }
                }
            }
        }

        Button(
            onClick = { onEvent(ActivityFormUiEvent.Save) },
            modifier = Modifier.fillMaxWidth(),
            enabled = inputsEnabled,
        ) {
            if (state.isSaving) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Text(if (state.isEditMode) "Guardar cambios" else "Crear actividad")
            }
        }

        OutlinedButton(
            onClick = onBackClick,
            modifier = Modifier.fillMaxWidth(),
            enabled = inputsEnabled,
        ) {
            Text("Cancelar")
        }

        if (state.isEditMode) {
            OutlinedButton(
                onClick = { onEvent(ActivityFormUiEvent.DeleteActivity) },
                modifier = Modifier.fillMaxWidth(),
                enabled = inputsEnabled,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.error,
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    if (state.isDeleting) "Eliminando..." else "Eliminar actividad",
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun ActivityTypeOption(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.selectable(selected = selected, enabled = enabled, role = Role.RadioButton, onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        ),
        border = BorderStroke(
            1.dp,
            if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
        ),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(icon, contentDescription = null)
            Text(label, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun EvaluationStrategySection(
    type: ActivityType,
    selectedStrategy: ApprovalStrategy,
    enabled: Boolean,
    onStrategySelected: (ApprovalStrategy) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Estrategia de evaluación", style = MaterialTheme.typography.titleSmall)

        if (type == ActivityType.QUIZ) {
            Text(
                "Los quizzes se califican automáticamente.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            StrategyOption(
                title = "Automática",
                description = "El sistema evalúa las respuestas correctas.",
                selected = true,
                enabled = false,
                onClick = {},
            )
        } else {
            StrategyOption(
                title = "Automática",
                description = "El sistema valida la entrega automáticamente.",
                selected = selectedStrategy == ApprovalStrategy.AUTO,
                enabled = enabled,
                onClick = { onStrategySelected(ApprovalStrategy.AUTO) },
            )
            StrategyOption(
                title = "Por pares",
                description = "Los estudiantes evalúan la entrega mediante una rúbrica.",
                selected = selectedStrategy == ApprovalStrategy.PEER_REVIEW,
                enabled = enabled,
                onClick = { onStrategySelected(ApprovalStrategy.PEER_REVIEW) },
            )
            StrategyOption(
                title = "Administrativa",
                description = "Un administrador califica la entrega manualmente.",
                selected = selectedStrategy == ApprovalStrategy.ADMIN,
                enabled = enabled,
                onClick = { onStrategySelected(ApprovalStrategy.ADMIN) },
            )
        }
    }
}

@Composable
private fun StrategyOption(
    title: String,
    description: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth().selectable(
            selected = selected,
            enabled = enabled,
            role = Role.RadioButton,
            onClick = onClick,
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RadioButton(selected = selected, onClick = null, enabled = enabled)
            Spacer(Modifier.width(8.dp))
            Column {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
                Text(
                    description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun LoadingContent(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun CenterMessage(message: String, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Text(message)
    }
}

@Preview(showBackground = true)
@Composable
private fun ActivityFormPreview() {
    MentorlyTheme {
        ActivityFormContent(
            state = ActivityFormUiState(
                isEditMode = true,
                title = "Análisis de algoritmos avanzados",
                type = ActivityType.EXERCISE,
                approvalStrategy = ApprovalStrategy.PEER_REVIEW,
                orderIndex = "3",
            ),
            onEvent = {},
            onBackClick = {},
        )
    }
}
