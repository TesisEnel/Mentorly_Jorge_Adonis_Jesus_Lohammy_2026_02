package com.sagrd.mentorly.presentation.admin.peerreview.rubric

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sagrd.mentorly.domain.model.peerreview.PeerReviewRubricCriterion
import com.sagrd.mentorly.ui.theme.MentorlyTheme

@Composable
fun AdminPeerReviewRubricScreen(
    activityId: String,
    onBackClick: () -> Unit,
    onCriterionCreated: () -> Unit,
    viewModel: AdminPeerReviewRubricViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(activityId) {
        viewModel.onEvent(AdminPeerReviewRubricUiEvent.Load(activityId))
    }
    LaunchedEffect(state.isCriterionSaved) {
        if (state.isCriterionSaved) {
            viewModel.onEvent(AdminPeerReviewRubricUiEvent.CriterionSavedHandled)
            onCriterionCreated()
        }
    }

    AdminPeerReviewRubricContent(
        state = state,
        onEvent = viewModel::onEvent,
        onBackClick = onBackClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminPeerReviewRubricContent(
    state: AdminPeerReviewRubricUiState,
    onEvent: (AdminPeerReviewRubricUiEvent) -> Unit,
    onBackClick: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Administrar rúbrica") },
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
                "No tienes permisos para administrar rúbricas.",
                Modifier.padding(paddingValues),
            )
            state.isLoading && state.criteria.isEmpty() -> LoadingContent(Modifier.padding(paddingValues))
            else -> RubricBody(
                state = state,
                onEvent = onEvent,
                onBackClick = onBackClick,
                modifier = Modifier.padding(paddingValues),
            )
        }
    }
}

@Composable
private fun RubricBody(
    state: AdminPeerReviewRubricUiState,
    onEvent: (AdminPeerReviewRubricUiEvent) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = if (state.editingCriterionId == null) "Agregar criterio a la rúbrica" else "Editar criterio",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )

                Text(
                    text = "Los revisores evaluarán la entrega asignando un puntaje a cada criterio.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                OutlinedTextField(
                    value = state.title,
                    onValueChange = { onEvent(AdminPeerReviewRubricUiEvent.TitleChanged(it)) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Título del criterio") },
                    placeholder = { Text("Ej: Complejidad Técnica, Calidad de Código...") },
                    isError = state.titleError != null,
                    supportingText = {
                        state.titleError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    },
                    enabled = !state.isSaving && !state.isDeleting,
                    singleLine = true,
                )

                OutlinedTextField(
                    value = state.description,
                    onValueChange = { onEvent(AdminPeerReviewRubricUiEvent.DescriptionChanged(it)) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Descripción / Pautas de evaluación") },
                    placeholder = { Text("Describe qué aspectos debe evaluar el revisor...") },
                    isError = state.descriptionError != null,
                    supportingText = {
                        state.descriptionError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    },
                    enabled = !state.isSaving && !state.isDeleting,
                    minLines = 3,
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedTextField(
                        value = state.maxScore,
                        onValueChange = { onEvent(AdminPeerReviewRubricUiEvent.MaxScoreChanged(it)) },
                        modifier = Modifier.weight(1f),
                        label = { Text("Puntaje máximo") },
                        isError = state.maxScoreError != null,
                        supportingText = {
                            state.maxScoreError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                        },
                        enabled = !state.isSaving && !state.isDeleting,
                        singleLine = true,
                    )

                    OutlinedTextField(
                        value = state.orderIndex,
                        onValueChange = { onEvent(AdminPeerReviewRubricUiEvent.OrderChanged(it)) },
                        modifier = Modifier.weight(1f),
                        label = { Text("Posición") },
                        isError = state.orderError != null,
                        supportingText = {
                            state.orderError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                        },
                        enabled = !state.isSaving && !state.isDeleting,
                        singleLine = true,
                    )
                }

                state.errorMessage?.let { message ->
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                        Column(Modifier.padding(12.dp)) {
                            Text(message, color = MaterialTheme.colorScheme.onErrorContainer)
                            TextButton(onClick = { onEvent(AdminPeerReviewRubricUiEvent.ClearError) }) {
                                Text("Cerrar")
                            }
                        }
                    }
                }

                Button(
                    onClick = { onEvent(AdminPeerReviewRubricUiEvent.SaveCriterion) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isSaving && !state.isDeleting,
                ) {
                    if (state.isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Text(if (state.editingCriterionId == null) "Agregar criterio" else "Guardar cambios")
                    }
                }

                if (state.editingCriterionId != null) {
                    OutlinedButton(
                        onClick = { onEvent(AdminPeerReviewRubricUiEvent.CancelEdit) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !state.isSaving && !state.isDeleting,
                    ) {
                        Text("Cancelar edición")
                    }

                    OutlinedButton(
                        onClick = { onEvent(AdminPeerReviewRubricUiEvent.DeleteCriterion) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !state.isSaving && !state.isDeleting,
                    ) {
                        if (state.isDeleting) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Spacer(Modifier.width(6.dp))
                            Text("Eliminar criterio", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }

        // Section: List of Existing Criteria
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                Icons.AutoMirrored.Filled.FactCheck,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
            )
            Text(
                text = "Criterios registrados (${state.criteria.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        }

        if (state.criteria.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "No hay criterios registrados en la rúbrica.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                state.criteria.forEach { criterion ->
                    CriterionItemCard(
                        criterion = criterion,
                        isEditing = state.editingCriterionId == criterion.id,
                        onEditClick = { onEvent(AdminPeerReviewRubricUiEvent.EditCriterion(criterion.id)) },
                    )
                }
            }
        }
    }
}

@Composable
private fun CriterionItemCard(
    criterion: PeerReviewRubricCriterion,
    isEditing: Boolean,
    onEditClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isEditing) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else MaterialTheme.colorScheme.surface,
        ),
        border = BorderStroke(
            width = if (isEditing) 1.5.dp else 1.dp,
            color = if (isEditing) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
        ),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = criterion.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFE0F2FE),
                    ) {
                        Text(
                            text = "Máx: ${criterion.maxScore} pts",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0369A1),
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        )
                    }
                }

                if (criterion.description.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = criterion.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            IconButton(onClick = onEditClick) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Editar criterio",
                    tint = MaterialTheme.colorScheme.primary,
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
private fun AdminPeerReviewRubricPreview() {
    MentorlyTheme {
        AdminPeerReviewRubricContent(
            state = AdminPeerReviewRubricUiState(
                criteria = listOf(
                    PeerReviewRubricCriterion(
                        id = "crit-1",
                        activityId = "act-1",
                        title = "Complejidad Técnica",
                        description = "Evalúa la eficiencia, arquitectura y uso de estructuras de datos adecuadas.",
                        maxScore = 5,
                        orderIndex = 1,
                    ),
                    PeerReviewRubricCriterion(
                        id = "crit-2",
                        activityId = "act-1",
                        title = "Calidad del Código",
                        description = "Evalúa la legibilidad, modularidad y seguimiento de convenciones.",
                        maxScore = 5,
                        orderIndex = 2,
                    ),
                ),
            ),
            onEvent = {},
            onBackClick = {},
        )
    }
}
