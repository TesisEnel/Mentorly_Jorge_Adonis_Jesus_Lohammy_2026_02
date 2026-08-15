package com.sagrd.mentorly.presentation.community.members

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sagrd.mentorly.domain.model.community.CourseMember
import com.sagrd.mentorly.ui.theme.MentorlyTheme

@Composable
fun CourseMembersScreen(
    courseId: String,
    onBackClick: () -> Unit,
    onStudentClick: ((String) -> Unit)? = null,
    viewModel: CourseMembersViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val filteredMembers by viewModel.filteredMembers.collectAsStateWithLifecycle()

    LaunchedEffect(courseId) {
        viewModel.setCourseId(courseId)
    }

    CourseMembersContent(
        state = state,
        filteredMembers = filteredMembers,
        onEvent = viewModel::onEvent,
        onBackClick = onBackClick,
        onStudentClick = { studentId -> onStudentClick?.invoke(studentId) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseMembersContent(
    state: CourseMembersUiState,
    filteredMembers: List<CourseMember>,
    onEvent: (CourseMembersUiEvent) -> Unit,
    onBackClick: () -> Unit,
    onStudentClick: (String) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Compañeros del curso") },
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
                onValueChange = { onEvent(CourseMembersUiEvent.SearchChanged(it)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Buscar compañeros...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true
            )

            if (state.isLoading && !state.isRefreshing) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (state.errorMessage != null) {
                ErrorView(
                    message = state.errorMessage,
                    onRetry = { onEvent(CourseMembersUiEvent.Load) }
                )
            } else if (filteredMembers.isEmpty() && !state.isLoading) {
                EmptyView(
                    message = if (state.searchQuery.isEmpty())
                        "Aún no hay compañeros visibles en este curso."
                    else "No se encontraron compañeros."
                )
            } else {
                Text(
                    text = "${filteredMembers.size} compañeros visibles",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredMembers) { member ->
                        MemberItem(
                            member = member,
                            onClick = { onStudentClick(member.studentId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MemberItem(member: CourseMember, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
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
            Column {
                Text(
                    text = member.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${member.totalPoints} puntos",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun ErrorView(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = message, color = MaterialTheme.colorScheme.error)
        Button(onClick = onRetry, modifier = Modifier.padding(top = 16.dp)) {
            Text("Reintentar")
        }
    }
}

@Composable
fun EmptyView(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = message, style = MaterialTheme.typography.bodyLarge)
    }
}

@Preview(showBackground = true)
@Composable
fun CourseMembersScreenPreview() {
    MentorlyTheme {
        CourseMembersContent(
            state = CourseMembersUiState(
                members = listOf(
                    CourseMember("1", "Juan Perez", 150),
                    CourseMember("2", "Maria Lopez", 200)
                )
            ),
            filteredMembers = listOf(
                CourseMember("1", "Juan Perez", 150),
                CourseMember("2", "Maria Lopez", 200)
            ),
            onEvent = {},
            onBackClick = {},
            onStudentClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CourseMembersScreenEmptyPreview() {
    MentorlyTheme {
        CourseMembersContent(
            state = CourseMembersUiState(),
            filteredMembers = emptyList(),
            onEvent = {},
            onBackClick = {},
            onStudentClick = {}
        )
    }
}
