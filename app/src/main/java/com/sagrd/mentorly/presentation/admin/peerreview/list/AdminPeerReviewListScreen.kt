package com.sagrd.mentorly.presentation.admin.peerreview.list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingFlat
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sagrd.mentorly.domain.model.peerreview.PeerReview
import com.sagrd.mentorly.ui.theme.MentorlyTheme
import com.sagrd.mentorly.util.DateFormatter

@Composable
fun AdminPeerReviewListScreen(
    onBackClick: () -> Unit,
    onPeerReviewClick: (String) -> Unit,
    viewModel: AdminPeerReviewListViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val filteredPeerReviews by viewModel.filteredPeerReviews.collectAsStateWithLifecycle()

    AdminPeerReviewListContent(
        state = state,
        peerReviews = filteredPeerReviews,
        onBackClick = onBackClick,
        onPeerReviewClick = onPeerReviewClick,
        onEvent = viewModel::onEvent
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminPeerReviewListContent(
    state: AdminPeerReviewListUiState,
    peerReviews: List<PeerReview>,
    onBackClick: () -> Unit,
    onPeerReviewClick: (String) -> Unit,
    onEvent: (AdminPeerReviewListUiEvent) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Auditoría de revisiones",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
                actions = {
                    IconButton(onClick = { onEvent(AdminPeerReviewListUiEvent.Refresh) }) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Recargar")
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
                onValueChange = { onEvent(AdminPeerReviewListUiEvent.SearchChanged(it)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Buscar por comentario...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = RoundedCornerShape(24.dp),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )

            // Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PeerReviewFilter.entries.forEach { filter ->
                    val isSelected = state.selectedFilter == filter
                    FilterChip(
                        selected = isSelected,
                        onClick = { onEvent(AdminPeerReviewListUiEvent.FilterChanged(filter)) },
                        label = {
                            Text(
                                text = when (filter) {
                                    PeerReviewFilter.All -> "Todas"
                                    PeerReviewFilter.Approved -> "Aprobadas"
                                    PeerReviewFilter.Rejected -> "Rechazadas"
                                }
                            )
                        },
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }

            if (state.isLoading && !state.isRefreshing && state.peerReviews.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (state.errorMessage != null && state.peerReviews.isEmpty()) {
                ErrorView(
                    message = state.errorMessage,
                    onRetry = { onEvent(AdminPeerReviewListUiEvent.Load) }
                )
            } else if (peerReviews.isEmpty() && !state.isLoading) {
                EmptyView(message = "No se encontraron revisiones.")
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(peerReviews, key = { it.id }) { review ->
                        AdminPeerReviewItem(
                            review = review,
                            onClick = { onPeerReviewClick(review.id) }
                        )
                    }

                    if (state.isLoading) {
                        item {
                            Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminPeerReviewItem(
    review: PeerReview,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Status Badge
            StatusBadgeItem(isApproved = review.isApproved)

            Spacer(modifier = Modifier.height(12.dp))

            // Feedback snippet
            Text(
                text = review.feedbackComment,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = DateFormatter.format(review.createdAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = onClick,
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    Text("Ver auditoría", fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.TrendingFlat,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusBadgeItem(isApproved: Boolean) {
    Surface(
        color = if (isApproved) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
        contentColor = if (isApproved) Color(0xFF2E7D32) else Color(0xFFC62828),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isApproved) Icons.Default.CheckCircle else Icons.Default.Cancel,
                contentDescription = null,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = if (isApproved) "Aprobada" else "Rechazada",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ErrorView(message: String, onRetry: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = message, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRetry) { Text("Reintentar") }
        }
    }
}

@Composable
private fun EmptyView(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = message, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Preview(showBackground = true)
@Composable
fun AdminPeerReviewListScreenPreview() {
    MentorlyTheme {
        AdminPeerReviewListContent(
            state = AdminPeerReviewListUiState(),
            peerReviews = listOf(
                PeerReview("1", "sub-1", "rev-1", true, "El análisis presentado demuestra una comprensión profunda de los principios...", "2023-10-12T14:30:00Z"),
                PeerReview("2", "sub-2", "rev-2", false, "La entrega no cumple con los criterios mínimos de la rúbrica. Faltan citas bibliográficas y la...", "2023-10-10T09:15:00Z"),
                PeerReview("3", "sub-3", "rev-3", true, "Excelente redacción y síntesis. Los argumentos están bien fundamentados y la conclusión se...", "2023-10-08T16:45:00Z")
            ),
            onBackClick = {},
            onPeerReviewClick = {},
            onEvent = {}
        )
    }
}
