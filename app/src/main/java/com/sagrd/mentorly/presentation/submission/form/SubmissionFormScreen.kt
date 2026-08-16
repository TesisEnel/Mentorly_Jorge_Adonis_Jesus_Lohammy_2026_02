package com.sagrd.mentorly.presentation.submission.form

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import com.sagrd.mentorly.domain.model.submission.EvidenceType
import com.sagrd.mentorly.ui.theme.MentorlyTheme

@Composable
fun SubmissionFormScreen(
    enrollmentId: String,
    activityId: String,
    submissionId: String? = null,
    onBackClick: () -> Unit,
    onSaved: (String) -> Unit,
    viewModel: SubmissionFormViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(enrollmentId, activityId, submissionId) {
        viewModel.onEvent(SubmissionFormUiEvent.Load(enrollmentId, activityId, submissionId))
    }

    LaunchedEffect(state.savedSubmissionId) {
        state.savedSubmissionId?.let(onSaved)
    }

    SubmissionFormContent(
        state = state,
        onEvent = viewModel::onEvent,
        onBackClick = onBackClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SubmissionFormContent(
    state: SubmissionFormUiState,
    onEvent: (SubmissionFormUiEvent) -> Unit,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = if (state.isEditing) "Editar entrega" else "Nueva entrega",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                TextButton(
                    onClick = { onEvent(SubmissionFormUiEvent.EvidenceTypeChanged(EvidenceType.URL)) },
                    enabled = state.evidenceType != EvidenceType.URL
                ) {
                    Text("Enlace")
                }
                TextButton(
                    onClick = { onEvent(SubmissionFormUiEvent.EvidenceTypeChanged(EvidenceType.TEXT)) },
                    enabled = state.evidenceType != EvidenceType.TEXT
                ) {
                    Text("Texto")
                }
            }

            OutlinedTextField(
                value = state.evidenceContent,
                onValueChange = { onEvent(SubmissionFormUiEvent.EvidenceContentChanged(it)) },
                label = { Text(if (state.evidenceType == EvidenceType.URL) "Enlace de evidencia" else "Evidencia textual") },
                placeholder = {
                    Text(
                        if (state.evidenceType == EvidenceType.URL) {
                            "https://github.com/usuario/repositorio"
                        } else {
                            "Describe o pega tu evidencia"
                        }
                    )
                },
                isError = state.evidenceContentError != null,
                supportingText = {
                    state.evidenceContentError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                },
                singleLine = state.evidenceType == EvidenceType.URL,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = { onEvent(SubmissionFormUiEvent.Save) },
                enabled = !state.isSaving,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                } else {
                    Text("Guardar")
                }
            }
        }
    }

    state.errorMessage?.let { message ->
        AlertDialog(
            onDismissRequest = { onEvent(SubmissionFormUiEvent.DismissError) },
            title = { Text("Error") },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = { onEvent(SubmissionFormUiEvent.DismissError) }) { Text("Aceptar") }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SubmissionFormPreview() {
    MentorlyTheme {
        SubmissionFormContent(
            state = SubmissionFormUiState(
                isEditing = false,
                evidenceType = EvidenceType.URL,
                evidenceContent = "https://github.com/usuario/repositorio"
            ),
            onEvent = {},
            onBackClick = {}
        )
    }
}
