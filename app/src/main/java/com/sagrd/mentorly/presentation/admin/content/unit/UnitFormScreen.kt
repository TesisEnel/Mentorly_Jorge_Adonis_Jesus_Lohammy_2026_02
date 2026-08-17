package com.sagrd.mentorly.presentation.admin.content.unit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sagrd.mentorly.ui.theme.MentorlyTheme

@Composable
fun UnitFormScreen(
    courseId: String,
    unitId: String? = null,
    onBackClick: () -> Unit,
    onSaved: () -> Unit,
    viewModel: UnitFormViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(courseId, unitId) {
        viewModel.onEvent(UnitFormUiEvent.Load(courseId, unitId))
    }
    LaunchedEffect(state.isSaved) {
        if (state.isSaved) {
            viewModel.onEvent(UnitFormUiEvent.SavedHandled)
            onSaved()
        }
    }
    LaunchedEffect(state.isDeleted) {
        if (state.isDeleted) {
            viewModel.onEvent(UnitFormUiEvent.DeletedHandled)
            onSaved()
        }
    }

    UnitFormContent(
        state = state,
        onEvent = viewModel::onEvent,
        onBackClick = onBackClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UnitFormContent(
    state: UnitFormUiState,
    onEvent: (UnitFormUiEvent) -> Unit,
    onBackClick: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.isEditMode) "Editar unidad" else "Crear unidad") },
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
            else -> UnitForm(
                state = state,
                onEvent = onEvent,
                onBackClick = onBackClick,
                modifier = Modifier.padding(paddingValues),
            )
        }
    }
}

@Composable
private fun UnitForm(
    state: UnitFormUiState,
    onEvent: (UnitFormUiEvent) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            if (state.isEditMode) "Modifica los detalles de la unidad de estudio actual." else "Agrega una nueva unidad al contenido del curso.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        OutlinedTextField(
            value = state.title,
            onValueChange = { onEvent(UnitFormUiEvent.TitleChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Título de la unidad") },
            isError = state.fieldErrors.containsKey("title"),
            supportingText = {
                state.fieldErrors["title"]?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            },
            enabled = !state.isSaving && !state.isDeleting,
            singleLine = true,
        )

        OutlinedTextField(
            value = state.orderIndex,
            onValueChange = { onEvent(UnitFormUiEvent.OrderChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Posición en el curso") },
            isError = state.fieldErrors.containsKey("order"),
            supportingText = {
                state.fieldErrors["order"]?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            },
            enabled = !state.isSaving && !state.isDeleting,
            singleLine = true,
        )

        Text(
            "Define el orden en que aparecerá dentro del curso.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        state.errorMessage?.let { message ->
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                Column(Modifier.padding(12.dp)) {
                    Text(message, color = MaterialTheme.colorScheme.onErrorContainer)
                    TextButton(onClick = { onEvent(UnitFormUiEvent.ClearError) }) {
                        Text("Cerrar")
                    }
                }
            }
        }

        Button(
            onClick = { onEvent(UnitFormUiEvent.Save) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isSaving && !state.isDeleting,
        ) {
            if (state.isSaving) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Text(if (state.isEditMode) "Guardar cambios" else "Crear unidad")
            }
        }

        OutlinedButton(
            onClick = onBackClick,
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isSaving && !state.isDeleting,
        ) {
            Text("Cancelar")
        }

        if (state.isEditMode) {
            OutlinedButton(
                onClick = { onEvent(UnitFormUiEvent.DeleteUnit) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isSaving && !state.isDeleting,
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.error,
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    if (state.isDeleting) "Eliminando..." else "Eliminar unidad",
                    color = MaterialTheme.colorScheme.error,
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
private fun UnitFormPreview() {
    MentorlyTheme {
        UnitFormContent(
            state = UnitFormUiState(
                isEditMode = true,
                title = "Fundamentos de Python",
                orderIndex = "1",
            ),
            onEvent = {},
            onBackClick = {},
        )
    }
}
