package com.sagrd.mentorly.presentation.admin.content.activity

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.sagrd.mentorly.domain.model.content.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityFormScreen(
    themeId: String,
    activityId: String? = null,
    onBackClick: () -> Unit,
    onSaved: () -> Unit,
    viewModel: ActivityFormViewModel = hiltViewModel(),
) {
    val s by viewModel.state.collectAsState()
    LaunchedEffect(activityId) { viewModel.onEvent(ActivityFormUiEvent.Load(themeId, activityId)) }
    LaunchedEffect(s.isSaved) { if (s.isSaved) onSaved() }
    Scaffold(topBar = { TopAppBar(title = { Text("Actividad") }) }) { p ->
        Column(Modifier.padding(p).padding(16.dp)) {
            OutlinedTextField(
                s.title,
                { viewModel.onEvent(ActivityFormUiEvent.TitleChanged(it)) },
                label = { Text("Título") },
            )
            Row {
                TextButton(
                    onClick = {
                        viewModel.onEvent(ActivityFormUiEvent.TypeChanged(ActivityType.EXERCISE))
                    }
                ) {
                    Text("Ejercicio")
                }
                TextButton(
                    onClick = {
                        viewModel.onEvent(ActivityFormUiEvent.TypeChanged(ActivityType.QUIZ))
                    }
                ) {
                    Text("Quiz")
                }
            }
            Row {
                Text("Obligatoria")
                Switch(
                    s.isMandatory,
                    { viewModel.onEvent(ActivityFormUiEvent.MandatoryChanged(it)) },
                )
            }
            OutlinedTextField(
                s.orderIndex,
                { viewModel.onEvent(ActivityFormUiEvent.OrderChanged(it)) },
                label = { Text("Orden") },
            )
            Text(s.errorMessage.orEmpty())
            Button(
                onClick = { viewModel.onEvent(ActivityFormUiEvent.Save) },
                enabled = !s.isSaving,
            ) {
                Text("Guardar")
            }
        }
    }
}
