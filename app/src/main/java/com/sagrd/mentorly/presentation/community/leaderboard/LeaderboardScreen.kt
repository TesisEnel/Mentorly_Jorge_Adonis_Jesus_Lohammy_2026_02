package com.sagrd.mentorly.presentation.community.leaderboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import com.sagrd.mentorly.domain.model.community.LeaderboardEntry
import com.sagrd.mentorly.ui.theme.MentorlyTheme
import java.text.NumberFormat
import java.util.Locale

private val PrimaryBlue = Color(0xFF1565C0)
private val HeroBlue = Color(0xFF2377E5)
private val Gold = Color(0xFFF57C00)
private val Silver = Color(0xFFE5E2E9)
private val Bronze = Color(0xFF8A5868)

@Composable
fun LeaderboardScreen(
    courseId: String,
    onBackClick: () -> Unit,
    onStudentClick: ((String) -> Unit)? = null,
    viewModel: LeaderboardViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val filteredEntries by viewModel.filteredEntries.collectAsStateWithLifecycle()

    LaunchedEffect(courseId) { viewModel.setCourseId(courseId) }

    LeaderboardContent(
        state = state,
        filteredEntries = filteredEntries,
        onEvent = viewModel::onEvent,
        onBackClick = onBackClick,
        onStudentClick = { onStudentClick?.invoke(it) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardContent(
    state: LeaderboardUiState,
    filteredEntries: List<LeaderboardEntry>,
    onEvent: (LeaderboardUiEvent) -> Unit,
    onBackClick: () -> Unit,
    onStudentClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Mentorly", color = PrimaryBlue, fontWeight = FontWeight.Bold, fontSize = 24.sp) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Atrás")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { onEvent(LeaderboardUiEvent.Refresh) },
                        enabled = !state.isLoading && !state.isRefreshing && state.hasSession
                    ) {
                        Icon(Icons.Outlined.Refresh, "Actualizar", tint = PrimaryBlue)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        when {
            state.isLoading && state.entries.isEmpty() -> LoadingView(Modifier.fillMaxSize().padding(innerPadding))
            !state.hasSession -> ErrorView(
                state.errorMessage ?: "No se encontró una sesión activa.",
                { onEvent(LeaderboardUiEvent.Load) },
                Modifier.fillMaxSize().padding(innerPadding)
            )
            else -> LeaderboardSections(
                state,
                filteredEntries,
                onEvent,
                onStudentClick,
                Modifier.fillMaxSize().padding(innerPadding)
            )
        }
    }
}

@Composable
private fun LeaderboardSections(
    state: LeaderboardUiState,
    filteredEntries: List<LeaderboardEntry>,
    onEvent: (LeaderboardUiEvent) -> Unit,
    onStudentClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Ranking del curso", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(
                    "Descubre tu progreso y compáralo con tus compañeros.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        state.ownPosition?.let { entry -> item { OwnPositionCard(entry) } }

        item {
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { onEvent(LeaderboardUiEvent.SearchChanged(it)) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Buscar en ranking...", style = MaterialTheme.typography.bodyMedium) },
                leadingIcon = { Icon(Icons.Outlined.Search, "Buscar") },
                trailingIcon = {
                    if (state.searchQuery.isNotBlank()) {
                        IconButton(onClick = { onEvent(LeaderboardUiEvent.SearchChanged("")) }) {
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

        if (state.isRefreshing) item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                CircularProgressIndicator(Modifier.size(24.dp), color = PrimaryBlue)
            }
        }

        state.errorMessage?.let { message -> item {
            ErrorCard(message, { onEvent(LeaderboardUiEvent.Load) }, { onEvent(LeaderboardUiEvent.ClearError) })
        } }

        if (filteredEntries.isEmpty()) item {
            EmptyCard(if (state.searchQuery.isBlank()) "Todavía no hay estudiantes visibles en el ranking." else "No se encontraron estudiantes.")
        } else item {
            LeaderboardListCard(filteredEntries, state.ownPosition?.studentId, onStudentClick)
        }

        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
fun OwnPositionCard(entry: LeaderboardEntry) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = HeroBlue),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                InitialsAvatar(entry.displayName, isOwn = true, size = 60)
                Spacer(Modifier.width(16.dp))
                Column {
                    Text("Tu posición actual", color = Color.White.copy(alpha = 0.9f), style = MaterialTheme.typography.bodyMedium)
                    Text(entry.displayName, color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            Spacer(Modifier.height(16.dp))
            Row(
                Modifier.fillMaxWidth().background(Color.Black.copy(alpha = 0.08f), RoundedCornerShape(16.dp)).padding(16.dp),
                Arrangement.SpaceBetween,
                Alignment.CenterVertically
            ) {
                Column {
                    Text("Posición", color = Color.White.copy(alpha = 0.9f), style = MaterialTheme.typography.bodySmall)
                    Text("#${entry.rank}", color = Color.White, fontSize = 42.sp, fontWeight = FontWeight.Bold)
                }
                Row(
                    Modifier.background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(20.dp)).padding(horizontal = 12.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Star, null, Modifier.size(18.dp), tint = Color.White)
                    Spacer(Modifier.width(5.dp))
                    Text("${formatPoints(entry.totalPoints)} pts", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun LeaderboardListCard(entries: List<LeaderboardEntry>, ownStudentId: String?, onStudentClick: (String) -> Unit) {
    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Column {
            entries.forEachIndexed { index, entry ->
                LeaderboardItem(entry, entry.studentId == ownStudentId) { onStudentClick(entry.studentId) }
                if (index < entries.lastIndex) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
            }
        }
    }
}

@Composable
fun LeaderboardItem(entry: LeaderboardEntry, isOwn: Boolean, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().background(if (isOwn) PrimaryBlue.copy(alpha = 0.08f) else Color.Transparent)
            .clickable(onClick = onClick).padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RankBadge(entry.rank)
        Spacer(Modifier.width(12.dp))
        InitialsAvatar(entry.displayName, isOwn, 40)
        Spacer(Modifier.width(12.dp))
        Text(
            text = entry.displayName + if (isOwn) " (Tú)" else "",
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (isOwn) FontWeight.Bold else FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            formatPoints(entry.totalPoints),
            color = if (isOwn) PrimaryBlue else if (entry.rank == 1) Gold else MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isOwn || entry.rank == 1) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
private fun RankBadge(rank: Int) {
    val color = when (rank) { 1 -> Gold; 2 -> Silver; 3 -> Bronze; else -> Color.Transparent }
    Box(
        Modifier.size(34.dp).clip(CircleShape).background(color),
        contentAlignment = Alignment.Center
    ) {
        Text(rank.toString(), color = if (rank == 1 || rank == 3) Color.White else MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun InitialsAvatar(displayName: String, isOwn: Boolean, size: Int) {
    val initials = displayName.trim().split(Regex("\\s+")).filter(String::isNotBlank).take(2).joinToString("") { it.first().uppercase() }.ifBlank { "?" }
    Box(
        Modifier.size(size.dp).clip(CircleShape).background(if (isOwn) if (size > 40) Color.White else HeroBlue else MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Text(initials, color = if (isOwn) PrimaryBlue else MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold, fontSize = if (size > 40) 24.sp else 16.sp)
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
fun LeaderboardScreenPreview() {
    val entries = listOf(
        LeaderboardEntry("1", "Carlos Cruz", 3400, 1), LeaderboardEntry("2", "Laura Mora", 3150, 2),
        LeaderboardEntry("3", "Javier Peña", 2900, 3), LeaderboardEntry("4", "Sofía Vargas", 2500, 4),
        LeaderboardEntry("5", "Ana García", 1250, 12), LeaderboardEntry("6", "David López", 1100, 13)
    )
    MentorlyTheme {
        LeaderboardContent(
            LeaderboardUiState(entries = entries, ownPosition = entries[4]), entries, {}, {}, {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun LeaderboardScreenEmptyPreview() {
    MentorlyTheme { LeaderboardContent(LeaderboardUiState(), emptyList(), {}, {}, {}) }
}
