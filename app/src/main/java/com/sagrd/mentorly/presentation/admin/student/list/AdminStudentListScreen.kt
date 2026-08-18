package com.sagrd.mentorly.presentation.admin.student.list

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sagrd.mentorly.domain.model.student.Student
import com.sagrd.mentorly.domain.model.student.StudentRole
import com.sagrd.mentorly.ui.theme.MentorlyTheme

@Composable
fun AdminStudentListScreen(
    onBackClick: () -> Unit,
    onStudentClick: (String) -> Unit,
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
        onBackClick = onBackClick,
        onStudentClick = onStudentClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminStudentListContent(
    state: AdminStudentListUiState,
    filteredStudents: List<Student>,
    onEvent: (AdminStudentListUiEvent) -> Unit,
    onBackClick: () -> Unit,
    onStudentClick: (String) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Administrar estudiantes",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Atrás",
                            tint = MaterialTheme.colorScheme.onSurface
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
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Search Bar
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { onEvent(AdminStudentListUiEvent.SearchChanged(it)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Buscar por nombre...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (state.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onEvent(AdminStudentListUiEvent.SearchChanged("")) }) {
                            Icon(Icons.Default.Close, contentDescription = "Limpiar")
                        }
                    }
                },
                shape = RoundedCornerShape(24.dp),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                )
            )

            if (state.isLoading && !state.isRefreshing && state.students.isEmpty()) {
                LoadingState()
            } else if (state.errorMessage != null && state.students.isEmpty()) {
                ErrorContent(
                    message = state.errorMessage,
                    onRetry = { onEvent(AdminStudentListUiEvent.Load) }
                )
            } else if (filteredStudents.isEmpty() && !state.isLoading) {
                EmptyContent()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(filteredStudents, key = { it.id }) { student ->
                        StudentItem(
                            student = student,
                            isPromoting = state.promotingStudentId == student.id,
                            onPromoteClick = { onEvent(AdminStudentListUiEvent.RequestPromotion(student.id)) },
                            onDetailClick = { onStudentClick(student.id) }
                        )
                    }

                    if (state.isLoading) {
                        item {
                            LoadingItem()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StudentItem(
    student: Student,
    isPromoting: Boolean,
    onPromoteClick: () -> Unit,
    onDetailClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Avatar with initials
                val initials = student.displayName.split(" ")
                    .filter { it.isNotEmpty() }
                    .take(2)
                    .map { it[0] }
                    .joinToString("")
                    .uppercase()

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initials,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = student.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = student.email ?: "Sin correo",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Role Badge
                Surface(
                    color = if (student.role == StudentRole.ADMIN)
                        MaterialTheme.colorScheme.secondaryContainer
                    else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (student.role == StudentRole.ADMIN) "Administrador" else "Estudiante",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (student.role == StudentRole.ADMIN)
                            MaterialTheme.colorScheme.onSecondaryContainer
                        else MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Points and Privacy row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SmallInfoBadge(
                    icon = Icons.Default.Star,
                    text = "${"%,d".format(student.totalPoints)} pts"
                )
                SmallInfoBadge(
                    icon = if (student.isLeaderboardPublic) Icons.Default.Public else Icons.Default.VisibilityOff,
                    text = if (student.isLeaderboardPublic) "Perfil público" else "Perfil privado"
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (student.role != StudentRole.ADMIN) {
                    OutlinedButton(
                        onClick = onPromoteClick,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        enabled = !isPromoting
                    ) {
                        if (isPromoting) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Text("Promover a administrador", fontSize = 12.sp)
                        }
                    }
                }

                Button(
                    onClick = onDetailClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text("Ver detalle")
                }
            }
        }
    }
}

@Composable
fun SmallInfoBadge(icon: ImageVector, text: String) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun LoadingState() {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        repeat(3) {
            LoadingItem()
        }
    }
}

@Composable
private fun LoadingItem() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp).height(100.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(Color.LightGray.copy(alpha = 0.3f)))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Box(modifier = Modifier.width(120.dp).height(16.dp).background(Color.LightGray.copy(alpha = 0.3f)))
                Spacer(modifier = Modifier.height(8.dp))
                Box(modifier = Modifier.width(180.dp).height(12.dp).background(Color.LightGray.copy(alpha = 0.3f)))
            }
        }
    }
}

@Composable
private fun ErrorContent(message: String, onRetry: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = message, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRetry) { Text("Reintentar") }
        }
    }
}

@Composable
private fun EmptyContent() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("No se encontraron estudiantes.", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Preview(showBackground = true)
@Composable
fun AdminStudentListScreenPreview() {
    MentorlyTheme {
        AdminStudentListContent(
            state = AdminStudentListUiState(
                students = listOf(
                    Student("1", "juan.delgado@example.com", "Juan Delgado", StudentRole.STUDENT, true, 1250),
                    Student("2", "maria.admin@example.com", "Maria Acosta", StudentRole.ADMIN, false, 5400)
                )
            ),
            filteredStudents = listOf(
                Student("1", "juan.delgado@example.com", "Juan Delgado", StudentRole.STUDENT, true, 1250),
                Student("2", "maria.admin@example.com", "Maria Acosta", StudentRole.ADMIN, false, 5400)
            ),
            onEvent = {},
            onBackClick = {},
            onStudentClick = {}
        )
    }
}
