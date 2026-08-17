package com.sagrd.mentorly.presentation.admin.content.theme

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
fun ThemeFormScreen(
    unitId: String,
    themeId: String? = null,
    onBackClick: () -> Unit,
    onSaved: () -> Unit,
    viewModel: ThemeFormViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(unitId, themeId) {
        viewModel.onEvent(ThemeFormUiEvent.Load(unitId, themeId))
    }
    LaunchedEffect(state.isSaved) {
        if (state.isSaved) {
            viewModel.onEvent(ThemeFormUiEvent.SavedHandled)
            onSaved()
        }
    }
    LaunchedEffect(state.isDeleted) {
        if (state.isDeleted) {
            viewModel.onEvent(ThemeFormUiEvent.DeletedHandled)
            onSaved()
        }
    }

    ThemeFormContent(
        state = state,
        onEvent = viewModel::onEvent,
        onBackClick = onBackClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThemeFormContent(
    state: ThemeFormUiState,
    onEvent: (ThemeFormUiEvent) -> Unit,
    onBackClick: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.isEditMode) "Editar tema" else "Crear tema") },
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
            else -> ThemeForm(
                state = state,
                onEvent = onEvent,
                onBackClick = onBackClick,
                modifier = Modifier.padding(paddingValues),
            )
        }
    }
}

@Composable
private fun ThemeForm(
    state: ThemeFormUiState,
    onEvent: (ThemeFormUiEvent) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            if (state.isEditMode) "Modifica los detalles del tema de estudio actual." else "Agrega un nuevo tema al contenido de la unidad.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        OutlinedTextField(
            value = state.title,
            onValueChange = { onEvent(ThemeFormUiEvent.TitleChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Título del tema") },
            isError = state.fieldErrors.containsKey("title"),
            supportingText = {
                state.fieldErrors["title"]?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            },
            enabled = !state.isSaving && !state.isDeleting,
        )

        OutlinedTextField(
            value = state.contentText,
            onValueChange = { onEvent(ThemeFormUiEvent.ContentChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Contenido / Descripción") },
            isError = state.fieldErrors.containsKey("content"),
            supportingText = {
                state.fieldErrors["content"]?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            },
            enabled = !state.isSaving && !state.isDeleting,
            minLines = 5,
        )

        Text(
            "Escribe el contenido teórico que el estudiante leerá.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        OutlinedTextField(
            value = state.orderIndex,
            onValueChange = { onEvent(ThemeFormUiEvent.OrderChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Posición dentro de la unidad") },
            isError = state.fieldErrors.containsKey("order"),
            supportingText = {
                state.fieldErrors["order"]?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            },
            enabled = !state.isSaving && !state.isDeleting,
            singleLine = true,
        )

        state.errorMessage?.let { message ->
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                Column(Modifier.padding(12.dp)) {
                    Text(message, color = MaterialTheme.colorScheme.onErrorContainer)
                    TextButton(onClick = { onEvent(ThemeFormUiEvent.ClearError) }) {
                        Text("Cerrar")
                    }
                }
            }
        }

        Button(
            onClick = { onEvent(ThemeFormUiEvent.Save) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isSaving && !state.isDeleting,
        ) {
            if (state.isSaving) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Text(if (state.isEditMode) "Guardar cambios" else "Crear tema")
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
                onClick = { onEvent(ThemeFormUiEvent.DeleteTheme) },
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
                    if (state.isDeleting) "Eliminando..." else "Eliminar tema",
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
private fun ThemeFormPreview() {
    MentorlyTheme {
        ThemeFormContent(
            state = ThemeFormUiState(
                isEditMode = true,
                title = "Introducción al Diseño de Interfaces",
                contentText = "En este tema exploraremos los fundamentos de la creación de interfaces de usuario.",
                orderIndex = "1",
            ),
            onEvent = {},
            onBackClick = {},
        )
    }
}
