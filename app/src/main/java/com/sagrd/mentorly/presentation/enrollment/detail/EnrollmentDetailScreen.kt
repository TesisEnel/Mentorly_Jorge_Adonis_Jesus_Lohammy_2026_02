package com.sagrd.mentorly.presentation.enrollment.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.sagrd.mentorly.domain.model.enrollment.Certificate
import com.sagrd.mentorly.domain.model.enrollment.Enrollment
import com.sagrd.mentorly.domain.model.enrollment.EnrollmentStatus
import com.sagrd.mentorly.ui.theme.MentorlyTheme

@Composable
fun EnrollmentDetailScreen(
    enrollmentId: String,
    onBackClick: () -> Unit,
    onRestarted: (String) -> Unit,
    viewModel: EnrollmentDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(enrollmentId) { viewModel.initialize(enrollmentId) }
    LaunchedEffect(uiState.restartedEnrollmentId) {
        uiState.restartedEnrollmentId?.let(onRestarted)
    }

    EnrollmentDetailContent(
        uiState = uiState,
        onBackClick = onBackClick,
        onEvent = viewModel::onEvent
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EnrollmentDetailContent(
    uiState: EnrollmentDetailUiState,
    onBackClick: () -> Unit,
    onEvent: (EnrollmentDetailUiEvent) -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Detalle de inscripción", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { innerPadding ->
        when {
            uiState.isLoading && uiState.enrollment == null -> Box(
                Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }

            uiState.enrollment == null -> ErrorContent(
                message = uiState.errorMessage ?: "No se encontró la inscripción.",
                onRetry = { onEvent(EnrollmentDetailUiEvent.Refresh) },
                modifier = Modifier.fillMaxSize().padding(innerPadding)
            )

            else -> EnrollmentInformation(
                enrollment = uiState.enrollment,
                status = uiState.currentStatus ?: uiState.enrollment.status,
                certificate = uiState.certificate,
                isRestarting = uiState.isRestarting,
                isLoadingCertificate = uiState.isLoadingCertificate,
                errorMessage = uiState.errorMessage,
                onLoadCertificate = { onEvent(EnrollmentDetailUiEvent.LoadCertificate) },
                onRestart = { onEvent(EnrollmentDetailUiEvent.ShowRestartConfirmation) },
                onDismissError = { onEvent(EnrollmentDetailUiEvent.ClearError) },
                modifier = Modifier.fillMaxSize().padding(innerPadding)
            )
        }
    }

    if (uiState.isRestartConfirmationVisible) {
        AlertDialog(
            onDismissRequest = { onEvent(EnrollmentDetailUiEvent.DismissRestartConfirmation) },
            title = { Text("Reiniciar curso") },
            text = { Text("Se creará un nuevo intento para este curso.") },
            confirmButton = {
                TextButton(onClick = { onEvent(EnrollmentDetailUiEvent.ConfirmRestart) }, enabled = !uiState.isRestarting) {
                    Text("Reiniciar")
                }
            },
            dismissButton = {
                TextButton(onClick = { onEvent(EnrollmentDetailUiEvent.DismissRestartConfirmation) }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun EnrollmentInformation(
    enrollment: Enrollment,
    status: EnrollmentStatus,
    certificate: Certificate?,
    isRestarting: Boolean,
    isLoadingCertificate: Boolean,
    errorMessage: String?,
    onLoadCertificate: () -> Unit,
    onRestart: () -> Unit,
    onDismissError: () -> Unit,
    modifier: Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Intento ${enrollment.attemptNumber}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                Text("Estado: ${status.label()}", color = MaterialTheme.colorScheme.primary)
                Text("Inició: ${enrollment.startedAt}")
                Text("Vence: ${enrollment.expiresAt}")
                enrollment.completedAt?.let { Text("Completó: $it") }
            }
        }

        if (status == EnrollmentStatus.COMPLETED) {
            Button(onClick = onLoadCertificate, enabled = !isLoadingCertificate, modifier = Modifier.fillMaxWidth()) {
                Text(if (isLoadingCertificate) "Cargando certificado..." else "Ver certificado")
            }
        }

        if (status == EnrollmentStatus.EXPIRED) {
            OutlinedButton(onClick = onRestart, enabled = !isRestarting, modifier = Modifier.fillMaxWidth()) {
                Text(if (isRestarting) "Reiniciando..." else "Reiniciar curso")
            }
        }

        certificate?.let {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Certificado disponible", fontWeight = FontWeight.SemiBold)
                    Text("Emitido: ${it.issuedAt}", style = MaterialTheme.typography.bodySmall)
                    Text(it.certificateUrl, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        errorMessage?.let { message ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text(message, color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(4.dp))
                    TextButton(onClick = onDismissError) { Text("Aceptar") }
                }
            }
        }
    }
}

private fun EnrollmentStatus.label(): String = when (this) {
    EnrollmentStatus.ACTIVE -> "Activa"
    EnrollmentStatus.COMPLETED -> "Completada"
    EnrollmentStatus.EXPIRED -> "Expirada"
}

@Composable
private fun ErrorContent(message: String, onRetry: () -> Unit, modifier: Modifier) {
    Box(modifier.padding(24.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(message, color = MaterialTheme.colorScheme.error)
            Button(onClick = onRetry) { Text("Reintentar") }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun EnrollmentDetailPreview() {
    MentorlyTheme {
        EnrollmentDetailContent(
            uiState = EnrollmentDetailUiState(
                enrollment = Enrollment("enrollment-1", "student-1", "course-1", 1, "2026-08-12", "2026-11-12", null, EnrollmentStatus.ACTIVE)
            ),
            onBackClick = {},
            onEvent = {}
        )
    }
}
