package com.sagrd.mentorly.presentation.admin.quiz

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HelpOutline
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
import com.sagrd.mentorly.domain.model.quiz.AdminQuizQuestion
import com.sagrd.mentorly.ui.theme.MentorlyTheme

@Composable
fun AdminQuizQuestionScreen(
    activityId: String,
    onBackClick: () -> Unit,
    onQuestionCreated: () -> Unit,
    viewModel: AdminQuizQuestionViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(activityId) {
        viewModel.onEvent(AdminQuizQuestionUiEvent.Load(activityId))
    }
    LaunchedEffect(state.isQuestionSaved) {
        if (state.isQuestionSaved) {
            viewModel.onEvent(AdminQuizQuestionUiEvent.QuestionSavedHandled)
            onQuestionCreated()
        }
    }

    AdminQuizQuestionContent(
        state = state,
        onEvent = viewModel::onEvent,
        onBackClick = onBackClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminQuizQuestionContent(
    state: AdminQuizQuestionUiState,
    onEvent: (AdminQuizQuestionUiEvent) -> Unit,
    onBackClick: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Administrar preguntas") },
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
                "No tienes permisos para crear preguntas de quiz.",
                Modifier.padding(paddingValues),
            )
            state.isLoading -> LoadingContent(Modifier.padding(paddingValues))
            else -> QuestionManagementContent(
                state = state,
                onEvent = onEvent,
                onBackClick = onBackClick,
                modifier = Modifier.padding(paddingValues),
            )
        }
    }
}

@Composable
private fun QuestionManagementContent(
    state: AdminQuizQuestionUiState,
    onEvent: (AdminQuizQuestionUiEvent) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            if (state.editingQuestionId == null) "Agregar pregunta" else "Editar pregunta",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
                if (state.editingQuestionId == null) {
                    "Completa los campos para agregar una nueva pregunta al cuestionario."
                } else {
                    "Modifica los datos de la pregunta seleccionada."
                },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        OutlinedTextField(
            value = state.question,
            onValueChange = { onEvent(AdminQuizQuestionUiEvent.QuestionChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Pregunta") },
            placeholder = { Text("Escribe la pregunta aquí...") },
            isError = state.questionError != null,
            supportingText = {
                state.questionError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            },
            enabled = !state.isSaving && !state.isDeleting,
            minLines = 3,
        )

        OutlinedTextField(
            value = state.correctAnswer,
            onValueChange = { onEvent(AdminQuizQuestionUiEvent.CorrectAnswerChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Respuesta correcta") },
            placeholder = { Text("Escribe la respuesta correcta") },
            isError = state.correctAnswerError != null,
            supportingText = {
                state.correctAnswerError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            },
            enabled = !state.isSaving && !state.isDeleting,
            singleLine = true,
        )

        OutlinedTextField(
            value = state.orderIndex,
            onValueChange = { onEvent(AdminQuizQuestionUiEvent.OrderChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Posición") },
            isError = state.orderError != null,
            supportingText = {
                state.orderError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            },
            enabled = !state.isSaving && !state.isDeleting,
            singleLine = true,
        )

        state.errorMessage?.let { message ->
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                Column(Modifier.padding(12.dp)) {
                    Text(message, color = MaterialTheme.colorScheme.onErrorContainer)
                    TextButton(onClick = { onEvent(AdminQuizQuestionUiEvent.ClearError) }) {
                        Text("Cerrar")
                    }
                }
            }
        }

        Button(
            onClick = { onEvent(AdminQuizQuestionUiEvent.SaveQuestion) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isSaving && !state.isDeleting,
        ) {
            if (state.isSaving) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Text(if (state.editingQuestionId == null) "Agregar pregunta" else "Guardar cambios")
            }
        }

        OutlinedButton(
            onClick = {
                if (state.editingQuestionId == null) onBackClick()
                else onEvent(AdminQuizQuestionUiEvent.CancelEdit)
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isSaving && !state.isDeleting,
        ) {
            Text(if (state.editingQuestionId == null) "Volver" else "Cancelar edición")
        }

        if (state.editingQuestionId != null) {
            OutlinedButton(
                onClick = { onEvent(AdminQuizQuestionUiEvent.DeleteQuestion) },
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
                    if (state.isDeleting) "Eliminando..." else "Eliminar pregunta",
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }

        Text("Preguntas", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)

        if (state.questions.isEmpty()) {
            Text(
                "Aún no hay preguntas registradas para este cuestionario.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            state.questions.forEachIndexed { index, question ->
                QuizQuestionItem(
                    index = index + 1,
                    question = question,
                    onEditClick = { onEvent(AdminQuizQuestionUiEvent.EditQuestion(question.id)) },
                )
            }
        }
    }
}

@Composable
private fun QuizQuestionItem(
    index: Int,
    question: AdminQuizQuestion,
    onEditClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Pregunta $index", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(question.prompt, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                IconButton(onClick = onEditClick) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar pregunta")
                }
            }
            Text(
                "Respuesta correcta: ${question.correctAnswer}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                "Posición ${question.orderIndex}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
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
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.HelpOutline, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(message)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AdminQuizQuestionPreview() {
    MentorlyTheme {
        AdminQuizQuestionContent(
            state = AdminQuizQuestionUiState(
                questions = listOf(
                    AdminQuizQuestion("question-1", "¿Cuál es el primer principio de Material Design?", "La accesibilidad", 1),
                    AdminQuizQuestion("question-2", "Define el concepto de heurística.", "Una regla práctica", 2),
                ),
            ),
            onEvent = {},
            onBackClick = {},
        )
    }
}
