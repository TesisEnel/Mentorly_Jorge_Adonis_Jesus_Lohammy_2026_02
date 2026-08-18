package com.sagrd.mentorly.presentation.community.members

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sagrd.mentorly.domain.model.community.CourseMember
import com.sagrd.mentorly.ui.theme.MentorlyTheme
import java.text.NumberFormat
import java.util.Locale

private val PrimaryBlue = Color(0xFF1565C0)
private val AvatarLavender = Color(0xFFE9DDF7)
private val AvatarRose = Color(0xFFA36F7E)
private val PointsBackground = Color(0xFFF4F1F8)

@Composable
fun CourseMembersScreen(
    courseId: String,
    onBackClick: () -> Unit,
    onStudentClick: ((String) -> Unit)? = null,
    viewModel: CourseMembersViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val filteredMembers by viewModel.filteredMembers.collectAsStateWithLifecycle()

    LaunchedEffect(courseId) { viewModel.setCourseId(courseId) }

    CourseMembersContent(
        state = state,
        filteredMembers = filteredMembers,
        onEvent = viewModel::onEvent,
        onBackClick = onBackClick,
        onStudentClick = { onStudentClick?.invoke(it) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseMembersContent(
    state: CourseMembersUiState,
    filteredMembers: List<CourseMember>,
    onEvent: (CourseMembersUiEvent) -> Unit,
    onBackClick: () -> Unit,
    onStudentClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Compañeros del curso", color = PrimaryBlue, fontWeight = FontWeight.Bold, fontSize = 24.sp) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Atrás") }
                },
                actions = {
                    IconButton(
                        onClick = { onEvent(CourseMembersUiEvent.Refresh) },
                        enabled = !state.isLoading && !state.isRefreshing && state.hasSession
                    ) { Icon(Icons.Outlined.Refresh, "Actualizar", tint = PrimaryBlue) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        when {
            state.isLoading && state.members.isEmpty() -> LoadingView(Modifier.fillMaxSize().padding(innerPadding))
            !state.hasSession -> ErrorView(
                state.errorMessage ?: "No se encontró una sesión activa.",
                { onEvent(CourseMembersUiEvent.Load) },
                Modifier.fillMaxSize().padding(innerPadding)
            )
            else -> MembersList(
                state,
                filteredMembers,
                onEvent,
                onStudentClick,
                Modifier.fillMaxSize().padding(innerPadding)
            )
        }
    }
}

@Composable
private fun MembersList(
    state: CourseMembersUiState,
    filteredMembers: List<CourseMember>,
    onEvent: (CourseMembersUiEvent) -> Unit,
    onStudentClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { onEvent(CourseMembersUiEvent.SearchChanged(it)) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Buscar compañeros...", style = MaterialTheme.typography.bodyMedium) },
                leadingIcon = { Icon(Icons.Outlined.Search, "Buscar", tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                trailingIcon = {
                    if (state.searchQuery.isNotBlank()) {
                        IconButton(onClick = { onEvent(CourseMembersUiEvent.SearchChanged("")) }) {
                            Icon(Icons.Filled.Close, "Limpiar búsqueda")
                        }
                    }
                },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryBlue,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                singleLine = true
            )
        }

        item {
            Text(
                "${filteredMembers.size} ${if (filteredMembers.size == 1) "compañero visible" else "compañeros visibles"}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }

        if (state.isRefreshing) item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                CircularProgressIndicator(Modifier.size(24.dp), color = PrimaryBlue)
            }
        }

        state.errorMessage?.let { message -> item {
            ErrorCard(message, { onEvent(CourseMembersUiEvent.Load) }, { onEvent(CourseMembersUiEvent.ClearError) })
        } }

        if (filteredMembers.isEmpty()) item {
            EmptyCard(if (state.searchQuery.isBlank()) "Aún no hay compañeros visibles en este curso." else "No se encontraron compañeros.")
        } else items(filteredMembers, key = { it.studentId }) { member ->
            MemberItem(member) { onStudentClick(member.studentId) }
        }
    }
}

@Composable
fun MemberItem(member: CourseMember, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MemberAvatar(member)
            Spacer(Modifier.width(16.dp))
            Text(
                member.displayName,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.width(10.dp))
            Row(
                Modifier.background(PointsBackground, RoundedCornerShape(20.dp)).padding(horizontal = 11.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Outlined.StarOutline, null, Modifier.size(18.dp), tint = PrimaryBlue)
                Spacer(Modifier.width(4.dp))
                Text("${formatPoints(member.totalPoints)} pts", color = PrimaryBlue, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun MemberAvatar(member: CourseMember) {
    val initials = member.displayName.trim().split(Regex("\\s+")).filter(String::isNotBlank).take(2).joinToString("") { it.first().uppercase() }.ifBlank { "?" }
    val paletteIndex = member.studentId.hashCode().mod(3)
    val background = when (paletteIndex) { 0 -> PrimaryBlue; 1 -> AvatarLavender; else -> AvatarRose }
    val content = if (paletteIndex == 1) MaterialTheme.colorScheme.onSurfaceVariant else Color.White
    Box(Modifier.size(52.dp).clip(CircleShape).background(background), contentAlignment = Alignment.Center) {
        Text(initials, color = content, fontSize = 20.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun ErrorCard(message: String, onRetry: () -> Unit, onDismiss: () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer), shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(16.dp)) {
            Text(message, color = MaterialTheme.colorScheme.onErrorContainer)
            Row {
                TextButton(onClick = onRetry) { Text("Reintentar", color = MaterialTheme.colorScheme.onErrorContainer) }
                TextButton(onClick = onDismiss) { Text("Cerrar", color = MaterialTheme.colorScheme.onErrorContainer) }
            }
        }
    }
}

@Composable
private fun EmptyCard(message: String) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))) {
        Text(message, Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun ErrorView(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.padding(24.dp), Arrangement.Center, Alignment.CenterHorizontally) {
        Text(message, color = MaterialTheme.colorScheme.error)
        Button(onRetry, Modifier.padding(top = 16.dp), colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)) { Text("Reintentar") }
    }
}

@Composable
private fun LoadingView(modifier: Modifier) = Box(modifier, contentAlignment = Alignment.Center) { CircularProgressIndicator(color = PrimaryBlue) }

@Composable
fun EmptyView(message: String) = EmptyCard(message)

private fun formatPoints(points: Int): String = NumberFormat.getIntegerInstance(Locale.US).format(points)

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun CourseMembersScreenPreview() {
    val members = listOf(
        CourseMember("1", "Adonis García", 1250), CourseMember("2", "María Rodríguez", 980),
        CourseMember("3", "Juan Sánchez", 850), CourseMember("4", "Lucía Castro", 720)
    )
    MentorlyTheme {
        CourseMembersContent(CourseMembersUiState(members = members), members, {}, {}, {})
    }
}

@Preview(showBackground = true)
@Composable
fun CourseMembersScreenEmptyPreview() {
    MentorlyTheme { CourseMembersContent(CourseMembersUiState(), emptyList(), {}, {}, {}) }
}
