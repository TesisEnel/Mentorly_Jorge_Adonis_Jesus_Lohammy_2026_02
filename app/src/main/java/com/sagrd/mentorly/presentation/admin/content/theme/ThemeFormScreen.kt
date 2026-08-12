package com.sagrd.mentorly.presentation.admin.content.theme

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeFormScreen(
    unitId: String,
    themeId: String? = null,
    onBackClick: () -> Unit,
    onSaved: () -> Unit,
    viewModel: ThemeFormViewModel = hiltViewModel(),
) {
    val s by viewModel.state.collectAsState()
    LaunchedEffect(themeId) { viewModel.onEvent(ThemeFormUiEvent.Load(unitId, themeId)) }
    LaunchedEffect(s.isSaved) { if (s.isSaved) onSaved() }
    Scaffold(topBar = { TopAppBar(title = { Text("Tema") }) }) { p ->
        Column(Modifier.padding(p).padding(16.dp)) {
            OutlinedTextField(
                s.title,
                { viewModel.onEvent(ThemeFormUiEvent.TitleChanged(it)) },
                label = { Text("Título") },
            )
            OutlinedTextField(
                s.contentText,
                { viewModel.onEvent(ThemeFormUiEvent.ContentChanged(it)) },
                label = { Text("Contenido") },
            )
            OutlinedTextField(
                s.orderIndex,
                { viewModel.onEvent(ThemeFormUiEvent.OrderChanged(it)) },
                label = { Text("Orden") },
            )
            Text(s.errorMessage.orEmpty())
            Button(onClick = { viewModel.onEvent(ThemeFormUiEvent.Save) }, enabled = !s.isSaving) {
                Text("Guardar")
            }
        }
    }
}
