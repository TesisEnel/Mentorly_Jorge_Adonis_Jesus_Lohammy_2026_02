package com.sagrd.mentorly.presentation.admin.student.list

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sagrd.mentorly.domain.model.student.Student
import com.sagrd.mentorly.domain.model.student.StudentRole
import com.sagrd.mentorly.ui.theme.MentorlyTheme

@Composable
fun AdminStudentListScreen(
    onBackClick: () -> Unit,
    viewModel: AdminStudentListViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val filteredStudents by viewModel.filteredStudents.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(state.successMessage) {
        state.successMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.onEvent(AdminStudentListUiEvent.ClearSuccessMessage)
        }
    }

    if (state.studentPendingPromotion != null) {
        AlertDialog(
            onDismissRequest = { viewModel.onEvent(AdminStudentListUiEvent.DismissPromotionDialog) },
            title = { Text("Confirmar promoción") },
            text = { Text("¿Deseas promover a ${state.studentPendingPromotion?.displayName} a administrador?") },
            confirmButton = {
                TextButton(onClick = { viewModel.onEvent(AdminStudentListUiEvent.ConfirmPromotion) }) {
                    Text("Promover")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onEvent(AdminStudentListUiEvent.DismissPromotionDialog) }) {
                    Text("Cancelar")
                }
            }
        )
    }

    AdminStudentListContent(
        state = state,
        filteredStudents = filteredStudents,
        onEvent = viewModel::onEvent,
        onBackClick = onBackClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminStudentListContent(
    state: AdminStudentListUiState,
    filteredStudents: List<Student>,
    onEvent: (AdminStudentListUiEvent) -> Unit,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Administrar estudiantes") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Atrás"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { onEvent(AdminStudentListUiEvent.SearchChanged(it)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Buscar por nombre...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true
            )

            if (state.isLoading && !state.isRefreshing) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (state.errorMessage != null) {
                ErrorContent(
                    message = state.errorMessage,
                    onRetry = { onEvent(AdminStudentListUiEvent.Load) }
                )
            } else if (filteredStudents.isEmpty()) {
                EmptyContent()
            } else {
                StudentList(
                    students = filteredStudents,
                    promotingStudentId = state.promotingStudentId,
                    onPromoteClick = { studentId ->
                        onEvent(AdminStudentListUiEvent.RequestPromotion(studentId))
                    }
                )
            }
        }
    }
}

@Composable
private fun StudentList(
    students: List<Student>,
    promotingStudentId: String?,
    onPromoteClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(students, key = { it.id }) { student ->
            StudentItem(
                student = student,
                isPromoting = promotingStudentId == student.id,
                onPromoteClick = { onPromoteClick(student.id) }
            )
        }
    }
}

@Composable
private fun StudentItem(
    student: Student,
    isPromoting: Boolean,
    onPromoteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = student.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SuggestionChip(
                        onClick = { },
                        label = {
                            Text(
                                if (student.role == StudentRole.ADMIN) "Administrador"
                                else "Estudiante"
                            )
                        },
                        enabled = false
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${student.totalPoints} pts",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            if (student.role != StudentRole.ADMIN) {
                if (isPromoting) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    Button(onClick = onPromoteClick) {
                        Text("Promover")
                    }
                }
            }
        }
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = message, color = MaterialTheme.colorScheme.error)
        Button(
            onClick = onRetry,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Reintentar")
        }
    }
}

@Composable
private fun EmptyContent() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("No hay estudiantes registrados.")
    }
}

@Preview(showBackground = true)
@Composable
fun AdminStudentListScreenPreview() {
    MentorlyTheme {
        AdminStudentListContent(
            state = AdminStudentListUiState(
                students = listOf(
                    Student("1", "juan@example.com", "Juan Perez", StudentRole.STUDENT, true, 100),
                    Student("2", "admin@example.com", "Admin Sistema", StudentRole.ADMIN, true, 500)
                )
            ),
            filteredStudents = listOf(
                Student("1", "juan@example.com", "Juan Perez", StudentRole.STUDENT, true, 100),
                Student("2", "admin@example.com", "Admin Sistema", StudentRole.ADMIN, true, 500)
            ),
            onEvent = {},
            onBackClick = {}
        )
    }
}
