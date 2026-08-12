package com.sagrd.mentorly.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sagrd.mentorly.domain.model.student.Student
import com.sagrd.mentorly.domain.model.student.StudentRole
import com.sagrd.mentorly.domain.model.student.StudentStatistics

@Composable
fun ProfileScreen(
    onSignOutCompleted: () -> Unit,
    onAdminDashboardClick: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isSignedOut) {
        if (uiState.isSignedOut) {
            viewModel.onEvent(ProfileUiEvent.SignOutHandled)
            onSignOutCompleted()
        }
    }

    ProfileScreenContent(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onAdminDashboardClick = onAdminDashboardClick
    )
}

@Composable
fun ProfileScreenContent(
    uiState: ProfileUiState,
    onEvent: (ProfileUiEvent) -> Unit,
    onAdminDashboardClick: () -> Unit
) {
    if (uiState.isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val student = uiState.student ?: return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = student.displayName.firstOrNull()?.uppercase() ?: "?",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
        Spacer(Modifier.height(12.dp))
        Text(student.displayName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(
            text = if (student.role == StudentRole.ADMIN) "Administrador" else "Estudiante",
            style = MaterialTheme.typography.bodyMedium
        )
        Text("${student.totalPoints} pts", style = MaterialTheme.typography.bodyMedium)

        Spacer(Modifier.height(24.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("Información personal", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Text("Correo: ${student.email ?: "-"}")
                Text("Nombre: ${student.displayName}")
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = { onEvent(ProfileUiEvent.ShowEditDialog) }) {
                    Text("Editar perfil")
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("Insignias", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Text("Total: ${uiState.statistics?.badges?.size ?: 0}")
                uiState.statistics?.badges?.forEach { badge ->
                    Text("• ${badge.name}", style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Aparecer en el ranking")
                Switch(
                    checked = student.isLeaderboardPublic,
                    onCheckedChange = { onEvent(ProfileUiEvent.PrivacyChanged(it)) }
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        if (student.role == StudentRole.ADMIN) {
            OutlinedButton(
                onClick = onAdminDashboardClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Panel administrativo")
            }

            Spacer(Modifier.height(12.dp))
        }

        Button(
            onClick = { onEvent(ProfileUiEvent.ShowSignOutDialog) },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Text("Cerrar sesión")
        }
    }

    if (uiState.isEditDialogVisible) {
        AlertDialog(
            onDismissRequest = { onEvent(ProfileUiEvent.DismissEditDialog) },
            title = { Text("Editar perfil") },
            text = {
                Column {
                    OutlinedTextField(
                        value = uiState.editedDisplayName,
                        onValueChange = { onEvent(ProfileUiEvent.DisplayNameChanged(it)) },
                        label = { Text("Nombre") }
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = uiState.editedEmail,
                        onValueChange = { onEvent(ProfileUiEvent.EmailChanged(it)) },
                        label = { Text("Correo") }
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { onEvent(ProfileUiEvent.SaveProfile) },
                    enabled = !uiState.isSaving
                ) { Text("Guardar") }
            },
            dismissButton = {
                TextButton(onClick = { onEvent(ProfileUiEvent.DismissEditDialog) }) { Text("Cancelar") }
            }
        )
    }

    if (uiState.isSignOutDialogVisible) {
        AlertDialog(
            onDismissRequest = { onEvent(ProfileUiEvent.DismissSignOutDialog) },
            title = { Text("Cerrar sesión") },
            text = { Text("¿Seguro que deseas cerrar sesión?") },
            confirmButton = {
                TextButton(onClick = { onEvent(ProfileUiEvent.ConfirmSignOut) }) { Text("Cerrar sesión") }
            },
            dismissButton = {
                TextButton(onClick = { onEvent(ProfileUiEvent.DismissSignOutDialog) }) { Text("Cancelar") }
            }
        )
    }

    uiState.errorMessage?.let { message ->
        AlertDialog(
            onDismissRequest = { onEvent(ProfileUiEvent.DismissError) },
            title = { Text("Error") },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = { onEvent(ProfileUiEvent.DismissError) }) { Text("Aceptar") }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileScreenPreview() {
    MaterialTheme {
        ProfileScreenContent(
            uiState = ProfileUiState(
                isLoading = false,
                student = Student(
                    id = "1",
                    email = "alex@mentorly.edu",
                    displayName = "Alex Developer",
                    role = StudentRole.STUDENT,
                    isLeaderboardPublic = true,
                    totalPoints = 1450
                ),
                statistics = StudentStatistics(
                    studentId = "1",
                    role = StudentRole.STUDENT,
                    isLeaderboardPublic = true,
                    totalPoints = 1450,
                    badges = emptyList()
                )
            ),
            onEvent = {},
            onAdminDashboardClick = {}
        )
    }
}
