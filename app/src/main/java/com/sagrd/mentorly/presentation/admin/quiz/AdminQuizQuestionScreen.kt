package com.sagrd.mentorly.presentation.admin.quiz

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminQuizQuestionScreen(
    activityId: String,
    onBackClick: () -> Unit,
    onQuestionCreated: () -> Unit,
    viewModel: AdminQuizQuestionViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isQuestionCreated) {
        if (uiState.isQuestionCreated) {
            viewModel.onEvent(AdminQuizQuestionUiEvent.ClearCreatedState)
            onQuestionCreated()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nueva pregunta de quiz") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(),
            )
        },
    ) { paddingValues ->
        AdminQuizQuestionContent(
            paddingValues = paddingValues,
            uiState = uiState,
            onEvent = { event -> viewModel.onEvent(event, activityId) },
        )
    }
}

@Composable
private fun AdminQuizQuestionContent(
    paddingValues: PaddingValues,
    uiState: AdminQuizQuestionUiState,
    onEvent: (AdminQuizQuestionUiEvent) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        OutlinedTextField(
            value = uiState.question,
            onValueChange = { onEvent(AdminQuizQuestionUiEvent.QuestionChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Pregunta") },
            isError = uiState.questionError != null,
            supportingText = {
                uiState.questionError?.let { error -> Text(error) }
            },
            enabled = !uiState.isSaving,
            minLines = 3,
        )

        OutlinedTextField(
            value = uiState.correctAnswer,
            onValueChange = { onEvent(AdminQuizQuestionUiEvent.CorrectAnswerChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Respuesta correcta") },
            isError = uiState.correctAnswerError != null,
            supportingText = {
                uiState.correctAnswerError?.let { error -> Text(error) }
            },
            enabled = !uiState.isSaving,
        )

        uiState.errorMessage?.let { message ->
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = message, color = MaterialTheme.colorScheme.error)
                TextButton(
                    onClick = {
                        onEvent(AdminQuizQuestionUiEvent.ClearError)
                        onEvent(AdminQuizQuestionUiEvent.SaveQuestion)
                    },
                ) {
                    Text("Reintentar")
                }
            }
        }

        if (uiState.isQuestionCreated) {
            Text("Pregunta creada correctamente.", color = MaterialTheme.colorScheme.primary)
        }

        Button(
            onClick = { onEvent(AdminQuizQuestionUiEvent.SaveQuestion) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isSaving,
        ) {
            if (uiState.isSaving) {
                CircularProgressIndicator()
            } else {
                Text("Crear pregunta")
            }
        }

        Text(
            text = "Vista previa",
            style = MaterialTheme.typography.titleMedium,
        )
        Text(uiState.question.ifBlank { "La pregunta aparecerá aquí." })
        Text(uiState.correctAnswer.ifBlank { "La respuesta correcta aparecerá aquí." })
    }
}
