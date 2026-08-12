package com.sagrd.mentorly.presentation.quiz

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.sagrd.mentorly.domain.model.quiz.QuizAttempt
import com.sagrd.mentorly.domain.model.quiz.QuizQuestion
import com.sagrd.mentorly.ui.theme.MentorlyTheme
import com.sagrd.mentorly.util.DateFormatter

@Composable
fun QuizScreen(
    enrollmentId: String,
    activityId: String,
    onBackClick: () -> Unit,
    onQuizSubmitted: () -> Unit,
    viewModel: QuizViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(enrollmentId, activityId) {
        viewModel.initialize(enrollmentId, activityId)
    }
    LaunchedEffect(uiState.isSubmitted) {
        if (uiState.isSubmitted) onQuizSubmitted()
    }

    QuizContent(
        uiState = uiState,
        onBackClick = onBackClick,
        onEvent = viewModel::onEvent
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuizContent(
    uiState: QuizUiState,
    onBackClick: () -> Unit,
    onEvent: (QuizUiEvent) -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Cuestionario",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        },
        bottomBar = {
            if (uiState.questions.isNotEmpty() && !uiState.isLoading) {
                SubmitButton(
                    isSubmitting = uiState.isSubmitting,
                    isSubmitted = uiState.isSubmitted,
                    onClick = { onEvent(QuizUiEvent.SubmitQuiz) }
                )
            }
        }
    ) { innerPadding ->
        when {
            uiState.isLoading && uiState.questions.isEmpty() -> LoadingContent(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )

            uiState.errorMessage != null && uiState.questions.isEmpty() -> ErrorContent(
                message = uiState.errorMessage,
                onRetry = { onEvent(QuizUiEvent.Retry) },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )

            uiState.questions.isEmpty() -> EmptyContent(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )

            else -> QuizQuestions(
                uiState = uiState,
                onEvent = onEvent,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        }
    }
}

@Composable
private fun QuizQuestions(
    uiState: QuizUiState,
    onEvent: (QuizUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        uiState.result?.let { result ->
            item {
                ResultCard(result = result)
            }
        }

        uiState.errorMessage?.let { message ->
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = message,
                            color = MaterialTheme.colorScheme.error
                        )
                        TextButton(onClick = { onEvent(QuizUiEvent.ClearError) }) {
                            Text("Aceptar")
                        }
                    }
                }
            }
        }

        itemsIndexed(
            items = uiState.questions,
            key = { _, question -> question.id }
        ) { index, question ->
            QuestionCard(
                question = question,
                position = index + 1,
                total = uiState.questions.size,
                answer = uiState.answers[question.id].orEmpty(),
                enabled = !uiState.isSubmitting && !uiState.isSubmitted,
                onAnswerChanged = { answer ->
                    onEvent(QuizUiEvent.AnswerChanged(question.id, answer))
                }
            )
        }
    }
}

@Composable
private fun QuestionCard(
    question: QuizQuestion,
    position: Int,
    total: Int,
    answer: String,
    enabled: Boolean,
    onAnswerChanged: (String) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Pregunta $position de $total",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = question.prompt,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            OutlinedTextField(
                value = answer,
                onValueChange = onAnswerChanged,
                modifier = Modifier.fillMaxWidth(),
                enabled = enabled,
                label = { Text("Tu respuesta") },
                minLines = 2
            )
        }
    }
}

@Composable
private fun SubmitButton(
    isSubmitting: Boolean,
    isSubmitted: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = !isSubmitting && !isSubmitted,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        if (isSubmitting) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(2.dp),
                    strokeWidth = 2.dp
                )
                Text("Enviando...")
            }
        } else {
            Text(if (isSubmitted) "Intento enviado" else "Enviar intento")
        }
    }
}

@Composable
private fun ResultCard(result: QuizAttempt) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Resultado final",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(text = "Puntuación: ${result.score}")
            Text(text = if (result.passed) "Cuestionario aprobado" else "Cuestionario no aprobado")
            Text(
                text = "Enviado: ${DateFormatter.format(result.submittedAtUtc)}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun LoadingContent(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.padding(24.dp), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error
            )
            Button(onClick = onRetry) {
                Text("Reintentar")
            }
        }
    }
}

@Composable
private fun EmptyContent(modifier: Modifier = Modifier) {
    Box(modifier = modifier.padding(24.dp), contentAlignment = Alignment.Center) {
        Text("No se encontraron preguntas para este cuestionario.")
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun QuizScreenPreview() {
    MentorlyTheme {
        QuizContent(
            uiState = QuizUiState(
                isLoading = false,
                questions = listOf(
                    QuizQuestion(
                        id = "question-1",
                        prompt = "¿Qué significa encapsulación en programación orientada a objetos?",
                        orderIndex = 1
                    ),
                    QuizQuestion(
                        id = "question-2",
                        prompt = "Explica la diferencia entre una clase y un objeto.",
                        orderIndex = 2
                    )
                ),
                answers = mapOf(
                    "question-1" to "Es el mecanismo que protege el estado interno de un objeto."
                )
            ),
            onBackClick = {},
            onEvent = {}
        )
    }
}
