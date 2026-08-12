package com.sagrd.mentorly.presentation.admin.content.unit

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitFormScreen(
    courseId: String,
    unitId: String? = null,
    onBackClick: () -> Unit,
    onSaved: () -> Unit,
    viewModel: UnitFormViewModel = hiltViewModel(),
) {
    val s by viewModel.state.collectAsState()
    LaunchedEffect(unitId) { viewModel.onEvent(UnitFormUiEvent.Load(courseId, unitId)) }
    LaunchedEffect(s.isSaved) { if (s.isSaved) onSaved() }
    Scaffold(topBar = { TopAppBar(title = { Text("Unidad") }) }) { p ->
        Column(Modifier.padding(p).padding(16.dp)) {
            OutlinedTextField(
                s.title,
                { viewModel.onEvent(UnitFormUiEvent.TitleChanged(it)) },
                label = { Text("Título") },
            )
            OutlinedTextField(
                s.orderIndex,
                { viewModel.onEvent(UnitFormUiEvent.OrderChanged(it)) },
                label = { Text("Orden") },
            )
            Text(s.errorMessage.orEmpty())
            Button(onClick = { viewModel.onEvent(UnitFormUiEvent.Save) }, enabled = !s.isSaving) {
                Text("Guardar")
            }
        }
    }
}
